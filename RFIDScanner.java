import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;

public class RFIDScanner {

	String scannerId;
	String location;
	double fee;
	String server;

	// constructor
	public RFIDScanner(String sId) throws IOException {
		scannerId = sId;

		// Establish DB dataSource and connection
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("suarezka");
		dataSource.setPassword("suarezka");
		dataSource.setServerName("mysql.cis.gvsu.edu");
		Connection conn = dataSource.getConnection();
		Statement stmt = conn.createStatement();

		// Query DB for location and fee
		ResultSet rs = stmt.executeQuery("SELECT ID FROM USERS");
		
		


		//location = loc;
		//fee = amt;
		server = "localhost";

		Scanner input = new Scanner(System.in);

		/*
		 *
		 *	INITIAL CONNECTION
		 *
		 */
				
		// Create socket that is connected to server on specified port
		Socket socket = new Socket(server, 4096);
		DataInputStream in  = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());

		// write the location and fee amount of scanner to the server
		out.writeUTF(location + " " + Double.toString(fee));

		// close DB connections
		rs.close();
		stmt.close();
		conn.close();

		// close connection socket
		in.close();
		out.close();
		socket.close();
		System.exit(0);
	}

	public static void main (String args[]) throws IOException { 
		if (args.length != 1) {
			throw new RuntimeException ("Syntax: java RFIDScanner <scannerId>"); 
		}
		RFIDScanner rfid = new RFIDScanner(args[0]); 
	}
}