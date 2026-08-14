package core.ports;

import java.util.List;

import core.entities.Product;
import core.entities.Storage;
import core.entities.Storage.StatefulBin;

public interface Setup {
	public boolean initDB(Storage s); 
	public boolean initDB(List<StatefulBin<Product>> s);
}