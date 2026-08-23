package core.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import core.entities.Audit;
import core.entities.Product;
import core.entities.Warehouse;
import core.ports.Accountability;
import core.ports.Operations;
import core.ports.Repository;

public class OpController<T extends Product> implements Operations<T> {
	
	private final Repository<T> db;
	
	public OpController(Repository<T> db) throws IllegalStateException{
		if(db.getCapacity()<1) throw new IllegalStateException("No storage bin found"); this.db = db;}
	
	@Override
	public List<T> inventory() {return new Warehouse<T>(10, this.db.getStorage()).inventory();}

	@Override
	public int availability() {return new Warehouse<T>(10, this.db.getStorage()).remainingCapacity();}
	
	@Override
	public Entry<String, T> pickOne() throws NoSuchElementException {
		Warehouse<T> w =  new Warehouse<T>(10, this.db.getStorage());
		w.beginTS(); return this.finalizeOneDel(w.pick(), w);
	}

	@Override
	public Entry<String, T> pickOne(String serial) throws NoSuchElementException{
		Warehouse<T> w =  new Warehouse<T>(10, this.db.getStorage());
		w.beginTS(); return this.finalizeOneDel(w.pick(serial), w);
	}
	
	@Override
	public Map<String, List<T>> pickSome(int n) throws IndexOutOfBoundsException {
		Warehouse<T> w =  new Warehouse<T>(10, this.db.getStorage());
		w.beginTS(); return this.finalizeDel(w.pick(n), w);
	}
	
	@Override
	public Map<String, List<T>> put(List<T> p) {
		Warehouse<T> w =  new Warehouse<T>(10, this.db.getStorage());
		w.beginTS(); return this.finalizeAdd(w.put(p), w);
	}

	@Override
	public boolean refill(Accountability<T> v) throws IOException{
		Warehouse<T> w =  new Warehouse<T>(10, this.db.getStorage());
		if(w.check()) return false;
		List<T> list = v.makeTo(10-w.inventory().size());
		w.beginTS(); if(this.finalizeAdd(w.put(list), w).isEmpty()) throw new IOException();
		return true;
	}

	@Override
	public Audit getAudit(List<String> bins) {return this.db.getAudit(bins);}
	
	private Entry<String, T> finalizeOneDel(Entry<String, T> prod, Warehouse<T> w) {
		List<T> list = new ArrayList<T>(); list.add(prod.getValue());
		Map<String, List<T>> map = new HashMap<String, List<T>>(); map.put(prod.getKey(), list);
		map = this.finalizeDel(map, w);
		return Map.entry(map.keySet().iterator().next(), map.get(prod.getKey()).get(0));
	}
	
	private Map<String, List<T>> finalizeAdd(Map<String, List<T>> map, Warehouse<T> w){return this.finalize(map, w, this.db::addProducts);}
	
	private Map<String, List<T>> finalizeDel(Map<String, List<T>> map, Warehouse<T> w){return this.finalize(map, w, this.db::removeProducts);}
	
	private Map<String, List<T>> finalize(Map<String, List<T>> map, Warehouse<T> w, Function<Map<String, List<T>>, Boolean> dbAction){
		if(!dbAction.apply(map)) {w.rollback(); map.clear();} else try {w.commit();} catch (TimeoutException e) {map.clear();} return map;
	}
}
