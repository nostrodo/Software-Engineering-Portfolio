package cs499.capstone;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class CodeCatalog {
	
	// store codes for lookup
	HashMap<String, ProcedureCode> codes = new HashMap<>();
	
	public CodeCatalog() {
		try {
			Scanner file = new Scanner(new File("medical_codes.csv"));
			
			// skip header
			if (file.hasNextLine()) { file.nextLine(); }
			
			//read each medical code
			while (file.hasNextLine()) {
				String[] values = file.nextLine().split(",");
				
				String code = values[0].trim();
				String description = values[1].trim();
				double price = Double.parseDouble(values[2].trim());
				
				codes.put(code, new ProcedureCode(code, description, price));
			}
			
			file.close();
		} catch (FileNotFoundException e) {
			System.out.println("CSV not found.");
		}
	}
	
	public ProcedureCode getCode(String code) {
		return codes.get(code);
	}
}
