package core.ports;

import java.util.List;
import javax.management.RuntimeErrorException;

import core.entities.Bicycle;
import core.entities.Vendor;

public interface Operations {
	public List<Bicycle> pickSome(int n) throws IndexOutOfBoundsException;
	public Bicycle pickOne(String serial) throws ClassNotFoundException;
	public boolean push(Bicycle b);
	public List<Bicycle> load(List<Bicycle> l);
	public List<Bicycle> list();
	public boolean refill(Vendor v) throws RuntimeErrorException;
	public int getSlots();
}
