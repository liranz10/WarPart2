package BL;

import java.util.*;

import Entities.Missile;
import Entities.War;
import Interfaces.WarGameInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static Entities.War.create;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "war"
})
@SuppressWarnings("deprecation")
public class ConsoleWarGameImpl implements WarGameInterface {

    @JsonProperty("war")
    private War war;
    @JsonIgnore
    private Scanner scanner;

    public ConsoleWarGameImpl() {
        scanner = new Scanner(System.in);
        this.war = create(scanner, true);
        war.startGame((o, arg) -> {});
    }

    @Override
    public void addMissileLauncher() {
        war.addMissileLauncher();
    }

    @Override
    public void addMissileLauncherDestructor() {
        war.addMissileLauncherDestructor();
    }

    @Override
    public void addMissileDestructor() {
        war.addMissileDestructor();
    }

    @Override
    public void launchAMissile(String destination) {
        war.launchAMissile(destination);
    }

    @Override
    public void destructAMissile() {
        war.destructAMissile();
    }

    @Override
    public void destructAMissileLauncher() {
        war.destructAMissileLauncher();
    }

    @Override
    public void showStatistics() {
        war.stdOut.println(war.getWarInformation().toString());
    }

    public void menu() {
        final int MIN_MENU_INDEX = 1;
        final int MAX_MENU_INDEX = 8;
        boolean finished = false;

        int choice = -1;

        while (!finished) {
            System.out.println("1. Add missile launcher destructor\n2. Add missile destructor\n" +
                    "3. Add missile launcher\n4. Launch a missile\n5. Destruct a missile launcher" +
                    "\n6. Destruct a missile\n7. Show statistics\n8. Exit");

            while (choice < MIN_MENU_INDEX || choice > MAX_MENU_INDEX) {

                try {
                    choice = scanner.nextInt();

                    if (choice < MIN_MENU_INDEX || choice > MAX_MENU_INDEX) {
                        System.out.println("Input out of range, please try again");
                        choice = -1;
                    }
                }
                catch (InputMismatchException e) {
                    System.out.println("Input should be an integer");
                    choice = -1;
                }
                catch (NoSuchElementException e) {
                    System.out.println("Test");
                }
            }

            switch (choice) {
                case 1:
                    addMissileLauncherDestructor();
                    break;
                case 2:
                    addMissileDestructor();
                    break;
                case 3:
                    addMissileLauncher();
                    break;
                case 4:
                    System.out.println("Enter destination: ");
                    scanner.nextLine();
                    String destination = scanner.nextLine();
                    launchAMissile(destination);
                    break;
                case 5:
                    destructAMissileLauncher();
                    break;
                case 6:
                    destructAMissile();
                    break;
                case 7:
                    showStatistics();
                    break;
                case 8:
                    System.out.println("The game is over !\nBye Bye");
                    war.setGameOver(true);
                    finished = true;
                    break;
            }

            choice = -1;

        }
    }
}
