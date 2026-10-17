package cs499.capstone;

import java.util.ArrayList;

public class MainActivity {

    // class variables
    DatabaseHelper db;
    ConsoleUI ui;
    String user, pass;
    CodeCatalog catalog = new CodeCatalog();


    public static void main(String[] args) {
        MainActivity main = new MainActivity();
        main.onCreate();
    }


    protected void onCreate() {

        // initialize database and UI
        db = new DatabaseHelper();
        ui = new ConsoleUI();
        
        // hash test
        // db.testPasswordStorage();

        while (true) {

            String selection = ui.loginMenu();

            // create user
            if (selection.equals("2")) { createAccount(); }

            // login
            else if (selection.equals("1")) { logIn(); }

            // exit
            else if (selection.equals("3")) { break; }
        }
    }


    // creates a new account
    private void createAccount() {

        user = ui.getInput("Username: ");
        pass = ui.getInput("Password: ");

        // null input validation
        if (user.isEmpty() || pass.isEmpty()) {
            ui.showMessage("Enter username and password");
            return;
        }

        // show prompt if username already exists, else create account
        if (db.userExists(user)) {
            ui.showMessage("User exists");
            return;
        }

        if (db.insertUser(user,  pass)) {
        	ui.showMessage("Account created");
        } else {
        	ui.showMessage("Could not create account");
        }
    }


    // checks user information and logs in
    private void logIn() {

        user = ui.getInput("Username: ");
        pass = ui.getInput("Password: ");

        // check user, if exists = move to main menu
        if (!db.checkUser(user, pass)) {
            ui.showMessage("Invalid login");
            return;
        }

        mainMenu();
    }


    // main menu after login
    private void mainMenu() {

        while (true) {

            String selection = ui.mainMenu();

            if (selection.equals("1")) {
            	
            	int userId = db.getUserId(user);
            	
            	if (userId == -1) {
            		ui.showMessage("Could not identify user");
            		continue;
            	}
            	
            	ClaimActivity claim = new ClaimActivity(ui, userId);
            	
                // ClaimActivity claim = new ClaimActivity(ui, submittedClaims);
                claim.onCreate();
            }

            else if (selection.equals("2")) { viewClaims(); }

            else if (selection.equals("3")) { return;}
        }
    }
    
    // display submitted claims
    private void viewClaims() {
    	
    	// load submitted claims
    	int userId = db.getUserId(user);
    	
    	if (userId == -1) {
    		ui.showMessage("could not identify user");
    		return;
    	}
    	
    	ArrayList<Claim> submittedClaims = db.getClaims(userId);
    	
    	
    	if (submittedClaims.isEmpty()) {
    		ui.showMessage("No submitted claims");
    		return;
    	}
    	
    	System.out.println();
    	System.out.println("Submitted Claims");
    	ui.showMenuSeperator();
    	
    	// display each claim
    	for (Claim claim : submittedClaims) {
    		
    		System.out.println();
    		System.out.println(claim.getClaimNumber());
    		
    		ui.showClaimHeader("Claim Details");
    		
    		// claim variables
    		double total = 0;
    		int index = 0;
    		
    		// display each line on the claim    		
    		for (ClaimLine line : claim.getLines()) {
    			
    			ProcedureCode procedure = catalog.getCode(line.getCode());
    			
    			if (procedure != null) {
    				double charge = procedure.getPrice() * line.getUnits();
    				total += charge;
    				
    				ui.showClaimLine(index, line.getCode(), procedure.getDescription(), String.valueOf(line.getUnits()),
    						line.getModifier(), charge);
    				
    			}
    			
        		index++;
    		}
    		
    		ui.showClaimFooter();
    		System.out.printf("Claim Total: $%.2f%n", total);
    	}
    }
}