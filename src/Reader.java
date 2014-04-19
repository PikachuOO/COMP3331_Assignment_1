/*
 *
 *  client for TCPClient from Kurose and Ross
 *
 *  * Usage: java TCPClient [server addr] [server port]
 */
import java.io.*;
import java.net.*;

public class Reader {

	public static void main(String[] args) throws Exception {
		String mode;
		int pollingInterval;
		String userName;
		String serverName;
		int serverPort;
		if (args.length != 5) {
			System.err.println("Incorrect number of args");
			return;
		} else {
			mode = args[0];
			pollingInterval = Integer.parseInt(args[1]);
			userName = args[2];
			serverName = args[3];
			serverPort = Integer.parseInt(args[4]);
		}

		TCP tcp = new TCP(serverName, serverPort);
		tcp.outToServer.writeBytes(userName + '\n');
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		while (!inFromUser.ready()) {
			String clientRequest = inFromUser.readLine();
			
			Request request = createRequest(clientRequest, userName);
			if (request == null) {
				System.err.println("request is invalid");
			} else {
				tcp.objectsOutToServer.writeObject(request);
				Object response = tcp.objectsInFromServer.readObject();
				System.out.println(response.toString());
			}
		}
		
	}
	
	
	private static Request createRequest (String clientRequest, String userName) {
		clientRequest = clientRequest.replaceAll("^ *", "").toLowerCase();
		Request request = null;
		if (clientRequest.startsWith("display")) {
			request = new DisplayRequest(userName, clientRequest.split(" ")[1], clientRequest.split(" ")[2]);
		}
		if (clientRequest.startsWith("post_to_forum")) {
			request = new PostRequest(userName, clientRequest.split(" ")[1], clientRequest.split(" ")[2]);
		}
		return request;
		
	}


	private static class TCP {
		private InetAddress serverIPAddress;
		private Socket clientSocket;
		private DataOutputStream outToServer;
		private BufferedReader inFromServer;
		private ObjectOutputStream objectsOutToServer;
		private ObjectInputStream objectsInFromServer;

		TCP (String serverName, int serverPort) throws IOException {
			this.serverIPAddress = InetAddress.getByName(serverName);
			this.clientSocket = new Socket(this.serverIPAddress, serverPort);
			this.outToServer = new DataOutputStream(this.clientSocket.getOutputStream());
			this.inFromServer = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			this.objectsOutToServer = new ObjectOutputStream(clientSocket.getOutputStream());
			this.objectsInFromServer = new ObjectInputStream(clientSocket.getInputStream());
		}

		void write(String data) throws IOException {
			this.outToServer.writeBytes(data + '\n');
		}
	}

}