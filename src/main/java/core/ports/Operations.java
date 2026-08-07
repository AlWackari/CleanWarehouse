package core.ports;

import java.util.List;
import javax.management.RuntimeErrorException;
import core.entities.Product;

public interface Operations<T extends Product> {
	public List<T> pickSome(int n) throws IndexOutOfBoundsException;
	public T pickOne(String serial) throws ClassNotFoundException;
	public boolean push(T b);
	public List<T> load(List<T> l);
	public List<T> list();
	public boolean refill(Accountable<T> vendor) throws RuntimeErrorException;
	public int getSlots();
}
