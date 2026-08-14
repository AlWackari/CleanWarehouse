package infrastructure.input;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import core.entities.Bicycle;
import core.entities.Vendor;
import core.services.IniController;
import core.services.OpController;

abstract class BicycleUIAdapter {
	
	protected OpController<Bicycle> service;
	protected final IniController adm;
	
	protected BicycleUIAdapter(OpController<Bicycle> c, IniController adm) {this.service=c; this.adm=adm;}
		
	public Map<String, List<Bicycle>> pick(int n) throws IndexOutOfBoundsException{return this.service.pickSome(n);}
	
	public Map<String, List<Bicycle>> load(List<Bicycle> list) {return this.service.put(list);}
	
	public void refill(Vendor v) throws IOException{this.service.refill(v);}
}
