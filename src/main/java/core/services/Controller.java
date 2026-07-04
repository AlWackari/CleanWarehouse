package core.services;

import java.util.List;
import java.util.Vector;

import javax.management.RuntimeErrorException;

import core.entities.Bicycle;
import core.entities.Vendor;
import core.entities.Warehouse;
import core.ports.Operations;
import core.ports.Repository;

public class Controller implements Operations {
	
	private final Repository db;
	
	public Controller(Repository db) {this.db = db;}

	@Override
	public List<Bicycle> pickSome(int n) throws IndexOutOfBoundsException {
		List<Bicycle> list =  new Warehouse(this.db.getMagazzino(), Bicycle.pid, this.db.getMinLevel(Bicycle.pid), this.db.getCapacity())
								.unloadProducts(n);
		if(this.db.dropMagazzino(list)) return list;
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean push(Bicycle b) {
		List<Bicycle> list = new Vector<Bicycle>();
		list.add(b);
		return this.load(list).contains(b);
	}
	
	@Override
	public List<Bicycle> load(List<Bicycle> l) {
		List<Bicycle> loaded = new Warehouse(this.db.getMagazzino(), Bicycle.pid, this.db.getMinLevel(Bicycle.pid), this.db.getCapacity())
				.loadProducts(l);
		this.db.addMagazzino(loaded);
		return loaded;
	}

	@Override
	public boolean refill(Vendor v) throws RuntimeErrorException {
		Warehouse m = new Warehouse(this.db.getMagazzino(), Bicycle.pid, this.db.getMinLevel(Bicycle.pid), this.db.getCapacity());
		if(!m.check()) return this.db.addMagazzino(m.loadProducts(v.makeOrder(m.getSlots())));
		return false;
	}

	@Override
	public List<Bicycle> list() {return this.db.getMagazzino();}

	@Override
	public int getSlots() {
		return new Warehouse(this.db.getMagazzino(), Bicycle.pid, this.db.getMinLevel(Bicycle.pid), this.db.getCapacity())
				.getSlots();
	}

	@Override
	public Bicycle pickOne(String serial) throws ClassNotFoundException{
		Bicycle b =  new Warehouse(this.db.getMagazzino(), Bicycle.pid, this.db.getMinLevel(Bicycle.pid), this.db.getCapacity())
						.unloadOne(serial);
		List<Bicycle> list = new Vector<Bicycle>(); list.add(b);
		if(this.db.dropMagazzino(list)) return b;
		throw new ClassNotFoundException();
	}
}
