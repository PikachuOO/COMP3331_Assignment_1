

import java.io.*;
import java.net.*;

public class Server {

	public static void main(String[] args)throws Exception {

		Ebook_db db = new Ebook_db();

		// see if we do not use default server port
		int serverPort = 6789; 
		/* change above port number this if required */

		if (args.length >= 1)
			serverPort = Integer.parseInt(args[0]);
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
			
			try {
				String userName = this.inFromClient.readLine();
				System.out.println("User " + userName + " connected");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			while (this.connectionSocket.isConnected()) {
				try {
					Object request = this.objectsInFromClient.readObject();
					
					if (request instanceof Request) {
						Response response = null;
						if (request instanceof DisplayRequest) {
							response = ((Request) request).process(db);
						}
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

		private void write(String data) throws IOException {
			this.outToClient.writeBytes(data + '\n');
		}
	}


} // end of class TCPServer