package unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Vector;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.entities.Bicycle;
import core.entities.Warehouse;

class TestWarehouse {
	
	private Warehouse ware;
	
	@BeforeEach
	void init() {this.ware = new Warehouse(new Vector<Bicycle>(), 1, 10, 10);}
	
	@Test
	void testLoadProduct() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		Bicycle b = new Bicycle("123456", 0, 590.00); 
		list.add(b);
		List<Bicycle> result = this.ware.loadProducts(list);
		assertTrue(result.contains(b));
	}

	@Test
	void testUnloadProduct() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		Bicycle b = new Bicycle("123456", 0, 590.00); 
		list.add(b);
		this.ware.loadProducts(list);
		List<Bicycle> result = this.ware.unloadProducts(1);
		assertTrue(result.contains(b));
	}
	
	@Test
	void testUnloadOverLimit() {
		Vector<Bicycle> list = new Vector<Bicycle>();
		Bicycle b = new Bicycle("123456", 0, 590.00); 
		list.add(b); list.add(b);
		this.ware.loadProducts(list);
		assertThrows(IndexOutOfBoundsException.class, () -> this.ware.unloadProducts(3));
	}
	
	@Test
	void testCheck() {assertTrue(!this.ware.check());}
}
