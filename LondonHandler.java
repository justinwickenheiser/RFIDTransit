import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.*;
import javax.sql.*;

public class LondonHandler extends Thread {

	Socket clientSocket;
	DataInputStream in;
	DataOutputStream out;

	String scannerLocation;
	String scannerType;
	double scannerFee;

	String url = "jdbc:mysql://localhost:3306/datacomm";
	String username = "wickenhj";
	String password = "password";
	//Statement stmt;

	protected static Vector handlers = new Vector();

	// Constructor
	public LondonHandler(Socket socket) throws IOException {
		this.clientSocket = socket;
		this.in = new DataInputStream (clientSocket.getInputStream());
		this.out = new DataOutputStream(clientSocket.getOutputStream());

		
	}

	public void run() {
		// Connect to the datacomm database	
		try {
			Connection conn = DriverManager.getConnection(url, username, password);
			System.out.println("Database connected!");

			// Create a database statement object
			Statement stmt = conn.createStatement();
			ResultSet rs = null;
			// try connecting and receiving scanner info
			try {
				// Add this thread to the vector of threads
				handlers.addElement(this);

				System.out.println("\nHandling Request...\n" + 
						"Host: " + clientSocket.getInetAddress().getHostAddress() + 
						"\nPort: " + clientSocket.getPort());

				// Get scannerId from client
				String scannerInfo = in.readUTF();

				try {
					// Query DB for location, type, and fee
					// ResultSet rs = stmt.executeQuery("SELECT ID FROM USERS");
				} catch (Exception ex) {
					System.out.println("-- Query ResultSet Error");
					System.out.println(ex);
				}
				
				
				// temp variables
				scannerLocation = "Kings Cross";
				scannerType = "Rail";
				scannerFee = Double.parseDouble("2.50");


				System.out.println("\nScanner Info:\n" + 
						"Location: " + scannerLocation + 
						"\nType: " + scannerType + 
						"\nFee: " + scannerFee + "\n");


				// wait for card to be scanned
				String barcode = new String("");
				while(!barcode.equals("quit")) {
					barcode = in.readUTF();

					// get user info by querying db on barcode

					// check to see if they have sufficient funds
					// if not succient funds, add $10

					// deduct fee amount from card balance

					// write to client their name, location, type, fee charge, and remaining balance
					out.writeUTF("Bob/Ross/Kings Cross/Rail/" + scannerFee + "/7.50");

					// decrement the deadline value for Summary

					// check if deadline value is zero.
					// if deadline == 0, 
					// 		query for all travel history for the user since last deadline
					//		display the history on server side to indicate "mailing" statement
					//		delete all the user's entries in CardScanner table
					//		reset deadline value to 5 in TransitCard table

				}

			


				// close database connection and statement
				try {
					rs.close();
					stmt.close();
					conn.close();
				} catch (Exception ex) {
					System.out.println("-- Error closing the connection.");
					System.out.println(ex);
				}

				// remove RFIDscanner from vector
				handlers.remove(this);

				out.close();
				in.close();
				clientSocket.close();
			} catch (IOException ex) {
				System.out.println("-- Connection to scanner lost.");
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot connect the database!", e);
			// out.close();
			// in.close();
			// clientSocket.close();
		}
	}

}