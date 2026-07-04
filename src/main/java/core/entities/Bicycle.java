package core.entities;

import java.util.Objects;

public class Bicycle extends Product {
	
	private static final long serialVersionUID = 1L;
	public static final int pid = 1;
	public final String serial;
	public final int rgb;
	public final double price;

	public Bicycle(String serial, int rgb, double price) {
		super(Bicycle.pid, "Bicicletta da corsa");
		this.serial=serial; this.rgb=rgb; this.price=price;
	}
	
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Bicycle other = (Bicycle) o;
	    return this.serial.equals(other.serial);
	}
	
	public int hashCode() {return Objects.hash(this.serial);}
}
