package cs499.capstone;

import java.util.ArrayList;

public class Claim {
	
	// saved claim info
	String claimNumber;
	ArrayList<ClaimLine> lines;
	
	// constructor
	public Claim(String claimNumber, ArrayList<ClaimLine> lines) {
		this.claimNumber = claimNumber;
		this.lines = lines;
	}
	
	// getters
	public String getClaimNumber() { return claimNumber; }
	public ArrayList<ClaimLine> getLines() { return lines; }

}
