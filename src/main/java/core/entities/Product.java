package core.entities;

import java.io.Serializable;

public abstract class Product implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public final int pid;
	public final String desc;

	protected Product(int pid, String desc) {
		this.pid = pid;
		this.desc = desc;
	}
}
