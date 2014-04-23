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

import model.DiscussionPost;
import model.Line;
import model.Page;
import request.DisplayRequest;
import request.PingRequest;
import request.PostRequest;
import request.ReadRequest;
import request.Request;
import response.DisplayResponse;
import response.InitialPush;
import response.PingResponse;
import response.PushNotification;
import response.ReadResponse;
import response.Response;

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
		} catch (IOException e) {
			System.err.println("Could not connect to designated server\n");
			return;
		}

		//vars push mode uses
		List<DiscussionPost> postDB = new ArrayList<DiscussionPost>();

		tcp.outToServer.writeBytes(userName + '\n');	//writes username to server for server log
		tcp.outToServer.writeBytes(mode + '\n');		//writes client operating mode, for server to process
		tcp.outToServer.flush();
		String reply = tcp.inFromServer.readLine();
		System.out.println(reply);

		//vars both modes use
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String clientRequest = "";
		PassByRef passByRef = new PassByRef();
		List<Integer> readPosts = new ArrayList<Integer>();
		Timer serverListener = new Timer();

		//Initialization finished
		//######################################################################
		System.out.printf("Operating in %s mode\n", mode);

		if (mode.equals("push")) {

			Object initialisation = tcp.objectsInFromServer.readObject();
			if (initialisation instanceof InitialPush) {
				postDB = ((InitialPush) initialisation).rebuild();
				System.out.println("downloading "+postDB.size()+" posts");
			}
			
			System.out.println("finished initialising posts db");
		}
		//after initialise for push, now can listen
		serverListener.schedule(new ServerListener(tcp, passByRef, postDB, readPosts, mode, userName, pollingInterval), 0);

		while ((clientRequest = inFromUser.readLine()) != null ) {					//loop for client input
			StringBuilder errorMessage = new StringBuilder();

			//request object is created with the input string
			Request request = createRequest(clientRequest, userName, passByRef.currentPage, errorMessage, readPosts);

			if (request == null) {
				//if input string is invalid, createRequest returns null
				System.err.println(errorMessage.toString());
			} else {
				if (mode.equals("push") && request instanceof ReadRequest) { // if reading posts in push mode
					Response response = ((ReadRequest) request).localProcess(postDB);
					System.out.println(response.toString());
				} else {
					//if request creation is successful, write and wait for response object
					tcp.writeToStream(request);
					//all returning objects are received by the serverListener thread
				}
			}
		}
	}

	private static class PassByRef {
		private Page currentPage = null;
	}

	private static class ServerListener extends TimerTask {

		private TCP tcp;
		private PassByRef passByRef;
		private List<DiscussionPost> postDB;
		private List<Integer> readPosts;
		private String mode;
		private String userName;
		private int pollingInterval;
		private Timer pingThread;

		private boolean pingNotNotified;

		private ServerListener (TCP tcp, PassByRef passByRef, List<DiscussionPost> postDB, List<Integer> readPosts, String mode, String userName, int pollingInterval) {
			this.tcp = tcp;
			this.passByRef = passByRef;
			this.postDB = postDB;
			this.readPosts = readPosts;
			this.mode = mode;
			this.userName = userName;
			this.pollingInterval = pollingInterval;
			this.pingThread = null;

			this.pingNotNotified = true;
		}

		@Override
		public void run() {
			Object response = null;
			try {
				while ((response = tcp.objectsInFromServer.readObject()) != null ) {
					if (response instanceof DisplayResponse) {
						//update the last visited page
						passByRef.currentPage = ((DisplayResponse) response).getPage();
						if (mode.equals("push")) {
							System.out.println(((DisplayResponse) response).toString(passByRef.currentPage.getBookName(), passByRef.currentPage.getPageNumber(), postDB, readPosts));
						}
						if (mode.equals("pull")) {
							System.out.println(((DisplayResponse) response).toString());
							if (pingThread != null) {
								pingThread.cancel();
							}
							pingThread = new Timer();
							pingThread.scheduleAtFixedRate(new Ping(tcp, userName, passByRef.currentPage.getBookName(), passByRef.currentPage.getPageNumber(), readPosts), pollingInterval * 1000, pollingInterval * 1000);
						}
					} 
					else if (response instanceof PushNotification) {
						((PushNotification) response).updateLocalDB(postDB, readPosts);
						if (this.passByRef.currentPage != null) {
							System.out.print(((PushNotification) response).toString(passByRef.currentPage.getBookName(), passByRef.currentPage.getPageNumber()));
						}
					}
					else if (response instanceof PingResponse) {
						if (((PingResponse) response).getStatus() == true && this.pingNotNotified) {
							System.out.println(response.toString());
							this.pingNotNotified = false;
						}
						else if (((PingResponse) response).getStatus() == false ) {
							this.pingNotNotified = true;
						}
					} else {
						//any errors on server side would return response object messageResponse with error description
						System.out.println(response.toString());
					}
				}
			} catch (SocketException e) {
				System.err.print("Server disconnected\r");
				System.exit(0);
			} catch (EOFException e) {
				System.err.print("Server disconnected\r");
				System.exit(0);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static class Ping extends TimerTask {

		private TCP tcp;
		private String userName;
		private String bookName;
		private int pageNumber;
		private List<Integer> readPosts;

		private Ping (TCP tcp, String userName, String bookName, int pageNumber, List<Integer> readPosts) {
			this.tcp = tcp;
			this.userName = userName;
			this.bookName = bookName;
			this.pageNumber = pageNumber;
			this.readPosts = readPosts;
		}

		@Override
		public void run() {
			try {
				tcp.writeToStream(new PingRequest(this.userName + " pings " + this.bookName + " " + this.pageNumber, this.userName, this.bookName, this.pageNumber, this.readPosts));
				//the ping response is received by the server listener thread
			} catch (SocketException e) {
				System.err.print("Server disconnected\r");
				System.exit(0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		
		private synchronized void writeToStream (Object request) throws IOException {
			this.objectsOutToServer.writeObject(request);
			this.objectsOutToServer.flush();
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
					request = new DisplayRequest(userName+" requests a page", userName, clientRequest.split(" ")[1], Integer.parseInt(clientRequest.split(" ")[2]), readPosts);
				}
			}
			else if (clientRequest.startsWith("post_to_forum")) {
				if (mostRecentPage == null) {
					errorMessage.append("Please go to a page first before you post to it\n");
				}
				else if (clientRequest.split(" ").length < 3) {
					errorMessage.append("Usage: post_to_forum [lineNumber] [content]\n");
				} else {
					request = new PostRequest(userName+" submits new post", userName, mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), clientRequest.split(" ")[1], clientRequest.replaceAll("^\\w* \\w* ", ""));
				}
			} 
			else if (clientRequest.startsWith("read_post")) {
				if (mostRecentPage == null) {
					errorMessage.append("Please go to a page first before you post to it\n");
				}
				else if (clientRequest.split(" ").length < 2) {
					errorMessage.append("Usage: read_post [lineNumber]\n");
				} else {
					request = new ReadRequest(userName+" read posts", userName, mostRecentPage.getBookName(), mostRecentPage.getPageNumber(), clientRequest.split(" ")[1], readPosts);
				}
			} else {
				errorMessage.append("Usage: (display [bookName] [pageNumber] | post_to_forum [lineNumber] [content])\n");
			}
		} catch (NumberFormatException nfe) {
			errorMessage.append("invalid Number inputed somewhere!\n");
		}
		return request;
	}
}