package Entities;

import BL.GUIWarGameImpl;
import DAL.SqlDataService;
import Interfaces.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Observer;
import java.util.Scanner;

public class War implements MissileDestructorWithoutObjectInterface, MissileLauncherInterface, MissileLauncherDestructorWithoutObjectInterface, StartGameWithoutWarInterface {

    @JsonIgnore
    public static long GAME_START_TIME;
    @JsonIgnore
    public static final long MILLISECOND_IN_SECOND = 1000;
    @JsonIgnore
    private static boolean gameOver = false;
    @JsonIgnore
    private static boolean consoleGame = false;
    @JsonIgnore
    private WarInformation warInformation = new WarInformation();
    @JsonProperty("missileLaunchers")
    private MissileLaunchers missileLaunchers = new MissileLaunchers();
    @JsonProperty("missileDestructors")
    private MissileDestructors missileDestructors = new MissileDestructors();
    @JsonProperty("missileLauncherDestructors")
    private MissileLauncherDestructors missileLauncherDestructors = new MissileLauncherDestructors();
    @JsonIgnore
    public static PrintStream stdOut = new PrintStream(System.out);


    private War() { }

    public static War loadWarGameFromJsonFile(String filename) {
        ObjectMapper mapper = new ObjectMapper();
        War war;

        try {
            war = mapper.readValue(new File(filename), War.class);
        }
        catch (IOException e) {
            war = new War();
        }

        return war;
    }

    public static War create(Scanner s, boolean isConsoleGame) {

        War war;

        consoleGame = isConsoleGame;

        System.out.println("Would you like to load the game properties from a file? (yes/no)");
        String shouldLoadFromFile = s.nextLine();

        if (shouldLoadFromFile.equalsIgnoreCase("yes")) {
            war = loadWarGameFromJsonFile("C:\\Users\\Liran\\WarGamePart2Final\\Maayan_and_Almog.zip_expanded\\workspace\\Final_Project\\properties.json");
            for (Launcher launcher : war.missileLaunchers.getLauncher()) {
				SqlDataService.getInstance().saveMissileLauncher(launcher.getId(), launcher.getIsHidden());
				for (Missile missile : launcher.getMissile()) {
					SqlDataService.getInstance().saveMissileLauncherMissile(missile.getId(),missile.getDestination(),missile.getLaunchTime(),missile.getFlyTime(),missile.getDamage(),launcher.getId());
				}
			}
            
            for (Destructor_ destructor_ : war.missileLauncherDestructors.getDestructor()) {
				SqlDataService.getInstance().saveMissileLauncherDestructor(destructor_.getType());
					for (DestructedLauncher destructedLauncher : destructor_.getDestructedLauncher()) {
						SqlDataService.getInstance().saveDestructedLauncher(destructedLauncher.getId(),destructor_.getType(),destructedLauncher.getDestructTime());
					}
			}
            for (Destructor destructor : war.missileDestructors.getDestructor()) {
				SqlDataService.getInstance().saveMissileDestructor(destructor.getId());
				for (DestructedMissile destructedMissile : destructor.getDestructedMissile()) {
					SqlDataService.getInstance().saveDestructedMissile(destructor.getId(),destructedMissile.getId(),destructedMissile.getDestructAfterLaunch());
				}
			}
        }
        else {
            war = new War();
        }

        return war;
    }

    public static boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public static boolean isConsoleGame() {
        return consoleGame;
    }

    @JsonProperty("warInformation")
    public WarInformation getWarInformation() {
        return warInformation;
    }

    @JsonProperty("missileLaunchers")
    public MissileLaunchers getMissileLaunchers() {
        return missileLaunchers;
    }

    @JsonProperty("missileLaunchers")
    public void setMissileLaunchers(MissileLaunchers missileLaunchers) {
        this.missileLaunchers = missileLaunchers;
    }

    @JsonProperty("missileDestructors")
    public MissileDestructors getMissileDestructors() {
        return missileDestructors;
    }

    @JsonProperty("missileDestructors")
    public void setMissileDestructors(MissileDestructors missileDestructors) {
        this.missileDestructors = missileDestructors;
    }

    @JsonProperty("missileLauncherDestructors")
    public MissileLauncherDestructors getMissileLauncherDestructors() {
        return missileLauncherDestructors;
    }

    @JsonProperty("missileLauncherDestructors")
    public void setMissileLauncherDestructors(MissileLauncherDestructors missileLauncherDestructors) {
        this.missileLauncherDestructors = missileLauncherDestructors;
    }

    @Override
    public void addMissileLauncher() {
        missileLaunchers.addMissileLauncher();
    }

    @Override
    public void addMissileLauncherDestructor() {
        missileLauncherDestructors.addMissileLauncherDestructor();
    }

    @Override
    public void addMissileDestructor() {
        missileDestructors.addMissileDestructor();
    }

    @Override
    public void launchAMissile(String destination) {
        missileLaunchers.launchAMissle(destination);
    }

    @Override
    public void destructAMissile() {
        missileDestructors.destructAMissile(missileLaunchers);
    }

    @Override
    public void destructAMissileLauncher() {
        missileLauncherDestructors.destructAMissileLauncher(missileLaunchers);
    }

    public static long timeSinceGameStartedInSeconds() {
        return (System.currentTimeMillis() - War.GAME_START_TIME) / War.MILLISECOND_IN_SECOND;
    }

    public static void setStartGameTime() {
        GAME_START_TIME = System.currentTimeMillis();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void startGame(Observer observer) {
        setStartGameTime();
        missileLaunchers.startGame(observer, this);
        missileDestructors.startGame(observer, this);
        missileLauncherDestructors.startGame(observer, this);
    }
}
