/*	
 * 
 * Developed by 1Ridav. 
 *  Published under GNU General Public Licence 3.0
 * 
 * Visit our web site http://computercraft.ru
 * 
 * 
 * 
 * */


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Bridge {

	public static void main(String[] args) throws IOException {
		if(args.length > 0){
			Data.PORT = Integer.parseInt(args[0]);
			if(args.length > 1){
				Data.WEB_PORT = Integer.parseInt(args[1]);
			}
		}
		
		new Bridge();
	}

	public Bridge() {
		startServer();
	}

	private void startServer() {
		ServerSocket ss = null;
		Socket socket = null;
		User u = null;
		try {
			ss = new ServerSocket(Data.PORT);
			System.out.println("Server Started on port " + Data.PORT);
			new Admin();
			new Web();
			
				while (Data.acceptConnections) {
					try{
						socket = ss.accept();
						u = new User(socket);
						u.self = u;
						Data.addUnknownUser(u);// add to pool of unpaired
													// connections
					} catch (IOException e) {
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
	
	public static synchronized boolean findPair(User self){
		for(User u : Data.unpairedUsers){
			if(u.KEY.equals(self.KEY)){
				//if(u.isAndroid != self.isAndroid){ //CONNECT ONLY ANDROID TO OC
				if(u != self){ //CONNECT ANY DEVICES TO EACH OTHER
					u.onPairFound(self);
					self.onPairFound(u);
					
					Data.moveToPaired(u);
					Data.moveToPaired(self);
					
					return true;
				}
			}
		}
		return false;
	}
}
