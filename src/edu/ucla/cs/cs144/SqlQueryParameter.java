package edu.ucla.cs.cs144;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqlQueryParameter {
	public String parameter;
	public String type;

	// Type can be: timestamp, string, float
	public SqlQueryParameter(String parameter, String type)
	{
		this.parameter = parameter;
		this.type = type;
	}

	public void setParameter(int location, PreparedStatement stmt) throws SQLException
	{
		if(this.type.equals("string"))
		{
			stmt.setString(location, this.parameter);
		}
	}
}