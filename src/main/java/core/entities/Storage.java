package core.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Storage{
	
	public static class Node{
		public static final String separator = "/";
		private final List<Node> children;
		private final String name;
		private final ArrayBlockingQueue<Product> products;
		public Node(String name, int maxCapacity) {this(name, new ArrayBlockingQueue<Product>(maxCapacity), Collections.emptyList());}
		public Node(String name, List<Node> children) {this(name, null, children);}		
		private Node(String name, ArrayBlockingQueue<Product> products, List<Node> children) {
			this.children=children; this.products = products;this.name=name;
		}
		private Iterator<Node> getChildren(){return this.children.iterator();}
		private boolean hasChildren() {return !this.children.isEmpty();}
		public void load(Product product) throws IllegalStateException {this.products.add(product);}
		public boolean unload(Product p) {return this.products.remove(p);}
		public Product unload() throws InterruptedException {return this.products.take();}
		public Product unload(String serial) throws NoSuchElementException {
			return this.products.stream().filter(item->item.serial.equals(serial)).findFirst().get();
		}
		public int remainingCapacity() {return this.products.remainingCapacity();}
		public List<Product> inventory(){
			List<Product> list = new ArrayList<>();
			this.products.iterator().forEachRemaining(list::add);
			return list;
		}
		public Node clone() {
			Node n = new Node(this.name, this.size()>0?new ArrayBlockingQueue<Product>(this.size()):null , new ArrayList<Node>(this.children));
			n.products.addAll(this.products); return n;
		}
		public int size() {try{return this.products.size()+this.products.remainingCapacity();} catch(NullPointerException e){return 0;}}
		public String toString() {return this.name.toUpperCase().concat(separator);}
	}
	
	public static class StatefulBin<T extends Product> implements Serializable{
		private static final long serialVersionUID = 1L;
		public final String path;
		public final int capacity;
		public List<T> products;
		@JsonCreator
		private StatefulBin(@JsonProperty("path") String path, @JsonProperty("capacity") int capacity, @JsonProperty("products") List<T> products)
			{this.path = path; this.capacity=capacity; this.products=products;}
		public boolean equals(Object o) {if (o instanceof StatefulBin<?> other) {return this.path.equals(other.path);} return false;}
	}
	
	private final Node root;
	
	public static <T extends Product> Map<String, Storage.Node> factory(List<Storage.StatefulBin<T>> bins) {
		Map<String, Storage.Node> map = new HashMap<String, Storage.Node>();
		bins.iterator().forEachRemaining(item->{
			String name[] = item.path.split(Node.separator);			
			Node n = new Node(name[name.length-1],item.capacity);
			item.products.iterator().forEachRemaining(prod->n.load(prod));
			map.put(item.path, n);
		});
		return map;
	}
	
	public Storage(Node n) {this.root=n;}
	
	public Map<String, Node> getBins(){return this.getBins(this.root.getChildren(), this.root.toString());}
	
	@SuppressWarnings("unchecked")
	public <T extends Product> List<Storage.StatefulBin<T>> getStatefulBin(){
		return this.getBins().entrySet().stream()
				.map(bin->new StatefulBin<T>(bin.getKey(), bin.getValue().size(), bin.getValue().inventory().stream()
							.filter(p->p instanceof Bicycle).map(p->(T)p).collect(Collectors.toList()))).toList();
	}
	
	private Map<String, Node> getBins(Iterator<Node> i, String name) {
		Map<String, Node> v = new HashMap<String, Node>();
		while (i.hasNext()) {
			Storage.Node node = i.next();
			if(node.hasChildren()) v.putAll(this.getBins(node.getChildren(), name.concat(node.toString())));
			else v.put(name.concat(node.toString()), node);
		} return v;
	}
}