package br.com.mymacro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteJDBCDriverConnection {
	private Connection connection;

	public void connect() {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:macro.db");
			Statement statement = connection.createStatement();
			statement.execute("CREATE TABLE IF NOT EXISTS MACRO( ORDEM INTEGER , MACRO VARCHAR NOT NULL, SYNTAX VARCHAR NOT NULL)");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public ResultSet  select(String sql) throws SQLException {
		this.connect();
		PreparedStatement stmt = connection.prepareStatement(sql);
		ResultSet set = stmt.executeQuery();
		return set;
	}
	
	public void  update(  Integer id, String macro, String syntax) throws SQLException {
		this.connect();
		PreparedStatement stmt = connection.prepareStatement("UPDATE MACRO SET MACRO= ?, ORDEM=?, SYNTAX=? WHERE ORDEM=?");
		
		stmt.setString(1, macro);
		stmt.setInt(2, id);
		stmt.setString(3, syntax);
		stmt.setInt(4, id);
		
		stmt.executeUpdate();
		connection.close();
	}
	
	public void  delete(Integer id) throws SQLException {
		this.connect();
		PreparedStatement stmt = connection.prepareStatement("DELETE FROM MACRO WHERE ORDEM=?");
		stmt.setInt(1, id);
		stmt.executeUpdate();
		connection.close();
	}
	
	public void  insert(String macro, Integer ordem, String syntax) throws SQLException {
		this.connect();
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO MACRO (MACRO, ORDEM, SYNTAX) VALUES (?, ?, ?)");
		
		stmt.setString(1, macro);
		stmt.setInt(2, ordem);
		stmt.setString(3, syntax);
		
		stmt.executeUpdate();
		connection.close();
	}
	
	
}
