package core.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Warehouse<T extends Product> {
		
	private List<T> products = new ArrayList<T>(), p_snapshot;
	private final Map<String, Storage.Node> storage;
	private Map<String, Storage.Node> s_snapshot;
	public final int minLevel, size;
	private ScheduledFuture<?> timeout;
	
	private Warehouse(int min, Map<String, Storage.Node> storage) {
		this.minLevel = min;
		this.storage = storage;
		AtomicInteger size = new AtomicInteger(0);
		storage.forEach((path, bin) -> {
			size.addAndGet(bin.size());
			@SuppressWarnings("unchecked")
			List<T> items = bin.inventory().stream()
								.map(item->(T) item)
								.toList();
			this.products.addAll(items);
		});
		this.size = size.get();
	}
	
	public Warehouse(int min, Storage storage) {this(min, storage.getBins());}
	
	public Warehouse(int min, List<Storage.StatefulBin<T>> bins) {this(min, Storage.factory(bins));}
	
	public List<T> inventory(){return new ArrayList<T>(this.products);}
	
	public void beginTS(int timeout, TimeUnit unit) {
		this.p_snapshot = new ArrayList<T>(this.products);
		this.s_snapshot = new HashMap<String, Storage.Node>();
		for (Map.Entry<String, Storage.Node> entry : this.storage.entrySet())
			{this.s_snapshot.put(entry.getKey(), entry.getValue().clone());}
		this.timeout = Executors.newSingleThreadScheduledExecutor().schedule(()->{this.rollback();}, timeout, unit);
	}
	
	public void beginTS() {this.beginTS(30, TimeUnit.SECONDS);}
	
	public void commit() throws TimeoutException{
		if(this.timeout.isDone()) throw new TimeoutException();
		this.release(); this.timeout.cancel(false);}
	
	public void rollback() {this.products=this.p_snapshot;this.storage.clear();this.storage.putAll(this.s_snapshot);this.release();}
	
	private void release() {this.p_snapshot=null; this.s_snapshot=null;}
	
	private void checkTS() throws IllegalStateException {if(this.s_snapshot==null) throw new IllegalStateException("Transaction chain only is allowed");}
	
	public synchronized Map<String, List<T>> put(List<T> p) throws IllegalStateException{
		this.checkTS();
		Map<String, List<T>> map  = new HashMap<String, List<T>>();
		p.iterator().forEachRemaining(prod->{
			try {String bin = this.put(prod);
				 map.computeIfAbsent(bin, k->new ArrayList<T>()).add(prod);}
			catch (IllegalStateException e) {map.put(e.getMessage(), null); return;}
			catch (IllegalArgumentException e1) {map.computeIfAbsent("_DUPLICATES", k->new ArrayList<T>()).add(prod);} }); 
		return map;
	}
	
	private String put(T b) throws IllegalStateException{
		if(this.products.contains(b)) throw new IllegalArgumentException(b.serial+" is already in the store");
		if(this.remainingCapacity()<1) throw new IllegalStateException("Warehouse is full");
		String key = this.loadBin(b);
		this.products.add(b); 
		return key;
	}
	
	public synchronized Map<String, List<T>> pick(int quantity) throws IndexOutOfBoundsException, IllegalStateException{
		if(quantity>this.products.size()) throw new IndexOutOfBoundsException("Quantity available is "+products.size());
		Map<String, List<T>> result = new HashMap<>();
		try {for (int j = 0; j < quantity; j++) { Entry<String, T> i = this.pick();
												  result.computeIfAbsent(i.getKey(), k -> new ArrayList<>()).add(i.getValue());}}
		catch(NoSuchElementException e) {}
		return result;
	}

	public Entry<String, T> pick() throws NoSuchElementException, IllegalStateException {this.checkTS(); String s=null; return this.pick(s);}
	
	public synchronized List<T> pick(List<String> serials) throws IllegalStateException{
		List<T> prod = new ArrayList<T>();
		serials.iterator().forEachRemaining(item->{try{prod.add(this.pick(item).getValue());}catch(IllegalStateException e){};});
		return prod;
	}
	
	public synchronized Entry<String, T> pick(String serial) throws NoSuchElementException, IllegalStateException {
		this.checkTS();
		Entry<String, T> i = this.unloadBin(serial);
		this.products.remove(i.getValue());
		return i;
	}
	
	public boolean check() {if(this.products.size()<this.minLevel) return false; return true;}

	public int remainingCapacity() {return this.size-this.products.size();}
	
	private String loadBin(Product o) throws IllegalStateException{
		Iterator<Entry<String, Storage.Node>> entries = this.storage.entrySet().iterator();
		while(entries.hasNext()) {
			Entry<String, Storage.Node> e = entries.next();
			try {e.getValue().load(o); return e.getKey();}
			catch (IllegalStateException e1) {}
		} throw new IllegalStateException("Warehouse is full");
	}

	@SuppressWarnings("unchecked")
	private Entry<String, T> unloadBin(String serial) throws NoSuchElementException{
		Iterator<Entry<String, Storage.Node>> entries = this.storage.entrySet().iterator();
		while(entries.hasNext()) {
			Entry<String, Storage.Node> e = entries.next();
			if(e.getValue().inventory().isEmpty()) continue;
			else if(serial!=null) try{return Map.entry(e.getKey(), (T) e.getValue().unload(serial));} catch (NoSuchElementException e2) {}
			else try {return Map.entry(e.getKey(), (T) e.getValue().unload());} catch (InterruptedException e1) {}
		} throw new NoSuchElementException("Bin empty");
	}
}
