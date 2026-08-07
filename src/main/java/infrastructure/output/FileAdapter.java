package infrastructure.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Vector;


import core.entities.Product;
import core.ports.Repository;

public class FileAdapter<T extends Product> implements Repository<T> {
	
	private final File store;
	private final Object lock = new Object();
	
	public FileAdapter(Path p) {store = new File(p.toUri());}

	@Override
	public List<T> getMagazzino() {return this.init();}

	@Override
	public boolean addMagazzino(List<T> prodotti) {
		List<T> list = this.init();
		list.addAll(prodotti);
		try {this.write(list); return true;}
		catch (IOException e) {return false;}
	}

	@Override
	public int getMinLevel(int pid) {return 5;}
	
	@SuppressWarnings("unchecked")
	private List<T> init(){
		try {return (List<T>) this.read();}
		catch (ClassCastException e) {return new Vector<T>();}
	}
	
	private Object read(){
		Object o = new Object();
		synchronized (this.lock) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(this.store));
				o = in.readObject();
				in.close();} 
			catch (IOException | ClassNotFoundException e) {}
		} return o;
	}
	
	private void write(List<T> list) throws IOException {
		Vector<Product> data = new Vector<Product>(list);
		synchronized (this.lock) {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.store));
			out.writeObject(data);
			out.close();			
		}
	}

	@Override
	public int getCapacity() {return 10;}

	@Override
	public boolean dropMagazzino(List<T> prodotti) {
		List<T> list = this.init();
		list.removeAll(prodotti);
		try {this.write(list); return true;}
		catch (IOException e) {return false;}
	}
}
