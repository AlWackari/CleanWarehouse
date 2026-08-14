package core.entities;

public class Bicycle extends Product {
	
	private static final long serialVersionUID = 1L;
	public static final int pid = 1;
	
	public final int rgb;

	public Bicycle(String serial, int rgb, double price) {
		super(Bicycle.pid, "Bicicletta da corsa", serial, price);
		this.rgb=rgb;
	}
}
