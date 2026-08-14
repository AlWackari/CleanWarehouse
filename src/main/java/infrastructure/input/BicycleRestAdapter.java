package infrastructure.input;

import core.entities.Bicycle;
import core.entities.Product;
import core.entities.Storage.StatefulBin;
import core.entities.Vendor;
import core.ports.Layout;
import core.ports.Repository;
import core.services.IniController;
import core.services.OpController;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

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
	
	public final static String namespace = "/api/v1/magat/";
	public final static String bikenode = "/bike";
	public final static String admin = "/admin/init";
	private HttpServer server;
	private final String context = BicycleRestAdapter.namespace;
	private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
	private final Repository<Bicycle> repo;
	
	public BicycleRestAdapter(Repository<Bicycle> repo, Layout<Product> layo) {super(null, new IniController(layo)); this.repo=repo;}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String[] pathString = exchange.getRequestURI().getPath().split(this.context);
		String method =exchange.getRequestMethod();
		String code;
		int status;
		
		switch (method) {
		case "GET":
			this.checkInit(exchange);
			if(pathString[1].toLowerCase().equals(bikenode)) 
				this.send(exchange, 200, this.mapper.writeValueAsString(super.service.inventory()));
			else if(pathString[1].toLowerCase().equals("/slots"))
				this.send(exchange, 200, this.mapper.writeValueAsString(super.service.availability()));
			else exchange.sendResponseHeaders(404, -1);
			break;
		case "POST":
			if(pathString[1].toLowerCase().equals(bikenode)) {
				this.checkInit(exchange);
				BiciclettaJSON bici = this.mapper.readValue(exchange.getRequestBody(), BiciclettaJSON.class);
				Bicycle b = new Bicycle(bici.serial, bici.color, bici.price);
				List<Bicycle> list = new ArrayList<Bicycle>(); list.add(b);
				Map<String, List<Bicycle>> map = this.service.put(list);
				status = map.entrySet().iterator().next().getValue().contains(b)?200:400;
				code = status==200?"OK":"KO";
				this.send(exchange, status, code);}
			else if(pathString[1].toLowerCase().equals(bikenode+"/refill")) {
				this.checkInit(exchange);
				status = this.service.refill(new Vendor(10))? 200 : 400;
				code = status==200?"OK":"KO";
				this.send(exchange, status, code);}
			else if(pathString[1].toLowerCase().equals("/admin/init")) {
				String adm = exchange.getRequestHeaders().getFirst("X-Admin-Key");
				if(!System.getenv("MAGAT_ADMIN_KEY").equals(adm)) exchange.sendResponseHeaders(401, -1);
				List<StatefulBin<Product>> bins = this.mapper.readValue(exchange.getRequestBody(), new TypeReference<List<StatefulBin<Product>>>(){});
				status = this.adm.initDB(bins)?200 : 400;
				code = status==200?"OK":"KO";
				if(super.service==null) super.service = new OpController<Bicycle>(this.repo);
				this.send(exchange, status, code);
			}
			else exchange.sendResponseHeaders(404, -1);
			break;
		case "DELETE":
			if(pathString.length==0) exchange.sendResponseHeaders(404, -1);
			else if(pathString[1].toLowerCase().contains("pick/")) {
				String serial = pathString[1].toLowerCase().split("pick/")[1];
				try{this.send(exchange, 200, this.mapper.writeValueAsString(this.service.pickOne(serial)));}
				catch (ArrayIndexOutOfBoundsException e) {exchange.sendResponseHeaders(404, -1);}}
			else exchange.sendResponseHeaders(404, -1); break;
		default: exchange.sendResponseHeaders(404, -1);}
		exchange.getRequestBody().close(); exchange.close();
	}
	
	public int start(int port) throws IOException {
		this.server = HttpServer.create(new InetSocketAddress(port), 0);
		this.server.createContext(this.context, this);
		this.server.setExecutor(Executors.newCachedThreadPool());
		this.server.start();	
		return server.getAddress().getPort();
	}
	
	public void stop(int seconds) {if(this.server != null) this.server.stop(seconds);}
	
	private void send(HttpExchange exchange, int status, String response) {
		try {
			byte[] bytes = response.getBytes("UTF-8");
			exchange.sendResponseHeaders(status, bytes.length);
			OutputStream os = exchange.getResponseBody();
			os.write(bytes);
			os.close();
		} catch (IOException e) {}
	}
	
	private void checkInit(HttpExchange exchange) throws IOException {if(this.service == null) exchange.sendResponseHeaders(404, -1);}
}
