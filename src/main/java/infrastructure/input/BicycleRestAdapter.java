package infrastructure.input;

import core.entities.Bicycle;
import core.entities.Product;
import core.entities.Storage.StatefulBin;
import core.entities.Vendor;
import core.entities.Warehouse;
import core.ports.Layout;
import core.ports.Repository;
import core.services.IniController;
import core.services.OpController;
import infrastructure.context.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;

import javax.management.InstanceAlreadyExistsException;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;;

public class BicycleRestAdapter extends BicycleUIAdapter implements HttpHandler{
	
	private static class BiciclettaJSON{
		public final String serial; public final int color; public final double price;
		@SuppressWarnings("unused")
		public BiciclettaJSON(@JsonProperty("serial") String serial, 
							  @JsonProperty("color") int color,
							  @JsonProperty("price") double price) {this.serial=serial; this.color=color; this.price=price;}}
	
	public final static String namespace = "/api/v1/magat", bikenode = "/bike", admin = "/admin/init", refill = "/refill", slots = "/slots",
							   pick = "/pick/", html = "text/html; charset=UTF-8", plain="text/plain; charset=UTF-8";
	private HttpServer server;
	private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
	private final Repository<Bicycle> repo;
	
	public BicycleRestAdapter(Repository<Bicycle> repo, Layout<Product> layo) {
		super(null, new IniController(layo)); this.repo=repo;
		try {super.service = new OpController<Bicycle>(repo);}
		catch(IllegalStateException e) {}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String[] pathString = exchange.getRequestURI().getPath().split(namespace);
		String method =exchange.getRequestMethod(), code="OK";
		int status;
		System.out.println("HTTP Requst " + method + " | PATH: " + exchange.getRequestURI());
		Context.put(Context.keys.USER, this.getUser(exchange));
		try {
			switch (method) {
			case "GET":
				if(pathString.length<1) this.send(exchange, 200, 
												 new String(getClass().getResourceAsStream("/welcome.html").readAllBytes(), StandardCharsets.UTF_8), 
												 html);
				else {
					if(!this.checkInit(exchange)) break;
					if(pathString[1].toLowerCase().equals(bikenode)) 
						this.send(exchange, 200, this.mapper.writeValueAsString(super.service.inventory()), html);
					else if(pathString[1].toLowerCase().equals(slots))
						this.send(exchange, 200, this.mapper.writeValueAsString(super.service.availability()), plain);
					else this.send(exchange, 404, "Service not found", plain);
				} break;
			case "POST":
				if(pathString.length<1) this.send(exchange, 404, "Service not found", plain);
				else if(pathString[1].toLowerCase().equals(bikenode)) {
					if(!this.checkInit(exchange)) break;
					BiciclettaJSON bici = this.mapper.readValue(exchange.getRequestBody(), BiciclettaJSON.class);
					Bicycle b = new Bicycle(bici.serial, bici.color, bici.price);
					List<Bicycle> list = new ArrayList<Bicycle>(); list.add(b);
					List<Entry<String, List<Bicycle>>> result = this.service.put(list).entrySet().stream()
																								 .filter(item->item.getKey()!=Warehouse._dupli).toList();
					try {status = result.getFirst().getValue().contains(b)?200:400;} catch(NoSuchElementException e) {status=400; code=e.getMessage();}
					code = status==200?"OK":"KO";
					this.send(exchange, status, code, plain);}
				else if(pathString[1].toLowerCase().equals(bikenode+refill)) {
					if(!this.checkInit(exchange)) break;
					status = this.service.refill(new Vendor(10))? 200 : 400;
					code = status==200?"OK":"KO";
					this.send(exchange, status, code, plain);}
				else if(pathString[1].toLowerCase().equals(admin)) {
					String envKey = System.getenv("MAGAT_ADMIN_KEY");
					if(envKey==null) {this.send(exchange, 400, "Missing environment pwd variable", plain); break;}
					String adm = exchange.getRequestHeaders().getFirst("X-Admin-Key");
					if(!envKey.equals(adm)) {this.send(exchange, 404, "Service not found", plain); break;}
					List<StatefulBin<Product>> bins = this.mapper.readValue(exchange.getRequestBody(), new TypeReference<List<StatefulBin<Product>>>(){});
					try {status = this.adm.initDB(bins)?200 : 400;}catch(InstanceAlreadyExistsException e) {status = 400; code=e.toString();}
					if(super.service==null) super.service = new OpController<Bicycle>(this.repo);
					this.send(exchange, status, code, plain);}
				else this.send(exchange, 404, "Service not found", plain);
				break;
			case "DELETE":
				if(!this.checkInit(exchange)) break;
				if(pathString.length==0) this.send(exchange, 404, "Service not found", plain);
				else if(pathString[1].toLowerCase().contains(pick)) {
					String serial = pathString[1].toLowerCase().split(pick)[1];
					try{this.send(exchange, 200, this.mapper.writeValueAsString(this.service.pickOne(serial)), plain);}
					catch (ArrayIndexOutOfBoundsException e) {this.send(exchange, 400, "Serial not found", plain);}}
				else this.send(exchange, 404, "Service not found", plain); break;
			default: this.send(exchange, 404, "Service not found", plain);}}
		catch(Exception e) {e.printStackTrace(); try{this.send(exchange, 500, "Internal Server Error :(", plain);} catch (Exception e1) {}}
		finally {exchange.getRequestBody().close(); exchange.close(); Context.clear();}
	}
	
	public int start(int port) throws IOException {
		this.server = HttpServer.create(new InetSocketAddress(port), 0);
		this.server.createContext(namespace, this);
		this.server.setExecutor(Executors.newCachedThreadPool());
		this.server.start();	
		return server.getAddress().getPort();
	}
	
	public void stop(int seconds) {if(this.server != null) this.server.stop(seconds);}
	
	private void send(HttpExchange exchange, int status, String response, String type) {
		try {
			byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", type);
			exchange.sendResponseHeaders(status, bytes.length);
			OutputStream os = exchange.getResponseBody();
			os.write(bytes);
			os.close();
		} catch (IOException e) {}
	}
	
	private boolean checkInit(HttpExchange exchange) throws IOException {
	    if(this.service == null) {this.send(exchange, 400, "Initialization missing", plain); return false;}
	    return true;
	}
	private String getUser(HttpExchange exchange) {
		String usr = exchange.getRequestHeaders().getFirst("X-User");
		if(usr==null) exchange.getRequestHeaders().getFirst("User-Agent");
		if(usr==null) exchange.getRequestHeaders().getFirst("X-Forwarded-For");
		if(usr==null) exchange.getRemoteAddress().getAddress().getHostAddress();
		if(usr==null) usr="anonymous";
		return usr;
	}
}
