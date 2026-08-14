package unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import config.StorageConfig;
import core.entities.Bicycle;
import core.entities.Warehouse;

class TestWarehouse {
	
	private Warehouse<Bicycle> ware;
	
	@BeforeEach
	void init() {this.ware = new Warehouse<Bicycle>(10, StorageConfig.createStorage());}
	
	@Test
	void testCreation() {assertTrue(this.ware.remainingCapacity()==15);}
	
	@Test 
	void testAtomicOperation(){
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		assertThrows(IllegalStateException.class, () -> this.ware.put(list));
	}
	
	@Test
	void TestAutomaticRollback() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("987565", 125, 690.00));
		this.ware.beginTS();
		this.ware.put(list);
		try {Thread.sleep(31000);} catch (InterruptedException e) {}
		assertAll(()->assertThrows(TimeoutException.class, () -> this.ware.commit()), ()-> assertTrue(this.ware.inventory().size()<1));		
	}
	
	@Test
	void testRollback() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("987565", 125, 690.00));
		this.ware.beginTS();
		this.ware.put(list);
		this.ware.rollback();
		assertTrue(this.ware.inventory().isEmpty());
	}
	
	@Test
	void testLoadProducts() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		list.add(new Bicycle("987565", 125, 690.00));
		this.ware.beginTS();
		assertTrue(this.ware.put(list).size()==2);
	}

	@Test
	void testUnloadProduct() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		Bicycle b = new Bicycle("123456", 0, 590.00);
		list.add(b);
		this.ware.beginTS(); this.ware.put(list);
		assertTrue(this.ware.pick(b.serial).getValue().equals(b));
	}

	@Test
	void testUnloadProducts() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00));
		list.add(new Bicycle("987565", 125, 690.00));
		this.ware.beginTS(); this.ware.put(list);
		assertEquals(2, this.ware.pick(2).size());
	}

	@Test
	void testUnloadOverLimit() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		list.add(new Bicycle("123456", 0, 590.00)); list.add(new Bicycle("554422", 0, 590.00));
		this.ware.beginTS(); this.ware.put(list);
		assertThrows(IndexOutOfBoundsException.class, () -> this.ware.pick(3));
	}
	
	@Test
	void testDuplicateProduct() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		Bicycle b = new Bicycle("123456", 0, 590.00);
		list.add(b); list.add(b); list.add(new Bicycle("4567897", 0, 590.00));
		this.ware.beginTS();
		Map<String, List<Bicycle>> map = this.ware.put(list);
		assertTrue(map.containsKey("_DUPLICATES") && map.size()==3);
	}
	
	@Test
	void testCheck() {assertFalse(this.ware.check());}
}
