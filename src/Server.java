


import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Vector;

public class server {

	public static void main(String[] args)throws Exception {

		// see if we do not use default server port
		int serverPort = 6789; 
		/* change above port number this if required */

		if (args.length >= 1)
			serverPort = Integer.parseInt(args[0]);
		try {
			ServerSocket welcomeSocket = new ServerSocket(serverPort);

			System.out.printf("The server is listening on port number [%d]\n", welcomeSocket.getLocalPort());
			//after serverport is open, then initialise
			Ebook_db db = new Ebook_db();
			List<TCP> pushClients = new Vector<TCP>();

			while (true){
				new TCP(welcomeSocket.accept(), db, pushClients).start();
			}
		} catch (BindException be) {
			System.err.println("Could not open server on port: "+serverPort+", possibly in use!");
		}
	}

	private static class TCP extends Thread{

		private Socket connectionSocket;
		private BufferedReader inFromClient;
		private DataOutputStream outToClient;
		private ObjectOutputStream objectsOutToClient;
		private ObjectInputStream objectsInFromClient;
		private volatile Ebook_db db;
		private volatile List<TCP> pushClients;
		private String userName;
		private String mode;

		private TCP (Socket connectionSocket, Ebook_db db, List<TCP> pushClients) throws IOException {
			this.connectionSocket = connectionSocket;
			this.inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
			this.outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());
			this.objectsOutToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
			this.objectsInFromClient = new ObjectInputStream(connectionSocket.getInputStream());
			this.db = db;
			this.pushClients = pushClients;
			this.userName = null;
			this.mode = null;
		}

		public void run () {
			//new thread runs here
			try {
				this.userName = this.inFromClient.readLine();		//collect userName and print for log
				this.mode = this.inFromClient.readLine();			//collect client mode to process 
				System.out.println("User " + userName + " connected in " + mode +" mode");
				this.outToClient.writeBytes("connection established\n");
				this.outToClient.flush();
				if (mode.equals("push")) {
					System.out.println(this.userName+" has been added to the push list");
					this.pushClients.add(this);//add this client to the push list
					//if push mode, must download all posts, assume they have no posts at all
					int pushSize = db.getAllDiscussionPosts().size();
					this.outToClient.writeBytes(pushSize+"\n");	//send the number of posts over
					for (DiscussionPost post : db.getAllDiscussionPosts()) {
						String data = post.getSerialID() + "|" + post.getUserName() + "|" + post.getBookName() + "|" + post.getPageNumber() + "|" + post.getLineNumber() + "|" + post.getPost();
						this.outToClient.writeBytes(data+"\n");	//send each posts as a string, and rebuild on the other side
					}
					System.out.println("Finished uploading");
				}
			} catch (IOException e) {
				System.err.println("Failed to communicate data with client. Closing connection\n");
				return;
			}


			while (this.connectionSocket.isConnected()) {
				//loops until socket is closed
				try {
					Object request = this.objectsInFromClient.readObject();

					if (request instanceof Request) {	//if client works properly, all objects should be requests
						Response response = null;

						//server shows client activity
						System.out.println(((Request) request).getCommand());

						//requests process on their own and create their own response object
						response = ((Request) request).process(db, mode);

						//reply to client with the response
						this.objectsOutToClient.writeObject(response);
						this.objectsOutToClient.flush();

						if (request instanceof PostRequest && response.toString().equals("Post Successful")) {
							if (pushClients.size() == 0) {
								System.out.println("Push list empty. No action required.");
							} else {
								System.out.println("Pushing posts onto post clients");
								for (TCP tcp : pushClients) {
									System.out.println("Pushing to: " + tcp.userName);
									tcp.objectsOutToClient.writeObject(new PushNotification(db.getMostRecentPost()));
									tcp.objectsOutToClient.flush();
								}
							}
						}

					} else {
						System.err.println("Request is not a valid request");
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println(userName + " Disconnected");
					if (mode.equals("push")) {
						this.pushClients.remove(this);
						System.out.println("Removing this client from push list");
					}
					close();
					return;
				} catch (Exception e) {
					if (mode.equals("push")) {
						this.pushClients.remove(this);
						System.out.println("Removing this client from push list");
					}
				}
			}
		}

		private void close() {
			try {
				this.outToClient.close();
				this.inFromClient.close();
				this.objectsOutToClient.close();
				this.objectsInFromClient.close();
				this.connectionSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}