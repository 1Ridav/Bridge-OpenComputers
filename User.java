import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class User extends Thread {
	// /SERVICE VARIABLES
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	// /USABLE VARIABLES
	public User self;
	public User pair;
	public boolean isAndroid;
	public boolean isInitialized = false;
	public String KEY = "";
	

	public User(Socket s) throws IOException {
		socket = s;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));

		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
		this.start();
	}

	private void init() throws IOException, TooLargeTransferMessageException,
			SocketTimeoutException {
		String msg;
		String[] s;
		socket.setSoTimeout(5000);// 5 SECONDS TO INIT
		do {
			msg = receive().toString();
			if (msg.startsWith("K")) {
				KEY = msg.substring(4);
				if(KEY.length() == 0){
					throw new IOException();
				}
				if (msg.startsWith("KOC")) {
					isAndroid = false;
					socket.setSoTimeout(0);// OC WILL NOT BE PINGED
					break;
				} else if (msg.startsWith("KDR")) {
					isAndroid = true;
					socket.setSoTimeout(30000);// ANDROID WILL BE PINGED EVERY 30
												// SECONDS
					break;
				}
			}

		} while (true);
		isInitialized = true;
		Data.removeUnknownUser(self);
		Data.addUnpairedUser(self);
		send(Data.INIT_OK);
	}

	private void processCommand(String cmd) throws IOException {
		switch (cmd) {
		case Data.PING:
			send(Data.PONG);
			break;
		case Data.PONG:
			break;
		}
	}

	///connection main function
	public void run() {
		try {
			String msg = "";
			init();///GET KEY
			Bridge.findPair(self);

			do {
				if (isAndroid) {
					try {
						msg = receive().toString();
					} catch (SocketTimeoutException e) {
						try {
							send(Data.PING);// SEND PING REQUEST
							socket.setSoTimeout(5000);
							msg = receive().toString();
							socket.setSoTimeout(30000);
						} catch (SocketTimeoutException e1) {//LOG AND BREAK MAIN LOOP TO CLOSE CONNECTION
							System.out.println("Client timeout " + getDetailedInfo());
							break;
						}
					}
				} else {// OC DOES NOT NEED TO BE PINGED
					msg = receive().toString();
				}

				// IF COMMAND THEN DO NOT TRANSMIT
				if (msg.startsWith(Data.BRIDGE_COMMAND)) {
					processCommand(msg);
					continue;
				}

				// TRANSMISSION
				try {
					pair.send(msg);
				} catch (Exception e) {// /if failed to transmit to pair
					send(Data.FAILED_TO_TRANSMIT);//INFORM ABOUT TRANSMISSION FAIL
					onPairLost();
				}
			} while (true);
		} catch (SocketTimeoutException e1) {
			System.out.println("Init timeout " + getDetailedInfo());
		} catch (IOException e) {// /if receive return exception
			System.err.println("Socket disconnected " + getDetailedInfo());
		} catch (TooLargeTransferMessageException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			onDestroy();
			try {
				try {
					pair.send(Data.PAIR_LOST);
				} catch (Exception e) {

				}
				socket.close();
				in.close();
				out.close();
				// Main.onDestroy(this.self);
			} catch (IOException e) {
				System.err.println("Socket not closed");
			}
		}
	}

	public StringBuilder receive() throws IOException,
			TooLargeTransferMessageException {// /CALLED BY ME
		int tmp, len = 0;
		StringBuilder str = new StringBuilder();
		while ((tmp = in.read()) != 0) {
			if (tmp == -1) {
				throw new IOException();
			}
			len++;
			str.append((char) tmp);
			if (len > 30000)
				throw new TooLargeTransferMessageException();
		}
		if (Data.debugMode) {
			Data.log("DEBUG R:" + str);
		}
		return str;
	}

	public void send(String str) throws IOException {// CALLED BY PAIR TO
														// TRANSMIT
		if (Data.debugMode) {
			Data.log("DEBUG S:" + str);
		}

		out.write(String.valueOf(str) + "\0");
		out.flush();
	}

	public void onPairLost() {
		if (self.pair != null) {
			Data.moveToUnpaired(self);
			self.pair = null;
		}
		Bridge.findPair(self);
	}

	public void onPairFound(User u) {
		pair = u;
		try {
			send(Data.PAIR_FOUND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onDestroy() {
		try {
			self.pair.onPairLost();
		} catch (Exception e) {

		}
		Data.removePairedUser(self);
		Data.removeUnpairedUser(self);
		Data.removeUnknownUser(self);
	}

	public String getSocket() {
		return socket.toString();
	}

	public String getDetailedInfo() {
		String device;
		if(isInitialized){
			device = (isAndroid) ? "Android" : "OpenComputers";
		}else{
			device = "UNKNOWN";
		}
		return socket + " " + device + " " + KEY;
	}

	public boolean ping() {
		try {
			send(Data.PING);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

class TooLargeTransferMessageException extends Exception {
	public TooLargeTransferMessageException() {
	}

	public TooLargeTransferMessageException(String message) {
		super(message);
	}
}
