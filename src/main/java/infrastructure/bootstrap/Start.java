package infrastructure.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import core.entities.Bicycle;
import core.entities.Product;
import infrastructure.input.BicycleRestAdapter;
import infrastructure.output.FileAdapter;

public class Start {

	public static void main(String[] args) {
		
		Path path = Path.of("./data/warehouse.db");
		BicycleRestAdapter server = null;
		try { Files.createDirectories(path.getParent());
			 if(Files.notExists(path)) Files.createFile(path);
			 server = new BicycleRestAdapter(new FileAdapter<Bicycle>(path), new FileAdapter<Product>(path));}
		catch (IOException  e) {System.out.println(e.getMessage()); System.exit(1);}
		
		try { int port = Integer.valueOf(args[0]); server.start(port); System.out.println("Server running on port "+port);}
		catch (IOException | NumberFormatException e) {System.out.println(e.getMessage());}
	}
}
