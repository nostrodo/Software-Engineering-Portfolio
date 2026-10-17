package cs499.capstone;

public class ClaimLine {
	// claim line info
	String code;
	int units;
	String modifier;
	
	// constructor
	public ClaimLine(String code, int units, String modifier) {
		this.code = code;
		this.units = units;
		this.modifier = modifier;
	}
	
	// getters
	public String getCode() { return code; }
	public int getUnits() { return units; }
	public String getModifier() { return modifier; }
}
