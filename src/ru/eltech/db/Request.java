package ru.eltech.db;
import java.io.Serializable;

public class Request implements Serializable {
	private static final long serialVersionUID = -8689733764257560174L;
	
	public int id;
	public String table;
	public String matchColumn;
	public String matchValue;
	public String requestColumn;
	
	public Request(
			int id_,
			String table_,
			String matchColumn_,
			String matchValue_,
			String requestColumn_) {
		id = id_;
		table = table_;
		matchColumn = matchColumn_;
		matchValue = matchValue_;
		requestColumn = requestColumn_;
	}
	
	public Request(int id_) {
		id = id_;
	}
}
