package config;
import java.util.List;
import java.util.Vector;

import core.entities.Storage;
import core.entities.Storage.Node;

public class StorageConfig {

	static public Storage createStorage() {
		List<Storage.Node> list = new Vector<Storage.Node>();
		list.add(new Storage.Node("Slot#1", 1));
		Storage.Node zon1 = new Storage.Node("ZONE#1", new Vector<Storage.Node>(list));
		list.add(new Storage.Node("Slot#2", 2));
		Storage.Node zon2 = new Storage.Node("ZONE#2", new Vector<Storage.Node>(list));
		list.add(new Storage.Node("Slot#3", 3));
		Storage.Node zon3 = new Storage.Node("ZONE#3", new Vector<Storage.Node>(list));
		list.clear();list.add(zon1);list.add(zon2);list.add(zon3);
		Storage.Node are1 = new Storage.Node("AREA#1", new Vector<Storage.Node>(list));
		list.clear();list.add(new Storage.Node("BigBin", 5));
		Storage.Node are2 = new Storage.Node("AREA#2", new Vector<Storage.Node>(list));
		list.clear(); list.add(are1); list.add(are2);
		return new Storage(new Node("ZONE#A", new Vector<Storage.Node>(list)));
	}
	
	static public String getStorageJSON() {return "[\r\n"
			+ "  {\r\n"
			+ "    \"path\": \"MAGAZZINO/ZONA_A/BIN_01/\",\r\n"
			+ "    \"capacity\": 10,\r\n"
			+ "    \"products\": []\r\n"
			+ "  },\r\n"
			+ "  {\r\n"
			+ "    \"path\": \"MAGAZZINO/ZONA_A/BIN_02/\",\r\n"
			+ "    \"capacity\": 5,\r\n"
			+ "    \"products\": []\r\n"
			+ "  }\r\n"
			+ "]";}
}
