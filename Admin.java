import java.io.IOException;
import org.json.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Admin extends Thread {

	public Admin() {
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
		this.start();
	}

	public void run() {
		Scanner keyboard = new Scanner(System.in);
		String cmd[];
		Runtime runtime = Runtime.getRuntime();
		do {
			cmd = keyboard.nextLine().split(" ");
			try {
				switch (cmd[0]) {
				case "info":
					Data.log("Threads " + Thread.activeCount());
					Data.log("Allocated mem: " + runtime.totalMemory() / 1024
							+ " KB");
					Data.log("Free mem: " + runtime.freeMemory() / 1024 + " KB");
					break;
				case "list":
					list(cmd);
					break;
				case "stop":
					Data.log("Broadcasting about server shutdown");
					broadcast(Data.pairedUsers, Data.BRIDGE_STOP);
					broadcast(Data.unpairedUsers, Data.BRIDGE_STOP);
					System.exit(0);
					break;
				case "broadcast":

					if (cmd[1].equals("android")) {
						JSONObject json = new JSONObject();
						System.out.println("Blinking title: ");
						json.put("blinkTitle", keyboard.nextLine());
						System.out.println("title: ");
						json.put("title", keyboard.nextLine());
						System.out.println("text: ");
						json.put("text", keyboard.nextLine());
						System.out.println("urgent(true/null): ");
						json.put("urgent", keyboard.nextLine());
						System.out.println("id(int >= 2): ");
						json.put("id", keyboard.nextLine());

						String str = Data.DEVICE_COMMAND + "31 "
								+ json.toString();
						Data.log("Broadcasting \"" + json.toString() + "\"");
						broadcast_android(Data.pairedUsers, str);
						broadcast_android(Data.unpairedUsers, str);

					} else if (cmd[1].equals("opencomputers")) {

					}
					break;
				case "gc":
					for (User u : Data.pairedUsers)
						if (!u.ping())
							Data.removePairedUser(u);

					for (User u : Data.unpairedUsers)
						if (!u.ping())
							Data.removeUnpairedUser(u);

					for (User u : Data.unknownUsers)
						if (!u.ping())
							Data.removeUnknownUser(u);

					System.gc();
					Data.log("Freed");
					break;
				case "debug":
					Data.debugMode = !Data.debugMode;
					System.out.println("\tDEBUG MODE "
							+ (Data.debugMode == true ? "ON" : "OFF"));
					break;
				case "help":
					Data.log("info\n   list\n\tlist paired\n\tlist unpaired\n\tlist unknown\n   \n   broadcast\n\tbroadcast android\n\tbroadcast opencomputers\n   stop\n   gc\n   clear");
					break;
				case "clear":
					System.out
							.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
					break;
				case "version":
					Data.log(Data.VERSION);
					break;
				default:
					Data.log("INVALID COMMAND");
				}
			} catch (Exception e) {

			}
		} while (true);
	}

	private void broadcast(ArrayList<User> users, String text) {
		for (User u : users) {
			try {
				u.send(text);
			} catch (IOException e) {
				Data.log("Failed to send to " + u.getSocket());
			}

		}
	}

	private void broadcast_android(ArrayList<User> users, String text) {
		for (User u : users) {
			if (u.isAndroid) {
				try {
					u.send(text);
				} catch (IOException e) {
					Data.log("Failed to send to " + u.getSocket());
				}
			}
		}
	}

	private void list(String cmd[]) {

		int i = 0;
		if (cmd.length > 1) {
			if (cmd[1].equals("paired")) {
				for (User u : Data.pairedUsers) {
					Data.log(i + " " + u.getSocket() + " KEY: " + u.KEY
							+ " Ping " + u.ping());
				}
			} else if (cmd[1].equals("unpaired")) {
				for (User u : Data.unpairedUsers) {
					Data.log(u.getSocket() + " KEY: " + u.KEY + " Ping "
							+ u.ping());
				}
			} else if (cmd[1].equals("unknown")) {
				for (User u : Data.unknownUsers) {
					Data.log(u.getSocket() + " Ping " + u.ping());
				}
			}
		} else {
			Data.log("Paired " + Data.pairedUsers.size());
			Data.log("Unpaired " + Data.unpairedUsers.size());
			Data.log("Unknown " + Data.unknownUsers.size());
		}
	}

}
