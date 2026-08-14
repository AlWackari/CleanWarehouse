package integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import config.StorageConfig;
import core.entities.Bicycle;
import core.entities.Product;
import core.entities.Vendor;
import core.services.IniController;
import core.services.OpController;
import infrastructure.output.FileAdapter;

class TestControllers {
	
	private OpController<Bicycle> admin;
	
	@TempDir
    Path tempDir;
	
	@BeforeEach
	void setUp(){
		Path path = tempDir.resolve("test-warehous.dat");
		new IniController(new FileAdapter<Product>(path)).initDB(StorageConfig.createStorage());
		this.admin = new OpController<Bicycle>(new FileAdapter<Bicycle>(path));		
	}

	@Test
	void testInventory() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		list.add(new Bicycle("987565", 125, 690.00));		
		this.admin.put(list);
		assertTrue(this.admin.inventory().containsAll(list) && this.admin.inventory().size()==2);
	}

	@Test
	void testAvailability() {assertTrue(this.admin.availability()==15);}

	@Test
	void testPickOne() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		list.add(new Bicycle("987565", 125, 690.00));		
		this.admin.put(list);
		assertTrue(!this.admin.pickOne().getValue().serial.isEmpty());
	}

	@Test
	void testPickOneString() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		list.add(new Bicycle("987565", 125, 690.00));		
		this.admin.put(list);
		assertTrue(this.admin.pickOne("123456").getValue().serial.equals("123456"));
	}

	@Test
	void testPickSome() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		list.add(new Bicycle("987565", 125, 690.00));
		list.add(new Bicycle("777777", 125, 690.00));
		this.admin.put(list);
		assertTrue(this.admin.pickSome(2).size()==2);
	}

	@Test
	void testPull() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		list.add(new Bicycle("987565", 125, 690.00));
		list.add(new Bicycle("777777", 125, 690.00));
		this.admin.put(list);
		assertTrue(this.admin.inventory().size()==3);
	}

	@Test
	void testRefill() throws IOException {assertTrue(this.admin.refill(new Vendor(123)));}

}
