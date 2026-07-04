package infrastructure.input;

import core.entities.Bicycle;
import core.entities.Vendor;
import core.services.Controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;;

public class RestAdapter extends UIAdapter implements HttpHandler{
	
	private static class BiciclettaJSON{
		public final String serial; public final int color; public final double price;
		@SuppressWarnings("unused")
		public BiciclettaJSON(@JsonProperty("serial") String serial, 
							  @JsonProperty("color") int color,
							  @JsonProperty("price") double price) {this.serial=serial; this.color=color; this.price=price;}}
	
	public final static String namespace = "/api/v1/magat/";
	private HttpServer server;
	private final String context = RestAdapter.namespace;
	private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
	
	public RestAdapter(Controller c) {super(c);}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String[] pathString = exchange.getRequestURI().getPath().split(this.context);
		String method =exchange.getRequestMethod();
		String code;
		int status;
		
		switch (method) {
		case "GET":
			if(pathString.length==0) 
				this.send(exchange, 200, this.mapper.writeValueAsString(super.service.list()));
			else if(pathString[1].toLowerCase().equals("/slots"))
				this.send(exchange, 200, this.mapper.writeValueAsString(super.service.getSlots()));
			else exchange.sendResponseHeaders(404, -1);
			break;
		case "POST":
			if(pathString.length==0) {
				BiciclettaJSON bici = this.mapper.readValue(exchange.getRequestBody(), BiciclettaJSON.class);
				status = this.service.push(new Bicycle(bici.serial, bici.color, bici.price))? 200 : 400;
				code = status==200?"OK":"KO";
				this.send(exchange, status, code);}
			else if(pathString[1].toLowerCase().contains("/refill")) {
				status = this.service.refill(new Vendor(10))? 200 : 400;
				code = status==200?"OK":"KO";
				this.send(exchange, status, code);}
			else exchange.sendResponseHeaders(404, -1);
			break;
		case "DELETE":
			if(pathString.length==0) exchange.sendResponseHeaders(404, -1);
			else if(pathString[1].toLowerCase().contains("pick/")) {
				String serial = pathString[1].toLowerCase().split("pick/")[1];
				try{this.send(exchange, 200, this.mapper.writeValueAsString(this.service.pickOne(serial)));}
				catch (ClassNotFoundException | ArrayIndexOutOfBoundsException e) {exchange.sendResponseHeaders(404, -1);}}
			else exchange.sendResponseHeaders(404, -1); break;
		default: exchange.sendResponseHeaders(404, -1);
		}
		
	}
	
	public int start(int port) throws IOException {
		this.server = HttpServer.create(new InetSocketAddress(port), 0);
		this.server.createContext(this.context, this);
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
}
