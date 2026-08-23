package integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.sql.Time;
import java.time.Instant;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.io.TempDir;

import config.StorageConfig;
import core.entities.Bicycle;
import core.entities.Product;
import infrastructure.context.Context;
import infrastructure.input.BicycleRestAdapter;
import infrastructure.output.FileAdapter;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestAPIServerFlow {
	
	private BicycleRestAdapter server;
	private final HttpClient client = HttpClient.newHttpClient();
	private int port;
	
	@BeforeAll
	void setUp(@TempDir Path tempDir){
		try {this.server = new BicycleRestAdapter(new FileAdapter<Bicycle>(tempDir.resolve("test-warehous.dat")), 
			  									  new FileAdapter<Product>(tempDir.resolve("test-warehous.dat")));
			 this.port = this.server.start(0); System.out.println("Server running on port "+this.port);}
		catch (IOException e) {fail("Cannot instantiate server on a free port");}
	}
	
	@Test
	@Order(1)
	void testAdminSetup() {
		try {assertTrue(this.setup().body().contains("OK"));}
		catch (IOException | InterruptedException e) {{fail("Destination unreacheable");}}
	}
	
	@Test
	@Order(2)
	void testSetupTwice() {
		try {assertTrue(!this.setup().body().contains("OK"));}
		catch (IOException | InterruptedException e) {{fail("Destination unreacheable");}}
	}	
	
	@Test
	void testWelcome() {
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace))
					.header("Content-Type", "application/json")
    				.header("X-User", "TestUser")
					.GET().build();
			HttpResponse<String> response = this.client.send(request, BodyHandlers.ofString());
			assertTrue(response.body().contains("Welcome"));
		} catch (IOException | InterruptedException e) {fail("Destination unreacheable");}
	}
	
	@Test
	void testPick() {
		try {
//			get first or refill and get
			HttpResponse<String> response = getWarehouse();
			String serial="";
			try {serial = response.body().split("serial")[1].split(",")[0].split("\"")[2];}
			catch(ArrayIndexOutOfBoundsException e) {
				this.refill(); response = getWarehouse();
				serial = response.body().split("serial")[1].split(",")[0].split("\"")[2];
			}

//			pick 
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace + BicycleRestAdapter.pick + serial))
	                .header("Content-Type", "application/json")
    				.header("X-User", "TestUser")
	                .DELETE().build();
//			check
	        try { response = this.client.send(request, BodyHandlers.ofString());
	        	  assertTrue(response.body().contains(serial));}
	        catch (IOException | InterruptedException e) {fail("Destination unreacheable");}}
		
		catch (IOException | InterruptedException e) {fail("Destination unreacheable");}
	}

	@Test
	void testPutTwice() {
		try {
//			create
			String ID = String.valueOf(Time.from(Instant.now()).getTime());
	        String jsonBici = "{\"serial\":\""+ID+"\", \"color\":101010, \"price\":250.00}";
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace + BicycleRestAdapter.bikenode))
	                .header("Content-Type", "application/json")
					.header("X-User", "TestUser")
	                .POST(BodyPublishers.ofString(jsonBici)).build();
			this.client.send(request, BodyHandlers.ofString());
//			twice
			HttpResponse<String> response = this.client.send(request, BodyHandlers.ofString());
			assertFalse(response.statusCode()==200);
			
		} catch (IOException | InterruptedException e) {fail("Destination unreacheable");}
	}
	
	@Test
	void testCreateNewWithAvailabilityCheck() {
        try {int available=0;
//    		get available slots
    		HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace+BicycleRestAdapter.slots))
                    .header("Content-Type", "application/json")
    				.header("X-User", "TestUser")
                    .GET().build();
    		
            HttpResponse<String> response;
        	 response = this.client.send(request, BodyHandlers.ofString());
        	 available = Integer.valueOf(response.body());

//			create
			String ID = String.valueOf(Time.from(Instant.now()).getTime());
	        String jsonBici = "{\"serial\":\""+ID+"\", \"color\":101010, \"price\":250.00}";
	        request = HttpRequest.newBuilder()
	                .uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace + BicycleRestAdapter.bikenode))
	                .header("Content-Type", "application/json")
    				.header("X-User", "TestUser")
	                .POST(BodyPublishers.ofString(jsonBici)).build();
	        response = this.client.send(request, BodyHandlers.ofString());

//			check
			response = getWarehouse();
			if(available>0) assertTrue(response.body().contains(ID));
			else assertFalse(response.body().contains(ID));
        }catch (IOException | InterruptedException e) {fail("Destination unreacheable");}
	}

	@Test
	void testRefill() {
		try {
			HttpResponse<String> response;
			int len = 0;
//			warehouse before
			response = getWarehouse();
			len = response.body().length();

//	 		refill when needed
			response = this.refill();
			if(response.statusCode()==400) return;
			
//			warehouse after
			response = getWarehouse();
			assertTrue(response.body().length()>len);
		} catch (IOException | InterruptedException e) {fail("Destination unreacheable");}
	}
	
	private HttpResponse<String> getWarehouse() throws IOException, InterruptedException{
//		get all
		HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace + BicycleRestAdapter.bikenode))
                .header("Content-Type", "application/json")
                .GET().build();
		return this.client.send(request, BodyHandlers.ofString());
	}
	
	private HttpResponse<String> refill() throws IOException, InterruptedException{
		HttpRequest request = HttpRequest.newBuilder()
	            .uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace+BicycleRestAdapter.bikenode+BicycleRestAdapter.refill))
	            .header("Content-Type", "application/json")
				.header("X-User", "TestUser")
	            .POST(BodyPublishers.ofString(new String())).build();
		return this.client.send(request, BodyHandlers.ofString());
	}
	
	private HttpResponse<String> setup() throws IOException, InterruptedException{
		String jsonBici = StorageConfig.getStorageJSON();
		HttpRequest request = HttpRequest.newBuilder()
    				.uri(URI.create("http://localhost:" + this.port + BicycleRestAdapter.namespace + BicycleRestAdapter.admin))
    				.header("Content-Type", "application/json")
    				.header("X-Admin-Key", "secret123")
    				.header("X-User", "TestUser")
    				.POST(BodyPublishers.ofString(jsonBici))
    				.build();
		return this.client.send(request, BodyHandlers.ofString());
	}
	
	@AfterAll
	public void stop(){this.server.stop(0); Context.clear();}
}
