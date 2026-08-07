package core.entities;

import java.io.Serializable;

public abstract class Product implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static int pid;
	public final String desc;
	public final String serial;

	protected Product(int pid, String desc, String serial) {
		Product.pid = pid;
		this.desc = desc;
		this.serial = serial;
	}
}
