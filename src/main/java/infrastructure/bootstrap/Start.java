package infrastructure.bootstrap;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import core.services.Controller;
import infrastructure.input.RestAdapter;
import infrastructure.output.FileAdapter;

public class Start {

	public static void main(String[] args) {
		
		Controller service;
		try { 
			service = new Controller(new FileAdapter(Files.createFile(FileSystems.getDefault().getPath("./src/main/resources", "warehouse.db"))));
		} catch (IOException e) { 
			service = new Controller(new FileAdapter(FileSystems.getDefault().getPath("./src/main/resources", "warehouse.db")));}
		
		RestAdapter server = new RestAdapter(service);
		
		try {
			int port = Integer.valueOf(args[0]);
			server.start(port); 
			System.out.println("Server running on port "+port);
		} catch (IOException | NumberFormatException e) {System.out.println(e.getMessage());}
	}
}
