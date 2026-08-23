package core.ports;

import java.util.List;

import javax.management.InstanceAlreadyExistsException;

import core.entities.Product;
import core.entities.Storage;
import core.entities.Storage.StatefulBin;

public interface Setup {
	public boolean initDB(Storage s) throws InstanceAlreadyExistsException; 
	public boolean initDB(List<StatefulBin<Product>> s)throws InstanceAlreadyExistsException;
	public boolean addBin(StatefulBin<Product> bin) throws InstanceAlreadyExistsException;
}