import java.util.Scanner;
import BL.ConsoleWarGameImpl;
import BL.GUIWarGameImpl;
import Interfaces.WarGameInterface;
import javafx.application.Application;

public class Main {

    public static void mainMenu(String args[]) {
        WarGameInterface warGame = null;

        try (Scanner s = new Scanner(System.in)) {

            System.out.println("Would you like use GUI game mode? (yes/no)");

            String isGUIGame = s.nextLine();

            if (isGUIGame.equalsIgnoreCase("yes")) {
                Application.launch(GUIWarGameImpl.class, args);
            }
            else {
                warGame = new ConsoleWarGameImpl();
                ((ConsoleWarGameImpl)warGame).menu();
            }
        }
    }

    public static void main(String[] args) {
        mainMenu(args);
    }

}
