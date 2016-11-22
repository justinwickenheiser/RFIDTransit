import java.io.*;
import java.net.*;
import java.util.*;

public class LondonServer {

	public LondonServer() throws IOException {
		// Establish the listen socket.
		ServerSocket serverListenSocket = new ServerSocket(4096);
		while (true) {
			// Listen for a TCP connection request.
			Socket clientSocket = serverListenSocket.accept();

			LondonHandler handlerForClient = new LondonHandler(clientSocket);
			handlerForClient.start();
		}
	}

	public static void main (String args[]) throws IOException { 
		if (args.length != 0) {
			throw new RuntimeException ("Syntax: java LondonServer"); 
		}
		new LondonServer(); 
	}
	
}