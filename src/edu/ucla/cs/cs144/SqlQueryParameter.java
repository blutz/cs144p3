package edu.ucla.cs.cs144;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

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
		else if (this.type.equals("float"))
		{
			stmt.setFloat(location, Float.parseFloat(this.parameter));
		}
		else if (this.type.equals("timestamp"))
		{
			SimpleDateFormat format = new SimpleDateFormat("MMM-dd-yy HH:mm:ss");
	        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        // Set this.started
        try {
	            Date parsed = format.parse(this.parameter);
	            stmt.setString(location, outputFormat.format(parsed));
        }
        catch(ParseException pe) {
	            System.err.println("Cannot parse \"" + this.parameter + "\"");
	            stmt.setString(location, "0000-00-00 00:00:00");
        }
		}
	}
}