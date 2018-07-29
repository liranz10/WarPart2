import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class WarClient implements WarClientIface {
	private Socket socket = null;
	private PrintStream toNetOutputStream;
	private Scanner scanner;
	private void StartClient() {
		try {
			socket = new Socket("localhost", 7000);
			System.out.println(" Connected to server at "
					+ socket.getLocalAddress() + ":" + socket.getLocalPort());
			toNetOutputStream = new PrintStream(socket.getOutputStream());

			 scanner = new Scanner(System.in);
			while (true) {
				printMenu();
				int choice = scanner.nextInt();
				switch (choice) {
				case 1:
					addMissileLauncher();
					break;
				case 2:
					launchMissile();
					break;
				case 3:
					destructMissile();
					break;
				case 4:
					quit();
					break;
				default:
					System.out.println("Invalid Choice");
				}


			}
		} catch (Exception e) {	System.err.println(e);
		} finally {
			quit();
		}
	}

	private void printMenu() {
		System.out.println("Select War Action:(1-3)");
		System.out.println("1 - Add Missile Launcher");
		System.out.println("2 - Launch Missile");
		System.out.println("3 - Destruct Missile");
		System.out.println("4 - Quit");



	}

	@Override
	public void addMissileLauncher() {
		toNetOutputStream.println("addMissileLauncher");

	}

	@Override
	public void launchMissile() {
		System.out.println("Enter Destination:");
		String dest = scanner.next();
		toNetOutputStream.println("launchMissile#"+dest);
		
	}

	@Override
	public void destructMissile() {
		toNetOutputStream.println("destructMissile");


	}

	@Override
	public void quit() {
		try {
			socket.close();
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		WarClient client = new WarClient();
		client.StartClient();
	}
}
