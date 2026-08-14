package infrastructure.bootstrap;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import core.entities.Bicycle;
import core.entities.Product;
import infrastructure.input.BicycleRestAdapter;
import infrastructure.output.FileAdapter;

public class Start {

	public static void main(String[] args) {
		
		Path path;
		try { path = Files.createFile(FileSystems.getDefault().getPath("./src/main/resources", "warehouse.db"));}
		catch (IOException e) {path = FileSystems.getDefault().getPath("./src/main/resources", "warehouse.db");}
		
		BicycleRestAdapter server = new BicycleRestAdapter(new FileAdapter<Bicycle>(path), new FileAdapter<Product>(path));
		
		try {
			int port = Integer.valueOf(args[0]);
			server.start(port); 
			System.out.println("Server running on port "+port);
		} catch (IOException | NumberFormatException e) {System.out.println(e.getMessage());}
	}
}
