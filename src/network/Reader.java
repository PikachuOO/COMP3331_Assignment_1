package network;
/*
 *
 *  client for TCPClient from Kurose and Ross
 *
 *  * Usage: java TCPClient [server addr] [server port]
 */
import java.io.*;
import java.net.*;

import model.Page;
import request.DisplayRequest;
import request.PostRequest;
import request.Request;
import response.DisplayResponse;

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
		Page mostRecentPage = null;

		TCP tcp = new TCP(serverName, serverPort);
		tcp.outToServer.writeBytes(userName + '\n');
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		while (!inFromUser.ready()) {
			String clientRequest = inFromUser.readLine();
			StringBuilder errorMessage = new StringBuilder();
			Request request = createRequest(clientRequest, userName, mostRecentPage, errorMessage);
			if (request == null) {
				System.err.println(errorMessage.toString());
			} else {
				tcp.objectsOutToServer.writeObject(request);
				Object response = tcp.objectsInFromServer.readObject();
				if (response instanceof DisplayResponse) {
					mostRecentPage = ((DisplayResponse) response).getPage();
				}
				System.out.println(response.toString());
			}
		}

	}


	private static Request createRequest (String clientRequest, String userName, Page mostRecentPage, StringBuilder errorMessage) {
		clientRequest = clientRequest.replaceAll("^ *", "").toLowerCase();
		Request request = null;
		if (clientRequest.startsWith("display")) {
			if (clientRequest.split(" ").length < 3) {
				errorMessage.append("Usage: display [bookName] [pageNumber]\n");
			} else {
				request = new DisplayRequest(userName, clientRequest.split(" ")[1], clientRequest.split(" ")[2]);
			}
		}
		else if (clientRequest.startsWith("post_to_forum")) {
			if (mostRecentPage == null) {
				errorMessage.append("Please go to a page first before you post to it\n");
			}
			else if (clientRequest.split(" ").length < 3) {
				errorMessage.append("Usage: post_to_forum [lineNumber] [content]\n");
			} else {
				request = new PostRequest(userName, mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), clientRequest.split(" ")[1], clientRequest.replaceAll("^\\w* \\w* ", ""));
			}
		} else {
			errorMessage.insert(0, "Usage: (display [bookName] [pageNumber] | post_to_forum [lineNumber] [content])\n");
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