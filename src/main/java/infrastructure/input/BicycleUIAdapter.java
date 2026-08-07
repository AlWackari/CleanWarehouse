package infrastructure.input;

import java.util.List;

import javax.management.RuntimeErrorException;

import core.entities.Bicycle;
import core.entities.Vendor;
import core.services.Controller;

abstract class BicycleUIAdapter {
	
	protected final Controller<Bicycle> service;
	
	protected BicycleUIAdapter(Controller<Bicycle> c) {this.service=c;}
		
	public List<Bicycle> pick(int n) throws IndexOutOfBoundsException{return this.service.pickSome(n);}
	
	public List<Bicycle> load(List<Bicycle> list) {return this.service.load(list);}
	
	public void refill(Vendor v) throws RuntimeErrorException{this.service.refill(v);}
}
