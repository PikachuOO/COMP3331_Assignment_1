package network;


import java.io.*;
import java.net.*;

import model.Ebook_db;
import model.Line;
import model.Page;
import request.DisplayRequest;
import request.PostRequest;
import request.ReadRequest;
import request.Request;
import response.Response;

public class Server {

	public static void main(String[] args)throws Exception {

		Ebook_db db = new Ebook_db();

		// see if we do not use default server port
		int serverPort = 6789; 
		/* change above port number this if required */

		if (args.length >= 1)
			serverPort = Integer.parseInt(args[0]);
		@SuppressWarnings("resource")
		ServerSocket welcomeSocket = new ServerSocket(serverPort);

		while (true){
			new TCP(welcomeSocket.accept(), db).start();
		}
	}

	private static class TCP extends Thread{

		private Socket connectionSocket;
		private BufferedReader inFromClient;
		private DataOutputStream outToClient;
		private ObjectOutputStream objectsOutToClient;
		private ObjectInputStream objectsInFromClient;
		private Ebook_db db;

		private TCP (Socket connectionSocket, Ebook_db db) throws IOException {
			this.connectionSocket = connectionSocket;
			this.inFromClient = new BufferedReader(new InputStreamReader(this.connectionSocket.getInputStream()));
			this.outToClient = new DataOutputStream(this.connectionSocket.getOutputStream());
			this.objectsOutToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
			this.objectsInFromClient = new ObjectInputStream(connectionSocket.getInputStream());
			this.db = db;
		}

		public void run () {
			//new thread runs here

			try {
				//collect userName and print for log
				String userName = this.inFromClient.readLine();
				System.out.println("User " + userName + " connected");
				this.outToClient.writeBytes("connection established\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			while (this.connectionSocket.isConnected()) {
				//loops until socket is closed
				try {
					Object request = this.objectsInFromClient.readObject();

					if (request instanceof Request) {	//if client works properly, all objects should be requests
						Response response = null;
						
						//server shows client activity
						System.out.print("User " + ((Request) request).getUserName() + " requests: ");
						System.out.println(((Request) request).getCommand());
						
						//requests process on their own and create their own response object
						response = ((Request) request).process(db);
						
						//reply to client with the response
						this.objectsOutToClient.writeObject(response);
						this.objectsOutToClient.flush();
					} else {
						System.err.println("Request is not a valid request");
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					System.out.println("Socket Disconnected");
					close();
					return;
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