package integration;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import config.StorageConfig;
import core.entities.Bicycle;
import core.entities.Storage;
import infrastructure.output.FileAdapter;

class TestFileAdapter {
	
	private FileAdapter<Bicycle> file;
	
	@TempDir
    Path tempDir;
	
	@BeforeEach
	void init() throws Exception {
		this.file = new FileAdapter<Bicycle>(tempDir.resolve("test-warehouse.dat").toFile().toPath());
		if(this.file.getCapacity()<1) this.file.addBins(StorageConfig.createStorage().<Bicycle>getStatefulBin());
	}

	@Test
	void testAddProducts() {
		Map<String, List<Bicycle>> map = new HashMap<String, List<Bicycle>>();
		List<Bicycle> list = new ArrayList<Bicycle>();
		Bicycle a = new Bicycle("123", 54, 348.99);
		Bicycle b = new Bicycle("234", 54, 448.99);
		list.add(a); list.add(b);
		map.put("ZONE#A/AREA#1/ZONE#3/SLOT#1/", new ArrayList<Bicycle>(list));
		this.file.addProducts(map);
		List<Storage.StatefulBin<Bicycle>> bins = this.file.getStorage();
		list = bins.stream()
				   .filter(item->item.path.equals("ZONE#A/AREA#1/ZONE#3/SLOT#1/") || item.path.equals("ZONE#A/AREA#1/ZONE#3/SLOT#2/"))
				   .map(item->item.products)
				   .filter(item->item.contains(a) || item.contains(b))
				   .findFirst().get();
		assertTrue(list.contains(a) && list.contains(b));
	}

	@Test
	void testRemoveProducts() {
		Map<String, List<Bicycle>> map = new HashMap<String, List<Bicycle>>();
		List<Bicycle> list = new ArrayList<Bicycle>();
		Bicycle a = new Bicycle("123", 54, 348.99);
		Bicycle b = new Bicycle("234", 54, 448.99);
		list.add(a); list.add(b);
		map.put("ZONE#A/AREA#1/ZONE#3/SLOT#1/", new ArrayList<Bicycle>(list));
		map.put("ZONE#A/AREA#1/ZONE#3/SLOT#2/", new ArrayList<Bicycle>(list));
		this.file.addProducts(map);
		this.file.removeProducts(map);
		assertEquals(0, this.file.getStorage().stream().mapToInt(item->item.products.size()).sum());
	}

	@Test
	void testGetCapacity() {assertEquals(15, this.file.getCapacity());}

	@Test
	void testAdminRemoveBin() {
		List<Storage.StatefulBin<Bicycle>> list = new Vector<Storage.StatefulBin<Bicycle>>();
		Storage.StatefulBin<Bicycle> bin = StorageConfig.createStorage().<Bicycle>getStatefulBin().getFirst();
		list.add(bin);
		this.file.delBins(list);
		assertFalse(this.file.getStorage().contains(bin));
	}
	
	@Test
	void testAudit() {
		Map<String, List<Bicycle>> map = new HashMap<String, List<Bicycle>>();
		List<Bicycle> list = new ArrayList<Bicycle>();
		Bicycle a = new Bicycle("123", 54, 348.99);
		list.add(a); list.add(new Bicycle("234", 54, 448.99));
		map.put("ZONE#A/AREA#1/ZONE#3/SLOT#1/", new ArrayList<Bicycle>(list));
		list.clear();
		list.add(new Bicycle("456", 54, 348.99)); list.add(new Bicycle("789", 54, 448.99));		
		map.put("ZONE#A/AREA#1/ZONE#3/SLOT#2/", new ArrayList<Bicycle>(list));
		this.file.addProducts(map);
		this.file.removeProducts(map);
		AtomicInteger b1 = new AtomicInteger(), b2 = new AtomicInteger();
		this.file.getAudit(map.keySet().stream().toList()).getRecords().stream().forEach(item->{
			switch (item.bin) {
				case "ZONE#A/AREA#1/ZONE#3/SLOT#1/": b1.addAndGet(item.quantity); break;
				case "ZONE#A/AREA#1/ZONE#3/SLOT#2/": b2.addAndGet(item.quantity); break;
				default: break;}});
		assertTrue(b1.get()<1 & b2.get()<1);
	}
}
