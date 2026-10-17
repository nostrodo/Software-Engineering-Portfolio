package cs499.capstone;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class ClaimActivity {

    // class variables
    DatabaseHelper db;
    ConsoleUI ui;
    CodeCatalog catalog;
    ArrayList<Integer> ids = new ArrayList<>();
    int selectedIndex = -1;
    int userId;


    // constructor
    public ClaimActivity(ConsoleUI ui, int userId) {
    	this.ui = ui;
    	this.userId = userId;
    	}


    protected void onCreate() {

        // initialize database
        db = new DatabaseHelper();
        
        // load codes
        catalog = new CodeCatalog();

        // loads current claim
        load();

        while (true) {

            String selection = ui.claimMenu();

            // add code
            if (selection.equals("1")) { addCode(); }

            // edit code
            else if (selection.equals("2")) { editCodeSelection(); }

            // remove code
            else if (selection.equals("3")) { removeCodeSelection(); }

            // submit claim
            else if (selection.equals("4")) {
                if (submitClaim()) { return; }
            }

            // return to main menu
            else if (selection.equals("5")) { return; }
        }
    }


    // loading helper function
    private void load() {

        // resets screen data and clears stale selection
        ids.clear();
        selectedIndex = -1;

        // gets all codes in the current claim
        ResultSet c = db.getDraftLines(userId);
        int index = 0;

        ui.showClaimHeader("Current Claim");

        // loops through each code
        try {
            while (c != null && c.next()) {

                // gets the values from each claim line
                int lineId = c.getInt(1);
                String code = c.getString(2);
                String units = c.getString(3);
                String modifier = c.getString(4);
                ids.add(lineId);
                
                // get the code details
                ProcedureCode procedure = catalog.getCode(code);
                String description = "";
                double charge = 0;
                
                if (procedure != null) {
                	description = procedure.getDescription();
                	charge = procedure.getPrice() * Integer.parseInt(units);
                }
                

                // displays the claim line
                ui.showClaimLine(index, code, description, units, modifier, charge);

                index++;
            }

            if (c != null) { c.close(); }

        } catch (SQLException e) { e.printStackTrace(); }

        ui.showClaimFooter();
    }


    // row selection function
    private void selectRow(int index) { selectedIndex = index; }


    // gets and validates a claim line selection
    private int getSelectedIndex() {

        String input = ui.getInput("Select code number: ");

        try {
            int index = Integer.parseInt(input) - 1;
            selectRow(index);

            if (selectedIndex < 0 || selectedIndex >= ids.size()) {
                ui.showMessage("Select a valid code");
                return -1;
            }

            return selectedIndex;

        } catch (NumberFormatException e) {
            ui.showMessage("Select a valid code");
            return -1;
        }
    }


    // add code function
    private void addCode() {

        String code = ui.getInput("Medical code (X00001 - X00100): ");
        String units = ui.getInput("Units (REQUIRED): ");
        String modifier = ui.getInput("Modifier (OPTIONAL): ");

        code = code.trim().toUpperCase();
        units = units.trim();
        modifier = modifier.trim();

        // input validation
        if (!validClaimLine(code, units, modifier)) { return; }

        // adds the new code to the current claim, then reloads the list
        db.insertDraftLine(userId, code, Integer.parseInt(units), modifier);
        load();
    }


    // selects a code before editing
    private void editCodeSelection() {

        int index = getSelectedIndex();

        if (index == -1) { return; }

        editCode(index);
    }


    // edits selected code
    private void editCode(int index) {

        if (index >= ids.size()) { return; }

        // checks the database and gets code details
        ResultSet c = db.getDraftLines(userId);
        
        if (c == null) {
        	ui.showMessage("Could not load claim");
        	return;
        }
        
        int i = 0;

        String currentCode = "";
        String currentUnits = "";
        String currentModifier = "";

        try {
            while (c != null && c.next()) {

                if (i == index) {
                    currentCode = c.getString(2);
                    currentUnits = c.getString(3);
                    currentModifier = c.getString(4);
                    break;
                }

                i++;
            }

            if (c != null) {
                c.close();
            }

        } catch (SQLException e) { e.printStackTrace(); }


        String code = ui.getInput("Medical code [" + currentCode + "] (X00001 - X00100, blank = keep): ");
        String units = ui.getInput("Units [" + currentUnits + "] (blank = keep): ");
        String modifier = ui.getInput("Modifier [" + currentModifier + "] (blank = keep, NONE = clear): ");

        // retains current data when field is left blank
        if (code.isEmpty()) { code = currentCode; }

        if (units.isEmpty()) { units = currentUnits; }
        
        if (modifier.equalsIgnoreCase("NONE")) { modifier = ""; }
        else if (modifier.isEmpty()) { modifier = currentModifier; }

        code = code.trim().toUpperCase();
        units = units.trim();
        modifier = modifier.trim();

        if (!validClaimLine(code, units, modifier)) { return; }

        // updates the DB and reloads the view
        db.updateDraftLine(userId, ids.get(index), code, Integer.parseInt(units), modifier);
        load();
    }


    // selects code before removal
    private void removeCodeSelection() {

        int index = getSelectedIndex();

        if (index == -1) { return; }

        removeCode(index);
    }


    // removes selected code
    private void removeCode(int index) {

        if (index < ids.size() && index >= 0) {
            db.deleteDraftLine(userId, ids.get(index));
            load();
        }
    }


    // shared validation for adding and editing codes
    private boolean validClaimLine(String code, String units, String modifier) {

    	// first make sure code was entered
        if (code.isEmpty() || units.isEmpty()) {
            ui.showMessage("A CPT code and a unit amount is required");
            return false;
        }
        
        // then check if code exists
        if (catalog.getCode(code) == null) {
        	ui.showMessage("Medical code not found, valid codes are in the range X00001 - X00100.");
        	return false;
        }

        try {
            int numberOfUnits = Integer.parseInt(units);

            if (numberOfUnits < 1) {
                ui.showMessage("Units must be greater than zero");
                return false;
            }

        } catch (NumberFormatException e) {
            ui.showMessage("Units must be a number");
            return false;
        }

        return true;
    }


    // submits and clears the current claim
    private boolean submitClaim() {

        if (ids.isEmpty()) {
            ui.showMessage("Claim is empty");
            return false;
        }

        if (!ui.confirmSubmit()) {
        	load();
            return false;
        }
        
        // store claim lines
        ArrayList<ClaimLine> lines = new ArrayList<>();
        
        // copy claim to the list
        try {
        	ResultSet c = db.getDraftLines(userId);
        	
        	while (c != null && c.next()) {
        		String code = c.getString(2);
        		int units = c.getInt(3);
        		String modifier = c.getString(4);
        		
        		lines.add(new ClaimLine(code, units, modifier));
        	}
        	
        	if (c != null) { c.close(); }
        	
        } catch (SQLException e) {
        	e.printStackTrace();
        	ui.showMessage("Could not save claim");
        	return false;
        }
        
        // save the submitted claim to the db
        if (!db.saveClaim(userId,  lines)) {
        	ui.showMessage("Claim could not be submitted");
        	return false;
        }

        ids.clear();
        selectedIndex = -1;

        ui.showMessage("Claim submitted");

        return true;
    }
}