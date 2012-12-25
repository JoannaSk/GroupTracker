package pl.grouptracker.network;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

import pl.grouptracker.NameAndLocation;

import android.util.Log;

public class AndroidClient implements Runnable {
	private Socket serverSock;
	private BlockingQueue<NameAndLocation> queue;
	private BufferedReader reader;
	private Semaphore sem;

	public AndroidClient(BlockingQueue<NameAndLocation> queue_) {
		queue = queue_;
		sem = new Semaphore(0);
	}
	
	@Override
	public void run() {
		try {
			serverSock = new Socket("192.168.1.103", 1234);
			InputStreamReader isReader = new InputStreamReader(serverSock.getInputStream());
			reader = new BufferedReader(isReader);
			sem.release();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (true) {
			try {
				NameAndLocation nLoc = queue.take();
				sendToServerLocationAndName(nLoc.getLongitude(), nLoc.getLatitude(), nLoc.getName());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendToServerLocationAndName(int longitude, int latitude, String name) {
		try {
			
			DataOutputStream out = new DataOutputStream(serverSock.getOutputStream());
			String stringLongitude = Integer.toString(longitude);
			String stringLatitude = Integer.toString(latitude);
			
				out.writeBytes(name);
				out.writeBytes(" ");
				out.writeBytes(stringLongitude);
				out.writeBytes(" ");
				out.writeBytes(stringLatitude);
				out.writeBytes("\n");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public NameAndLocation getNewLocationAndName() throws InterruptedException, IOException {
		
		sem.acquire();
		NameAndLocation newNameAndLocation = null;
		int longit;
		int latit;
		try {
			//char[] tab = new char[10];
			//reader.read(tab);
			String received = reader.readLine();
			Log.d("getNewLocAndName", "przyszlo: " + received);
			//byte[] tab = received.getBytes();
			//Log.d("getNewLocAndName", "rozmiar tablicy: " + tab.length);
			//for (int i = 0; i < tab.length; ++i)
			//	Log.d("getNewLocAndName", "bajt " + i + " = " + (int)tab[i]);
			String words[] = received.split(" ");
			longit = (int)Long.parseLong(words[1]);
			latit = (int)Long.parseLong(words[2]);
			newNameAndLocation = new NameAndLocation(words[0], longit, latit);
			Log.d("getNewLocAndName", "wczytano: " + words[0] + " " + longit + " " + latit);
			//throw new IOException();
		} catch (IOException e) {
			throw e;
		} finally {
			sem.release();
		}
		return newNameAndLocation;
	}
}
