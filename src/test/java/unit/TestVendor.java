package unit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import core.entities.Vendor;

class TestVendor {

	@Test
	void testOrdina() {
		Vendor v = new Vendor(1);
		assertEquals(2, v.makeOrder(2).size());
	}

}
