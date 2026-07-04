package core.ports;

import java.util.List;

import core.entities.Bicycle;

public interface Repository {
	public List<Bicycle> getMagazzino();
	public boolean dropMagazzino(List<Bicycle> prodotti);
	public boolean addMagazzino(List<Bicycle> prodotti);
	public int getMinLevel(int pid);
	public int getCapacity();
}
