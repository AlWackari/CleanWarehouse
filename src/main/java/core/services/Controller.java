package core.services;

import java.util.List;
import java.util.Vector;

import javax.management.RuntimeErrorException;

import core.entities.Product;
import core.entities.Warehouse;
import core.ports.Accountable;
import core.ports.Operations;
import core.ports.Repository;

public class Controller<T extends Product> implements Operations<T> {
	
	private final Repository<T> db;
	
	public Controller(Repository<T> db) {this.db = db;}

	@Override
	public List<T> pickSome(int n) throws IndexOutOfBoundsException {
		List<T> list =  new Warehouse<T>(this.db.getMagazzino(), T.pid, this.db.getMinLevel(T.pid), this.db.getCapacity())
								.unloadProducts(n);
		if(this.db.dropMagazzino(list)) return list;
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean push(T b) {
		List<T> list = new Vector<T>();
		list.add(b);
		return this.load(list).contains(b);
	}
	
	@Override
	public List<T> load(List<T> l) {
		List<T> loaded = new Warehouse<T>(this.db.getMagazzino(), T.pid, this.db.getMinLevel(T.pid), this.db.getCapacity())
				.loadProducts(l);
		this.db.addMagazzino(loaded);
		return loaded;
	}

	@Override
	public boolean refill(Accountable<T> v) throws RuntimeErrorException {
		Warehouse<T> m = new Warehouse<T>(this.db.getMagazzino(), T.pid, this.db.getMinLevel(T.pid), this.db.getCapacity());
		if(!m.check()) return this.db.addMagazzino(m.loadProducts(v.makeTo(m.getSlots())));
		return false;
	}

	@Override
	public List<T> list() {return this.db.getMagazzino();}

	@Override
	public int getSlots() {
		return new Warehouse<T>(this.db.getMagazzino(), T.pid, this.db.getMinLevel(T.pid), this.db.getCapacity())
				.getSlots();
	}

	@Override
	public T pickOne(String serial) throws ClassNotFoundException{
		T b =  new Warehouse<T>(this.db.getMagazzino(), T.pid, this.db.getMinLevel(T.pid), this.db.getCapacity())
						.unloadOne(serial);
		List<T> list = new Vector<T>(); list.add(b);
		if(this.db.dropMagazzino(list)) return b;
		throw new ClassNotFoundException();
	}
}
