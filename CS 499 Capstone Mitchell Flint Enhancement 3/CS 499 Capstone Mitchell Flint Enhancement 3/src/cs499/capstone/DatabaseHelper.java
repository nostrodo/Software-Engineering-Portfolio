package cs499.capstone;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.security.GeneralSecurityException;

public class DatabaseHelper {
    // class variables
    private static final String DATABASE_NAME = "app.db";
    private Connection db;

    // constructor
    public DatabaseHelper() {
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
            onCreate(db);
        } catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
    }

    public void onCreate(Connection db) {

        // when DB is created, make a table for users and items
        try {        	
            Statement statement = db.createStatement();
            
        	statement.execute("PRAGMA foreign_keys = ON");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
            		+ "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            		+ "username TEXT UNIQUE NOT NULL, "
            		+ "password_hash TEXT NOT NULL,"
            		+ "salt TEXT NOT NULL)");

            // stores unfinished claim lines for each user
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS draft_claim_lines ("
            		+ "line_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            		+ "user_id INTEGER NOT NULL, "
            		+ "code TEXT NOT NULL, "
            		+ "units INTEGER NOT NULL CHECK (units > 0), "
            		+ "modifier TEXT, "
            		+ "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // store submitted claims
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS claims ("
            		+ "claim_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            		+ "user_id INTEGER NOT NULL, "
            		+ "FOREIGN KEY (user_id) REFERENCES users(id))");
            
            // sores the medical code lines attached to the claims
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS claim_lines ("
            		+ "line_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            		+ "claim_id INTEGER NOT NULL, "
            		+ "code TEXT NOT NULL, "
            		+ "units INTEGER NOT NULL CHECK (units > 0), "
            		+ "modifier TEXT, "
            		+ "FOREIGN KEY (claim_id) REFERENCES claims(claim_id))");
            
            

            statement.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }


    // user table functions

    // create user
    public boolean insertUser(String username, String password) {
        try {
        	String salt = PasswordHelper.generateSalt();
        	String hash = PasswordHelper.hashPassword(password, salt);
        	
            PreparedStatement values = db.prepareStatement("INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)");
            
            values.setString(1, username);
            values.setString(2, hash);
            values.setString(3,  salt);
            
            boolean created = values.executeUpdate() > 0;
            values.close();
            
            return created;
        } catch (SQLException | GeneralSecurityException e) { 
        	e.printStackTrace();
        	return false; 
        	}
    }

    // check that user data matches when logging in
    public boolean checkUser(String username, String password) {
        try {
            PreparedStatement c = db.prepareStatement("SELECT password_hash, salt FROM users WHERE username=?");
            
            c.setString(1, username);
            ResultSet result = c.executeQuery();
            
            boolean valid = false;
            
            if (result.next()) {
            	String storedHash = result.getString("password_hash");
            	String salt = result.getString("salt");
            	
            	valid = PasswordHelper.verifyPassword(password,  salt,  storedHash);
            }
            
            result.close();
            c.close();
            
            return valid;
        } catch (SQLException | GeneralSecurityException e) { 
        	e.printStackTrace();
        	return false; 
        	}
    }
    
    // gets the ID of the user
    public int getUserId(String username) {
    	try (PreparedStatement statement = db.prepareStatement(
    			"SELECT id FROM users WHERE username = ?")) {
    		statement.setString(1,  username);
    		
    		try (ResultSet result = statement.executeQuery()) {
    			if (result.next()) {
    				return result.getInt("id");
    			}
    		}
    		
    	} catch (SQLException e) { e.printStackTrace(); }
    	
    	return -1;
    }

    // check for existing UN only
    public boolean userExists(String username) {
        try {
            PreparedStatement c = db.prepareStatement("SELECT * FROM users WHERE username=?");
            c.setString(1, username);
            ResultSet result = c.executeQuery();
            boolean exists = result.next();
            result.close();
            c.close();
            return exists;
        } catch (SQLException e) { return false; }
    }


    // draft claim table functions

    // add a code to user's draft
    public boolean insertDraftLine(int userId, String code, int units, String modifier) {
        try {
            PreparedStatement v = db.prepareStatement("INSERT INTO draft_claim_lines (user_id, code, units, modifier) VALUES (?, ?, ?, ?)");
            v.setInt(1,  userId);
            v.setString(2, code);
            v.setInt(3, units);
            v.setString(4, modifier);
            return v.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // loads the current user's draft claim lines
    public ResultSet getDraftLines(int userId) {
        try {
            PreparedStatement c = db.prepareStatement(
            		"SELECT line_id, code, units, modifier FROM draft_claim_lines WHERE user_id = ? ORDER BY line_id");
            c.setInt(1, userId);
            return c.executeQuery();
        } catch (SQLException e) { return null; }
    }

    // update line belonging to current user
    public boolean updateDraftLine(int userId, int lineId, String code, int units, String modifier) {
        try {
            PreparedStatement v = db.prepareStatement("UPDATE draft_claim_lines SET code=?, units=?, modifier=? WHERE line_id=? AND user_id=?");
            v.setString(1, code);
            v.setInt(2, units);
            v.setString(3, modifier);
            v.setInt(4, lineId);
            v.setInt(5, userId);
            return v.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    // deletes the selected item
    public boolean deleteDraftLine(int userId, int lineId) {
        try {
            PreparedStatement statement = db.prepareStatement(
            		"DELETE FROM draft_claim_lines WHERE line_id=? AND user_id=?");
            statement.setInt(1, lineId);
            statement.setInt(2,  userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }
    
    // saves a submitted claim and its medical code lines
    public boolean saveClaim(int userId, ArrayList<ClaimLine> lines) {
    	try {
    		db.setAutoCommit(false);
    		
    		// create and retrieve its new ID
    		PreparedStatement claim = db.prepareStatement(
    				"INSERT INTO claims (user_id) VALUES (?)",
    				Statement.RETURN_GENERATED_KEYS);
    		
    		claim.setInt(1,  userId);
    		claim.executeUpdate();
    		
    		ResultSet keys = claim.getGeneratedKeys();
    		
    		if (!keys.next()) {
    			throw new SQLException("Could not generate claim ID");
    		}
    		
    		int claimId = keys.getInt(1);
    		keys.close();
    		claim.close();
    		
    		// store the individual lines under that ID
    		PreparedStatement line = db.prepareStatement(
    				"INSERT INTO claim_lines (claim_id, code, units, modifier) VALUES (?, ?, ?, ?)");
    		
    		for (ClaimLine item : lines) {
    			line.setInt(1,  claimId);
    			line.setString(2,  item.getCode());
    			line.setInt(3, item.getUnits());
    			line.setString(4,  item.getModifier());
    			line.executeUpdate();
    		}
    		
    		line.close();
    		
    		// clear the draft claim
    		PreparedStatement clear = db.prepareStatement("DELETE FROM draft_claim_lines WHERE user_id = ?");
    		clear.setInt(1,  userId);
    		clear.executeUpdate();
    		clear.close();
    				
    		
    		db.commit();
    		return true;    	
    		
    	} catch (SQLException e) {
    		try {
    			db.rollback();
    		} catch (SQLException rollbackError) { rollbackError.printStackTrace(); }
    		
    		e.printStackTrace();
    		return false;
    		
    	} finally {
    		try {
    			db.setAutoCommit(true);
    		} catch (SQLException e) { e.printStackTrace(); }
    	}
    }
    
    // gets all submitted claims attached to a user
    public ArrayList<Claim> getClaims(int userId) {
    	ArrayList<Claim> claims = new ArrayList<>();
    	
    	try {
    		// find claims submitted by user
    		PreparedStatement claimStatement = db.prepareStatement(
    				"SELECT claim_id FROM claims WHERE user_id = ? ORDER BY claim_id");
    		
    		claimStatement.setInt(1,  userId);
    		ResultSet claimResults = claimStatement.executeQuery();
    		
    		while (claimResults.next()) {
    			int claimId = claimResults.getInt("claim_id");
    			ArrayList<ClaimLine> lines = new ArrayList<>();
    			
    			// get all medical code lines for claim
    			PreparedStatement lineStatement = db.prepareStatement(
    					"SELECT code, units, modifier FROM claim_lines WHERE claim_id = ? ORDER BY line_id");
    			
    			lineStatement.setInt(1,  claimId);
    			ResultSet lineResults = lineStatement.executeQuery();
    			
    			while (lineResults.next()) {
    				String code = lineResults.getString("code");
    				int units = lineResults.getInt("units");
    				String modifier = lineResults.getString("modifier");
    				
    				lines.add(new ClaimLine(code, units, modifier));
    			}
    			
    			lineResults.close();
    			lineStatement.close();
    			
    			// rebuild the claim using existing claim class
    			claims.add(new Claim("CLM" + claimId, lines));
    		}
    		
    		claimResults.close();
    		claimStatement.close();
    		
    	} catch (SQLException e) { e.printStackTrace(); }
    	
    	return claims;
    }
    
 // test to inspect pass hashes
    /*
    public void testPasswordStorage() {
        try {
            Statement statement = db.createStatement();
            ResultSet result = statement.executeQuery(
                    "SELECT username, password_hash, salt FROM users");

            while (result.next()) {
                System.out.println("Username: " + result.getString("username"));
                System.out.println("Hash: " + result.getString("password_hash"));
                System.out.println("Salt: " + result.getString("salt"));
            }

            result.close();
            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    */
}
