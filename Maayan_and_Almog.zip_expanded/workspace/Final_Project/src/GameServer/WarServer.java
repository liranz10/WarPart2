package GameServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class WarServer implements Runnable {
    @JsonIgnore
	private ArrayList<WarListener> listeners = new ArrayList<>();

	@Override
	public void run() {
		try {
			final ServerSocket server = new ServerSocket(7000);
			System.out.println("Server waits for clients...");

			while (true) {
				final Socket socket = server.accept(); // blocking

				new Thread(new Runnable() {
					@Override
					public void run() {
						String clientAddress = "";
						try {
							clientAddress = socket.getInetAddress() + ":" + socket.getPort();
							System.out.println("Client connected from " + clientAddress);
							  BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							String line = "";
							while (!line.equals("quit")) {
								line = br.readLine();
								notifyAllWarListener(line);
							}
						} catch (IOException e) {
							System.err.println(e);
						} finally {
							try {
								socket.close();
								server.close();
								System.out.println("The client from " + clientAddress + " is disconnected");
							} catch (IOException e) { // log and ignore
							}
						}
					} // run
				}).start();
			}
		} catch (IOException e) {
			// TODO: handle exception
		}
	}

	public void registerListener(WarListener newListener) {
		listeners.add(newListener);
	}

	public void notifyAllWarListener(String operation) {

		if (operation.equals("addMissileLauncher")) {
			for (WarListener warListener : listeners) {
				warListener.addMissileLauncherEvent();
			}
		} else if (operation.equals("launchMissile")) {
			for (WarListener warListener : listeners) {
				warListener.launchMissileEvent();
			}
		} else if (operation.equals("destructMissile")) {
			for (WarListener warListener : listeners) {
				warListener.destructMissileEvent();
			}
		}

	}

	

}
