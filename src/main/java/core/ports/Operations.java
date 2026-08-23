package core.ports;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import core.entities.Audit;
import core.entities.Product;

public interface Operations<T extends Product> {
	public Map<String, List<T>> pickSome(int n) throws IndexOutOfBoundsException;
	public Entry<String, T> pickOne(String serial) throws NoSuchElementException;
	public Entry<String, T> pickOne() throws NoSuchElementException;
	public Map<String, List<T>> put(List<T> p) throws IOException;
	public List<T> inventory();
	public boolean refill(Accountability<T> vendor) throws IOException;
	public Audit getAudit(List<String> bins);
	public int availability();
}
