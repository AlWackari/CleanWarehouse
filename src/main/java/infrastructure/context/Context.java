package infrastructure.context;

import java.util.HashMap;

public abstract class Context {
	
	public static enum keys{USER, IP, TOKEN}
		
	private final static ThreadLocal<HashMap<String, Object>> memory = ThreadLocal.withInitial(HashMap::new);

	public static final void put(Context.keys key, Object value) {memory.get().put(key.name(), value);}
	
	public static final Object get(Context.keys key){return memory.get().get(key.name());}
	
	public static final void clear() {memory.remove();}
	
}
