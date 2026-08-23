package core.entities;

import java.io.Serializable;
import java.util.Objects;

public abstract class Product implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public final int pid;
	public final String desc, serial;
	public final double price;

	protected Product(int pid, String desc, String serial, double price) {
		this.pid = pid;
		this.desc = desc;
		this.serial = serial;
		this.price = price;
	}
	
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Bicycle other = (Bicycle) o;
	    return this.serial.equals(other.serial);
	}
	
	public int hashCode() {return Objects.hash(this.serial);}
}
