package core.entities;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Warehouse<T extends Product> {
	
	private List<T> products;
	public final int minLevel, pid, capacity;
	
	public Warehouse(List<T> prod, int pid, int min, int capacity) {
		this.products = prod;
		this.pid = pid;
		this.minLevel = min;
		this.capacity = capacity;
	}

	public List<T> loadProducts(List<T> p) {
		List<T> list  = new Vector<T>();
		for (Iterator<T> iterator = p.iterator(); iterator.hasNext();){
			try {
				T ok = iterator.next();
				loadProduct(ok); list.add(ok);
			}catch (IllegalArgumentException | IllegalStateException e) {}
		} return list;
	}
	
	private void loadProduct(T b) throws IllegalStateException, IllegalArgumentException{
		if(this.products.contains(b)) throw new IllegalArgumentException("T "+b.serial+" is already in the store");
		if(this.products.size()==this.capacity) throw new IllegalStateException("Warehouse is full");
		this.products.add(b);
	}
	
	public List<T> unloadProducts(int quantity) throws IndexOutOfBoundsException{
		if(quantity>this.products.size()) throw new IndexOutOfBoundsException("Quantity available is "+products.size());
		List<T> list = new Vector<T>();
		for (int i = 0; i < quantity; i++) {
			list.add(this.products.remove(0));
		} return list;
	}
	
	public T unloadOne(String serial) throws ClassNotFoundException{
		for (Iterator<T> iterator = products.iterator(); iterator.hasNext();) {
			T bycicle = iterator.next();
			if (bycicle.serial.equals(serial)) {iterator.remove(); return bycicle;}
		} throw new ClassNotFoundException();
	}
	
	public boolean check() {
		if(this.products.size()<this.minLevel) return false;
		return true;
	}
	
	public int getSlots() {return this.capacity-this.products.size();}
}
