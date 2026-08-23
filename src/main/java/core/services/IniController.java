package core.services;

import java.util.List;

import javax.management.InstanceAlreadyExistsException;

import core.entities.Product;
import core.entities.Storage;
import core.entities.Storage.StatefulBin;
import core.entities.Warehouse;
import core.ports.Layout;
import core.ports.Setup;

public class IniController implements Setup {
	
	private final Layout<Product> db;
	
	public IniController(Layout<Product> layout) {this.db = layout;}
	
	@Override
	public boolean initDB(Storage s) throws InstanceAlreadyExistsException {return this.initDB(s.getStatefulBin());}

	@Override
	public boolean initDB(List<StatefulBin<Product>> s) throws InstanceAlreadyExistsException{
		if(!this.db.getStorage().isEmpty()) throw new InstanceAlreadyExistsException();
		return this.db.addBins(s);
	}

	@Override
	public boolean addBin(StatefulBin<Product> bin) throws InstanceAlreadyExistsException {
		if(new Warehouse<Product>(10, this.db.getStorage()).containsBin(bin)) throw new InstanceAlreadyExistsException();
		return this.db.addBins(List.of(bin));
	}
}