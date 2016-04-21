import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Data {
	static String VERSION = "0.7.94";
	static int PORT = 1111;
	static int WEB_PORT = 1112;
	static ArrayList<User> unknownUsers = new ArrayList();
	static ArrayList<User> unpairedUsers = new ArrayList();
	static ArrayList<User> pairedUsers = new ArrayList();
	static boolean acceptConnections = true;
	static boolean debugMode = false;
	static int numOfConnections = 0;

	static final Lock unknownMutex = new ReentrantLock(true);
	static final Lock unpairedMutex = new ReentrantLock(true);
	static final Lock pairedMutex = new ReentrantLock(true);

	static final public String BRIDGE_COMMAND = "+#";
	static final public String DEVICE_COMMAND = "+$";
	static final public String PING = BRIDGE_COMMAND + "00";
	static final public String PONG = BRIDGE_COMMAND + "01";
	static final public String BRIDGE_STOP = BRIDGE_COMMAND + "02";
	static final public String INIT_OK = BRIDGE_COMMAND + "03";
	static final public String FAILED_TO_TRANSMIT = BRIDGE_COMMAND + "10";
	static final public String PAIR_FOUND = BRIDGE_COMMAND + "20";
	static final public String PAIR_LOST = BRIDGE_COMMAND + "21";

	public Data() {

	}

	public static void log(String str) {
		System.out.println("   " + str);
	}

	static public void addUnknownUser(User u) {
		unknownMutex.lock();
		unknownUsers.add(u);
		unknownMutex.unlock();
	}

	static public void removeUnknownUser(User u) {
		try {
			unknownMutex.lock();
			unknownUsers.remove(u);
			unknownMutex.unlock();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void addUnpairedUser(User u) {
		unpairedMutex.lock();
		unpairedUsers.add(u);
		unpairedMutex.unlock();
	}

	static public void removeUnpairedUser(User u) {
		try {
			unpairedMutex.lock();
			unpairedUsers.remove(u);
			unpairedMutex.unlock();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void addPairedUser(User u) {
		pairedMutex.lock();
		pairedUsers.add(u);
		pairedMutex.unlock();
	}

	static public void removePairedUser(User u) {
		try {
			pairedMutex.lock();
			pairedUsers.remove(u);
			pairedMutex.unlock();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void moveToUnpaired(User u) {
		try {
			removePairedUser(u);
			addUnpairedUser(u);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public void moveToPaired(User u) {
		try {
			removeUnpairedUser(u);
			addPairedUser(u);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
