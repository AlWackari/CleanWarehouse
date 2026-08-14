package core.services;

import java.util.List;

import core.entities.Product;
import core.entities.Storage;
import core.entities.Storage.StatefulBin;
import core.ports.Layout;
import core.ports.Setup;

public class IniController implements Setup {
	
	private final Layout<Product> db;
	
	public IniController(Layout<Product> layout) {this.db = layout;}
	
	@Override
	public boolean initDB(Storage s) {return this.initDB(s.getStatefulBin());}

	@Override
	public boolean initDB(List<StatefulBin<Product>> s) {return this.db.addBins(s);}
}
