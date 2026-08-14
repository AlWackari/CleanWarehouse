package infrastructure.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.ArrayList;
import java.util.List;

import core.entities.Product;
import core.entities.Storage;
import core.entities.Storage.StatefulBin;
import core.ports.Layout;
import core.ports.Repository;

public class FileAdapter<T extends Product> implements Repository<T>, Layout<T>{
	
	private final File store;
	private final Object lock = new Object();
	
	public FileAdapter(Path p) {store = new File(p.toUri());}

	@Override
	public List<Storage.StatefulBin<T>> getStorage() {return this.init();}

	@Override
	public boolean addProducts(Map<String, List<T>> prodotti) {return this.updateProducts(prodotti, List::addAll);}

	@Override
	public boolean removeProducts(Map<String, List<T>> prodotti) {return this.updateProducts(prodotti, List::removeAll);}
	
	@Override
	public int getMinLevel(int pid) {return 5;}

	@Override
	public int getCapacity() {
		AtomicInteger i = new AtomicInteger();
		this.init().iterator().forEachRemaining(item->i.addAndGet(item.capacity));
		return i.get();
	}

	@Override
	public boolean addBins(List<StatefulBin<T>> bins) {
		List<Storage.StatefulBin<T>> mine = this.init();
		bins.iterator().forEachRemaining(item->mine.add(item));
		return this.trywrite(mine);
	}

	@Override
	public boolean delBins(List<StatefulBin<T>> bins) {
		List<Storage.StatefulBin<T>> mine = this.init();
		bins.iterator().forEachRemaining(item->this.searchBin(item.path, mine).ifPresent(bin->mine.remove(bin)));
		return this.trywrite(mine);
	}
	
	private boolean updateProducts(Map<String, List<T>> prodotti, BiConsumer<List<T>, List<T>> function) {
		List<Storage.StatefulBin<T>> store = this.init();
		prodotti.entrySet().iterator().forEachRemaining(item->this.searchBin(item.getKey(), store)
														.ifPresent(bin->function.accept(bin.products, item.getValue())));
		return this.trywrite(store);
	}
	
	private Optional<Storage.StatefulBin<T>> searchBin(String path, List<Storage.StatefulBin<T>> store){
		return store.stream().filter(item->item.path.equals(path)).findFirst();
	}
	
	@SuppressWarnings("unchecked")
	private List<Storage.StatefulBin<T>> init(){
		try {return (List<Storage.StatefulBin<T>>) this.read();} catch (ClassCastException e) {return new ArrayList<Storage.StatefulBin<T>>();}
	}
	
	private Object read(){
		Object o = new Object();
		synchronized (this.lock) {try( ObjectInputStream in = new ObjectInputStream(new FileInputStream(this.store))) 
									{o = in.readObject();}catch (IOException | ClassNotFoundException e) {}} 
		return o;
	}
	
	private boolean trywrite(List<Storage.StatefulBin<T>> list) {try {this.write(list); return true;} catch (IOException e) {return false;}}
	
	private void write(List<Storage.StatefulBin<T>> list) throws IOException {
		synchronized (this.lock) {try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(this.store));)
									{out.writeObject(list);}}
	}
}
