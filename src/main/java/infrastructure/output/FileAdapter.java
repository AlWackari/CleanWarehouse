package infrastructure.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.ArrayList;
import java.util.List;

import core.entities.Audit;
import core.entities.Product;
import core.entities.Storage;
import core.entities.Storage.StatefulBin;
import core.ports.Layout;
import core.ports.Repository;

public class FileAdapter<T extends Product> implements Repository<T>, Layout<T>{
	
	private final File store, audit;
	private final Object lock = new Object();
	
	public FileAdapter(Path p) throws IOException{
		if(!Files.isWritable(p.toAbsolutePath().normalize().getParent())) throw new IOException("Can't write here");
		this.store = new File(p.toUri());
		audit = new File(this.store.getAbsolutePath()+".auditlog");
	}

	@Override
	public List<Storage.StatefulBin<T>> getStorage() {return this.init();}

	@Override
	public boolean addProducts(Map<String, List<T>> prodotti) {return this.updateProducts(prodotti, List::addAll, Audit.factory(prodotti, Audit.Posting.CREDIT));}

	@Override
	public boolean removeProducts(Map<String, List<T>> prodotti) {return this.updateProducts(prodotti, List::removeAll, Audit.factory(prodotti, Audit.Posting.DEBIT));}
	
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
		return this.trywrite(mine, Audit.factory(bins, Audit.Posting.CREDIT));
	}

	@Override
	public boolean delBins(List<StatefulBin<T>> bins) {
		List<Storage.StatefulBin<T>> mine = this.init();
		bins.iterator().forEachRemaining(item->this.searchBin(item.path, mine).ifPresent(bin->mine.remove(bin)));
		return this.trywrite(mine, Audit.factory(bins, Audit.Posting.DEBIT));
	}
	
	@Override
	public Audit getAudit(List<String> bins) {
		try {return Audit.factory(this.audit, bins);} 
		catch (IndexOutOfBoundsException | NullPointerException | IOException e) {return null;}
	}
	
	private boolean updateProducts(Map<String, List<T>> prodotti, BiConsumer<List<T>, List<T>> function, Audit record) {
		List<Storage.StatefulBin<T>> store = this.init();
		prodotti.entrySet().iterator().forEachRemaining(item->this.searchBin(item.getKey(), store)
														.ifPresent(bin->function.accept(bin.products, item.getValue())));
		return this.trywrite(store, record);
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
	
	private boolean trywrite(List<Storage.StatefulBin<T>> list, Audit record) 
		{try {this.write(list, record); return true;} catch (IOException e) {return false;}}
	
	private void write(List<Storage.StatefulBin<T>> list, Audit record) throws IOException {
		synchronized (this.lock) {try(ObjectOutputStream opera = new ObjectOutputStream(new FileOutputStream(this.store)))
									 {opera.writeObject(list); 
									 Files.writeString(this.audit.toPath(), record.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
									 }}
	}
}
