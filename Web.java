import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;


public class Web extends Thread{

	public Web() {
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
		this.start();
	}
	
	public void run(){
		ServerSocket ss = null;
		Socket socket = null;
		try {
			ss = new ServerSocket(Data.WEB_PORT);
			System.out.println("WEBServer Started on port " + Data.WEB_PORT);
			
			while (true) {
				try{
					socket = ss.accept();
					String myText = "<!doctype html><html><head><title>BRIDGE WEB INFO</title></head><body><p>INFO:</p><ul>	<li>Threads: " + Thread.activeCount() + "</li></ul><p>NETWORK:</p><ul>	<li>Connections: " + (Data.pairedUsers.size() + Data.unpairedUsers.size() + Data.unknownUsers.size()) + "</li>	<li>Paired: " + Data.pairedUsers.size() + "</li>	<li>Unpaired: " + Data.unpairedUsers.size() + "</li></ul><p>Version: " + Data.VERSION + "</p><p>Timestamp millis: " + System.currentTimeMillis() + "</p></body></html>";

					String response = "HTTP/1.1 200 OK\r\n" +
			                    "Server: bridge/2016-01-19\r\n" +
			                    "Content-Type: text/html\r\n" +
			                    "Content-Length: " + myText.length() + "\r\n" +
			                    "Connection: close\r\n\r\n";
			        String result = response + myText;
			        socket.getOutputStream().write(result.getBytes());
			        socket.getOutputStream().flush();
			        socket.close();
					} catch (Exception e) {
						socket.close();
					}
				}
			
		} catch (IOException e1) {
		}finally{
			try {
				ss.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
