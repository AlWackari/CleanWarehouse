package infrastructure.bootstrap;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import core.entities.Bicycle;
import core.services.Controller;
import infrastructure.input.BicyleRestAdapter;
import infrastructure.output.FileAdapter;

public class Start {

	public static void main(String[] args) {
		
		Controller<Bicycle> service;
		try { 
			service = new Controller<Bicycle>(new FileAdapter<Bicycle>(Files.createFile(FileSystems.getDefault().getPath("./src/main/resources", "warehouse.db"))));
		} catch (IOException e) { 
			service = new Controller<Bicycle>(new FileAdapter<Bicycle>(FileSystems.getDefault().getPath("./src/main/resources", "warehouse.db")));}
		
		BicyleRestAdapter server = new BicyleRestAdapter(service);
		
		try {
			int port = Integer.valueOf(args[0]);
			server.start(port); 
			System.out.println("Server running on port "+port);
		} catch (IOException | NumberFormatException e) {System.out.println(e.getMessage());}
	}
}
