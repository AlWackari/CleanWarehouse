package infrastructure.output;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import core.entities.Bicycle;
import core.ports.Repository;

public abstract class DBAdapter implements Repository{
	
	private Connection db;
	
	protected DBAdapter(Connection conn) {
		this.db=conn;
	}
	
	public boolean setMagazzino(List<Bicycle> prodotti) {
		String sql = "";
		//TODO build sql
		try {this.sendSQL(sql); return true;} 
		catch (SQLException e) {return false;}
	}
	
	public int getMinLevel(int pid) {
		String sql = "";
		//TODO build sql
		try {return this.sendSQL(sql).getInt(1);}
		catch (SQLException e) {return 0;}
	}
	
	public List<Bicycle> gettMagazzino(){
		List<Bicycle> list = new Vector<Bicycle>();
		String sql = "";
		try {
			Bicycle b;
			ResultSet rs = this.sendSQL(sql);
			while(rs.next()) {
				b = new Bicycle(rs.getString(0), rs.getInt(1), rs.getInt(2));
				list.add(b);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
		
	}
	
	private ResultSet sendSQL(String sql_str) throws SQLException{
		Statement sql = this.db.createStatement();
		sql.execute(sql_str);
		this.db.commit();
		return sql.getResultSet();
	}
}
