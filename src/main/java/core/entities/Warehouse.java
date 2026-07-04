package core.entities;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Warehouse {
		
	private List<Bicycle> products = new Vector<Bicycle>();
	public final int minLevel, pid, capacity;
	
	public Warehouse(List<Bicycle> prod, int pid, int min, int capacity) {
		this.products = prod;
		this.pid = pid;
		this.minLevel = min;
		this.capacity = capacity;
	}

	public List<Bicycle> loadProducts(List<Bicycle> p) {
		List<Bicycle> list  = new Vector<Bicycle>();
		for (Iterator<Bicycle> iterator = p.iterator(); iterator.hasNext();){
			try {
				Bicycle ok = iterator.next();
				loadProduct(ok); list.add(ok);
			}catch (IllegalArgumentException | IllegalStateException e) {}
		} return list;
	}
	
	private void loadProduct(Bicycle b) throws IllegalStateException, IllegalArgumentException{
		if(this.products.contains(b)) throw new IllegalArgumentException("Bicycle "+b.serial+" is already in the store");
		if(this.products.size()==this.capacity) throw new IllegalStateException("Warehouse is full");
		this.products.add(b);
	}
	
	public List<Bicycle> unloadProducts(int quantity) throws IndexOutOfBoundsException{
		if(quantity>this.products.size()) throw new IndexOutOfBoundsException("Quantity available is "+products.size());
		List<Bicycle> list = new Vector<Bicycle>();
		for (int i = 0; i < quantity; i++) {
			list.add(this.products.remove(0));
		} return list;
	}
	
	public Bicycle unloadOne(String serial) throws ClassNotFoundException{
		for (Iterator<Bicycle> iterator = products.iterator(); iterator.hasNext();) {
			Bicycle bycicle = iterator.next();
			if (bycicle.serial.equals(serial)) {iterator.remove(); return bycicle;}
		} throw new ClassNotFoundException();
	}
	
	public boolean check() {
		if(this.products.size()<this.minLevel) return false;
		return true;
	}
	
	public int getSlots() {return this.capacity-this.products.size();}
}
