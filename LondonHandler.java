import java.io.*;
import java.net.*;
import java.util.*;

public class LondonHandler extends Thread {

	Socket clientSocket;
	DataInputStream in;
	DataOutputStream out;

	String clientLocation;
	double clientFee;

	protected static Vector handlers = new Vector();

	// Constructor
	public LondonHandler(Socket socket) throws IOException {
		this.clientSocket = socket;
		this.in = new DataInputStream (clientSocket.getInputStream());
		this.out = new DataOutputStream(clientSocket.getOutputStream());
	}

	public void run() {
		// try connecting and receiving scanner info
		try {
			// Add this thread to the vector of threads
			handlers.addElement(this);

			System.out.println("\nHandling Request...\n" + 
					"Host: " + clientSocket.getInetAddress().getHostAddress() + 
					"\nPort: " + clientSocket.getPort());

			// Get location and fee amount from scanner
			String scannerInfo = in.readUTF();
			String[] scannerInfoArgs = scannerInfo.split(" ");

			// Store that info into "Users" table
			clientLocation = scannerInfoArgs[0];
			clientFee = Double.parseDouble(scannerInfoArgs[1]);

			System.out.println("\nAdded Scanner to 'Scanner' Table...\n" + 
					"Scanner Location: " + clientLocation + 
					"\nFee: " + clientFee + "\n");




			// remove RFIDscanner from vector
			handlers.remove(this);

			out.close();
			in.close();
			clientSocket.close();
		} catch (IOException ex) {
			System.out.println("-- Connection to scanner lost.");
		}
	}

}