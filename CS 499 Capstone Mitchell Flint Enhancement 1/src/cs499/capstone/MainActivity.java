package cs499.capstone;

public class MainActivity {

    // class variables
    DatabaseHelper db;
    ConsoleUI ui;
    String user, pass;


    public static void main(String[] args) {
        MainActivity main = new MainActivity();
        main.onCreate();
    }


    protected void onCreate() {

        // initialize database and UI
        db = new DatabaseHelper();
        ui = new ConsoleUI();

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

        db.insertUser(user, pass);
        ui.showMessage("Account created");
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
                ClaimActivity claim = new ClaimActivity(ui);
                claim.onCreate();
            }

            else if (selection.equals("2")) { ui.showClaimsPlaceholder(); }

            else if (selection.equals("3")) { return;}
        }
    }
}