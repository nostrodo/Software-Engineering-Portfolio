package cs499.capstone;

import java.util.Scanner;

public class ConsoleUI {

    Scanner scanner = new Scanner(System.in);


    // login menu
    public String loginMenu() {
        System.out.println();
        System.out.println("Login Menu");
        System.out.println("-------------------------");
        System.out.println("1. Login");
        System.out.println("2. Create Account");
        System.out.println("3. Exit");
        System.out.print("Selection: ");

        return scanner.nextLine();
    }


    // main menu after login
    public String mainMenu() {
        System.out.println();
        System.out.println("Main Menu");
        System.out.println("-------------------------");
        System.out.println("1. Create Claim");
        System.out.println("2. View Claims");
        System.out.println("3. Logout");
        System.out.print("Selection: ");

        return scanner.nextLine();
    }


    // claim menu
    public String claimMenu() {
        System.out.println();
        System.out.println("1. Add Code");
        System.out.println("2. Edit Code");
        System.out.println("3. Remove Code");
        System.out.println("4. Submit Claim");
        System.out.println("5. Return to Main Menu");
        System.out.print("Selection: ");

        return scanner.nextLine();
    }


    // gets a text input from the user
    public String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }


    // displays a message
    public void showMessage(String message) {
        System.out.println(message);
    }


    // displays claim header
    public void showClaimHeader() {
        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println("Current Claim");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-5s %-12s %-10s %-15s%n", "#", "Code", "Units", "Modifier");
        System.out.println("------------------------------------------------------------");
    }


    // displays one line of the claim
    public void showClaimLine(int index, String code, String units, String modifier) {
        System.out.printf("%-5d %-12s %-10s %-15s%n", index +1, code, units, modifier);
    }


    // displays end of claim
    public void showClaimFooter() {
        System.out.println("------------------------------------------------------------");
    }


    // placeholder for future submitted claim history
    public void showClaimsPlaceholder() {
        System.out.println();
        System.out.println("Submitted Claims");
        System.out.println("-------------------------");
        System.out.println("Claim history will be available in a future enhancement.");
    }


    // confirms claim submission
    public boolean confirmSubmit() {
        System.out.print("Submit this claim? (Y/N): ");
        return scanner.nextLine().trim().equalsIgnoreCase("Y");
    }
}