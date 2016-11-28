import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;


public class RFIDScanner {

	String scannerId;
	String firstName;
	String lastName;
	String location;
	String type;
	double remainingBalance;
	double fee;
	String server;

	// constructor
	public RFIDScanner(String sId) throws IOException {
		scannerId = sId;

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

		// write the scannerId to the server
		out.writeUTF(scannerId);

		// wait for card to be scanned
		String barcode = new String("");
		String response = new String("");
		barcode = input.nextLine();
		while(!barcode.equals("quit")) {

			// write the scanned card's barcode to the server
			out.writeUTF(barcode);

			// read remaining balance
			response = in.readUTF();
			String responseVals[] = response.split("/");
			firstName = responseVals[0];
			lastName = responseVals[1];
			location = responseVals[2];
			type = responseVals[3];
			fee = Double.parseDouble(responseVals[4]);
			remainingBalance = Double.parseDouble(responseVals[5]);

			System.out.println("---------- Summary ----------\n" + 
				"Name:\t\t\t" + firstName + " " + lastName + "\n" +
				"Location:\t\t" + location + " - " + type + "\n" +
				"Charge:\t\t\t" + fee + "\n" +
				"Remaining Balance:\t" + remainingBalance + "\n");

			barcode = input.nextLine();
		}

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