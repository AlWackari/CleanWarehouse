package core.ports;

import java.util.List;

import core.entities.Product;

public interface Repository<T extends Product> {
	public List<T> getMagazzino();
	public boolean dropMagazzino(List<T> prodotti);
	public boolean addMagazzino(List<T> prodotti);
	public int getMinLevel(int pid);
	public int getCapacity();
}
