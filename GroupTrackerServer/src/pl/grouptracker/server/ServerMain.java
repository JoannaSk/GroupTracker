package pl.grouptracker.server;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
public class ServerMain {
	
	ArrayList<DataOutputStream> clientOutputStreams;
	Map<String, DwaStringi> myMap;
	
	public ServerMain() {
		myMap = new TreeMap<String, DwaStringi>();
	}
	
	public class ClientHandler implements Runnable {
		BufferedReader reader;
		Socket sock;
		public ClientHandler(Socket clientSocket) {
			try {
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
				
			} catch (Exception ex) {ex.printStackTrace();}
		}
		public void run() {
			System.out.println("Nowy watek!");
			String nameAndLocation;
			try {
				while((nameAndLocation = reader.readLine()) != null) {
					System.out.println("Dostalem " + nameAndLocation);
					sendEveryone(nameAndLocation);
				}
			} catch(Exception ex) {ex.printStackTrace();}
			
		}
	}
	
	public void go(){
		clientOutputStreams = new ArrayList<DataOutputStream>();
		try {
			ServerSocket serverSock = new ServerSocket(1234);
			while(true) {
				Socket clientSocket = serverSock.accept();
				DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
				clientOutputStreams.add(out);
				
				Thread t = new Thread(new ClientHandler(clientSocket));
				t.start();
			}
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public class DwaStringi {
		String longitude;
		String latitude;
		public DwaStringi (String longi, String lati){
			longitude = longi;
			latitude = lati;
		}
		public String toString() {
			return longitude + " " + latitude;
		}
	}
	public void sendEveryone(String nameAndLocation) {
		String[] words = nameAndLocation.split(" ");
		DwaStringi longAndLat = new DwaStringi(words[1], words[2]);
		myMap.put(words[0], longAndLat);
		Iterator<DataOutputStream> it = clientOutputStreams.iterator();
		while(it.hasNext()) {
			try {
				DataOutputStream out = it.next();
				for(Entry<String, DwaStringi> entry : myMap.entrySet()) {
					String s = entry.getKey() + " " + entry.getValue().toString();
					System.out.println("Wysylam do wszystkich: " + s);
					out.writeChars(s);
					out.writeChar('\n');
					out.flush();
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	public static void main (String[] args) {
		new ServerMain().go();
	}
	
}
