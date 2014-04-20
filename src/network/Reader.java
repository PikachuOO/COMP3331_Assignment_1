package network;
/*
 *
 *  client for TCPClient from Kurose and Ross
 *
 *  * Usage: java TCPClient [server addr] [server port]
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import model.Line;
import model.Page;
import request.DisplayRequest;
import request.PingRequest;
import request.PostRequest;
import request.ReadRequest;
import request.Request;
import response.DisplayResponse;
import response.ReadResponse;

public class Reader {

	public static void main(String[] args) throws Exception {
		String mode;
		int pollingInterval;
		String userName;
		String serverName;
		int serverPort;
		TCP tcp;

		if (args.length != 5) {
			//check args length
			System.err.println("Usage: Reader [mode] [polling_interval] [userName] [serverName] [serverPort]\n");
			return;
		}
		if (!args[0].toLowerCase().equals("pull") && !args[0].toLowerCase().equals("push")) {
			//check for valid mode
			System.err.println("Invalid mode: [push|pull]\n");
			return;
		}
		try {
			Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			System.err.println("Polling Interval is not a number\n");
			return;
		}
		try {
			Integer.parseInt(args[4]);
		} catch (NumberFormatException nfe) {
			System.err.println("Server Port is not a number\n");
			return;
		}

		mode = args[0];
		pollingInterval = Integer.parseInt(args[1]);
		userName = args[2];
		serverName = args[3];
		serverPort = Integer.parseInt(args[4]);

		try {
			tcp = new TCP(serverName, serverPort);		//open tcp connection
		} catch (IOException uhe) {
			System.err.println("Could not connect to designated server\n");
			return;
		}

		//client keeps track of last visited page, and posts read
		Page mostRecentPage = null;
		List<Integer> readPosts = new ArrayList<Integer>();
		Timer timer = null;

		tcp.outToServer.writeBytes(userName + '\n');	//writes username to server for server log
		String reply = tcp.inFromServer.readLine();
		System.out.println(reply);

		//Initialization finished
		//#####################################################################

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		while (!inFromUser.ready()) {					//loop for client input

			String clientRequest = inFromUser.readLine();

			StringBuilder errorMessage = new StringBuilder();

			//request object is created with the input string
			Request request = createRequest(clientRequest, userName, mostRecentPage, errorMessage, readPosts);

			if (request == null) {
				//if input string is invalid, createRequest returns null
				System.err.println(errorMessage.toString());
			} else {

				if (request instanceof DisplayRequest) {

				}

				//if request creation is successful, write and wait for response object
				tcp.objectsOutToServer.writeObject(request);
				tcp.objectsOutToServer.flush();

				Object response = tcp.objectsInFromServer.readObject();

				if (response instanceof DisplayResponse) {
					//update the last visited page
					mostRecentPage = ((DisplayResponse) response).getPage();
					//once the client receives a DisplayResponse, we can be sure the display request was successful

					if (timer != null) {
						timer.cancel();
					}
					timer = new Timer();
					timer.scheduleAtFixedRate(new Ping(tcp, userName, mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), readPosts), pollingInterval * 1000, pollingInterval * 1000);
				}
				if (response instanceof ReadResponse) {
					//update the list of read posts
					readPosts = ((ReadResponse) response).getReadPosts();
				}
				//any errors on server side would return response object messageResponse with error description
				System.out.println(response.toString());
			}
		}
	}

	private static Request createRequest (String clientRequest, String userName, Page mostRecentPage, StringBuilder errorMessage, List<Integer> readPosts) {
		clientRequest = clientRequest.replaceAll("^ *", "").toLowerCase();
		Request request = null;
		try {
			if (clientRequest.startsWith("display")) {
				if (clientRequest.split(" ").length < 3) {
					errorMessage.append("Usage: display [bookName] [pageNumber]\n");
				} else {
					request = new DisplayRequest(clientRequest, userName, clientRequest.split(" ")[1], Integer.parseInt(clientRequest.split(" ")[2]), readPosts);
				}
			}
			else if (clientRequest.startsWith("post_to_forum")) {
				if (mostRecentPage == null) {
					errorMessage.append("Please go to a page first before you post to it\n");
				}
				else if (clientRequest.split(" ").length < 3) {
					errorMessage.append("Usage: post_to_forum [lineNumber] [content]\n");
				} else {
					request = new PostRequest(clientRequest, userName, mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), clientRequest.split(" ")[1], clientRequest.replaceAll("^\\w* \\w* ", ""));
				}
			} 
			else if (clientRequest.startsWith("read_post")) {
				if (mostRecentPage == null) {
					errorMessage.append("Please go to a page first before you post to it\n");
				}
				else if (clientRequest.split(" ").length < 2) {
					errorMessage.append("Usage: read_post [lineNumber]\n");
				} else {
					request = new ReadRequest(clientRequest, userName, mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), clientRequest.split(" ")[1], readPosts);
				}

			} else {
				errorMessage.append("Usage: (display [bookName] [pageNumber] | post_to_forum [lineNumber] [content])\n");
			}
		} catch (NumberFormatException nfe) {
			errorMessage.append("invalid Number inputed somewhere!\n");
		}
		return request;
	}

	private static class Ping extends TimerTask {

		private TCP tcp;
		private String userName;
		private String bookName;
		private int pageNumber;
		private List<Integer> readPosts;
		private boolean notNotified;


		private Ping (TCP tcp, String userName, String bookName, int pageNumber, List<Integer> readPosts) {
			this.tcp = tcp;
			this.userName = userName;
			this.bookName = bookName;
			this.pageNumber = pageNumber;
			this.readPosts = readPosts;
			this.notNotified = true;
		}

		@Override
		public void run() {
			try {
				tcp.objectsOutToServer.writeObject(new PingRequest("ping " + this.bookName + " " + this.pageNumber, this.userName, this.bookName, this.pageNumber, this.readPosts));
				Object response = tcp.objectsInFromServer.readObject();
				
				if (response != null && this.notNotified) {
					System.out.println(response.toString());
					this.notNotified = false;
				}
				else if (response == null ) {
					this.notNotified = true;
				}

			} catch (IOException e) {
				System.err.print("Server disconnected\r");
				System.exit(0);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

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
	}

}