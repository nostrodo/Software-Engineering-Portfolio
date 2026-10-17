package cs499.capstone;

public class ProcedureCode {
	
	String code;
	String description;
	double price;
	
	// constructor
	public ProcedureCode(String code, String description, double price) {
		this.code = code;
		this.description = description;
		this.price = price;
	}
	
	// getters
	public String getCode() { return code; }
	public String getDescription() { return description; }
	public double getPrice() { return price; }

}
