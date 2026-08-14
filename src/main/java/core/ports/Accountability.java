package core.ports;

import java.util.List;

import core.entities.Product;

public interface Accountability<T extends Product> {
	public List<T> makeTo(int quantity);
}
