package core.entities;

import java.sql.Time;
import java.time.Instant;
import java.util.List;
import java.util.Vector;

import javax.management.RuntimeErrorException;

public class Vendor {
	
	final int id;
	
	public Vendor(int id) {this.id=id;}
	
	public List<Bicycle> makeOrder(int quantity) throws RuntimeErrorException{
		this.connect();
		List<Bicycle> list = new Vector<Bicycle>();
		for (int i = 0; i < quantity; i++) {list.add(new Bicycle(String.valueOf(Time.from(Instant.now()).getTime()), 0, 1000));} 
		return list;
	}

	//TODO
	private void connect() throws RuntimeErrorException{}
}
