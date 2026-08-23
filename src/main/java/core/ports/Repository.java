package core.ports;

import java.util.List;
import java.util.Map;

import core.entities.Audit;
import core.entities.Product;
import core.entities.Storage;

public interface Repository<T extends Product> {
	public List<Storage.StatefulBin<T>> getStorage();
	public boolean removeProducts(Map<String, List<T>> prodotti);
	public boolean addProducts(Map<String, List<T>> prodotti);
	public Audit getAudit(List<String> bins);
	public int getMinLevel(int pid);
	public int getCapacity();
}
