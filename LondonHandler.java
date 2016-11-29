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

				System.out.println("\nHandling Request...\n" + 
						"Host: " + clientSocket.getInetAddress().getHostAddress() + 
						"\nPort: " + clientSocket.getPort());

				// Get scannerId from client
				String scannerId = in.readUTF();
				String scannerQuery = "SELECT * FROM Scanner WHERE scanner_id=" + scannerId + ";";

				try {
					// Query DB for location, type, and fee
					rs = stmt.executeQuery(scannerQuery);
				} catch (Exception ex) {
					System.out.println("-- Query ResultSet Error: Scanner Query");
					System.out.println(ex);
				}
				
				// Loop over the single result and save the info into variables
				while (rs.next()) {
					scannerLocation = rs.getString("location");
					scannerType = rs.getString("type");
					scannerFee = Double.parseDouble(rs.getString("fee"));
				}


				System.out.println("\nScanner Info:\n" + 
						"Location: " + scannerLocation + 
						"\nType: " + scannerType + 
						"\nFee: " + scannerFee + "\n");


				// wait for card to be scanned
				String barcode = new String("");
				int cardId = -1;
				String firstName = "";
				String lastName = "";
				Double balance = -1.0;
				int deadline = -1;
				while(!barcode.equals("quit")) {
					barcode = in.readUTF();

					// get user info by querying db on barcode
					String barcodeQuery = "SELECT * FROM TransitCard WHERE barcode=" + barcode + ";";
					try {
						// Query DB for user's info
						rs = stmt.executeQuery(barcodeQuery);
					} catch (Exception ex) {
						System.out.println("-- Query ResultSet Error: Barcode Query");
						System.out.println(ex);
					}

					// Loop over the single result and save the info into variables
					while (rs.next()) {
						cardId = rs.getInt("card_id");
						firstName = rs.getString("first_name");
						lastName = rs.getString("last_name");
						balance = Double.parseDouble(rs.getString("balance"));
						deadline = rs.getInt("deadline");
					}

					// check to see if they have sufficient funds
					// if not succient funds, add $10
					if (balance - scannerFee < 0) {
						balance += 10;
					}

					// deduct fee amount from card balance
					balance -= scannerFee;

					// write to client their name, location, type, fee charge, and remaining balance
					out.writeUTF(firstName+"/"+lastName+"/"+scannerLocation+"/"+scannerType+"/"+scannerFee+"/"+balance);

					// insert into CardScanner Table to indicate a "transaction"
					String insert = "INSERT INTO CardScanner (card_id, scanner_id, entryDate, balance) VALUES ("+cardId+", "+scannerId+", CURRENT_TIMESTAMP,"+balance+");";
					try {
						int insertSuccess = stmt.executeUpdate(insert);
					} catch (Exception ex) {
						System.out.println("-- Query Error: CardScanner Insert");
						System.out.println(ex);
					}
					

					// decrement the deadline value for Summary
					deadline -= 1;

					// check if deadline value is zero
					if (deadline == 0) {
						// query for all travel history for the user since last deadline
						String historySearch = "SELECT cs.balance,s.location,s.type,s.fee,cs.entryDate FROM TransitCard c,Scanner s,CardScanner cs WHERE c.card_id = "+cardId+" AND c.card_id = cs.card_id AND cs.scanner_id = s.scanner_id ORDER BY cs.entryDate;";
						try {
							rs = stmt.executeQuery(historySearch);
						} catch (Exception ex) {
							System.out.println("-- Query Error: History Search");
							System.out.println(ex);
						}
						

						// display the history on server side to indicate "mailing" statement
						System.out.println("Date\t\tFrom\t\t\tCharge\tBalance");
						while (rs.next()) {
							System.out.println(rs.getDate("entryDate")+"\t"+
								rs.getString("location")+"\t"+
								rs.getString("fee")+"\t"+
								rs.getString("balance"));
						}

						// delete all the user's entries in CardScanner table
						String delete = "DELETE FROM CardScanner WHERE card_id = " + cardId + ";";
						try {
							int deleteSuccess = stmt.executeUpdate(delete);
						} catch (Exception ex) {
							System.out.println("-- Query Error: CardScanner Delete");
							System.out.println(ex);
						}

						// reset deadline value to 5
						deadline = 5;
					}

					// update TransitCard entry with the new balance and deadline
					String update = "UPDATE TransitCard SET balance='"+balance+"', deadline="+deadline+" WHERE card_id="+cardId+";";
					try {
						int udpateSuccess = stmt.executeUpdate(update);
					} catch (Exception ex) {
						System.out.println("-- Query Error: TransitCard Update");
						System.out.println(ex);
					}
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

				out.close();
				in.close();
				clientSocket.close();
			} catch (IOException ex) {
				System.out.println("-- Connection to scanner lost.");
			}
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot connect the database!", e);
		}
	}

}