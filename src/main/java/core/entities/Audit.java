package core.entities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import core.entities.Storage.StatefulBin;
import infrastructure.context.Context;
import infrastructure.context.Context.keys;

public class Audit {

	public static class Record{
		public final String user, time, bin, serials[];
		public final int quantity, pid;
		protected Record(int pid, String bin, String[] serials, Posting key) {
			this(Context.get(keys.USER), Instant.now().toString(), pid, bin, serials, key);
		}
		private Record(Object var, String time, int pid, String bin, String[] serials, Posting key) {
			this.user=var==null?"anonymous":var.toString();
			this.time = time;
			this.bin = bin;
			this.serials = serials;
			this.quantity = serials.length*key.multi;
			this.pid = pid;
		}
		public String toString() {
			return String.join(";", this.time, this.user, this.bin, String.valueOf(this.quantity), String.valueOf(this.pid), Arrays.toString(this.serials));
		}
	}
	public enum Posting{CREDIT(1), DEBIT(-1); final int multi; private Posting(int i) {this.multi=i;}
		public static Posting valueOf(int arg) {if(arg>0) return Posting.CREDIT; return Posting.DEBIT;}
	}
	private List<Record> records = new ArrayList<Audit.Record>();
	
	public static <T extends Product> Audit factory(Map<String, List<T>> map, Posting key) {
		List<SimpleEntry<String, String[]>> list = 
				map.entrySet().stream().map(item->new SimpleEntry<String, String[]>(
																item.getKey(), 
					  											item.getValue().stream().map(prod->{Product p = (Product) prod;
					  																				return p.serial;})
					  																	.toArray(String[]::new)))
									   .toList();
		
		return new Audit(list, map.entrySet().iterator().next().getValue().getFirst().pid, key);
	}
	
	public static <T extends Product> Audit factory(List<StatefulBin<T>> bins, Posting key) {
		List<SimpleEntry<String, String[]>> list = new ArrayList<SimpleEntry<String,String[]>>();
		bins.iterator().forEachRemaining(item->list.add(
				new SimpleEntry<String, String[]>(item.path,
												  item.products.stream().map(prod->{Product p = (Product) prod;
													return p.serial;}).toArray(String[]::new))));
		return new Audit(list, 0, key);
	}
	
	public static Audit factory(File file, List<String> bins) throws IndexOutOfBoundsException, NullPointerException, IOException{
		List<Record> reco = new ArrayList<Audit.Record>();
		try(Stream<String> lines = Files.lines(file.toPath())){
			lines.filter(item->{String col[] = item.split(";");
								if((!bins.contains(col[2]) & !bins.contains(col[3])) | Integer.valueOf(col[3])==0) return false; return true;})
				 .forEach(item->{String col[] = item.split(";");
				 				 String serial[] = col[5].equals("[]")? new String[0] : col[5].substring(1, col[5].length()-1).split(",");
				 				 reco.add(new Record(col[1], col[0], Integer.valueOf(col[4]), col[2], serial, 
				 						 			 Posting.valueOf(Integer.valueOf(col[3]))));});}
		return new Audit(reco);
	}
	
	private Audit(List<SimpleEntry<String, String[]>> what, int pid, Posting key) {
		what.iterator().forEachRemaining(item->{this.records.add(new Record(pid, item.getKey(),item.getValue(), key));});
	}
	
	private Audit(List<Record> lines) {this.records=lines;}
	
	public List<Record> getRecords(){return new ArrayList<Audit.Record>(this.records);}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		this.records.iterator().forEachRemaining(item->sb.append(item.toString()+System.lineSeparator()));
		return sb.toString();
	}
}
