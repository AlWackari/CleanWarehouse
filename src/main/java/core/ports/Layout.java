package core.ports;

import java.util.List;

import core.entities.Product;
import core.entities.Storage;

public interface Layout<T extends Product> {
	public boolean addBins(List<Storage.StatefulBin<T>> bins);
	public boolean delBins(List<Storage.StatefulBin<T>> bins);
	public List<Storage.StatefulBin<T>> getStorage();
}
