package Entities;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import BL.GUIWarGameImpl;
import DAL.IDataService;
import DAL.MongoDataService;
import Interfaces.LoggerSetupInterface;
import Interfaces.StartGameWithWarInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.aspectj.lang.annotation.Aspect;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "destructedLauncher"
})
@SuppressWarnings("deprecation")
public class Destructor_ extends Observable implements LoggerSetupInterface, StartGameWithWarInterface {

    public static enum eTYPES { SHIP, PLANE, TANK }
    @JsonIgnore
    private static final int MAX_FLY_TIME = 15;
    @JsonProperty("type")
    private String type;
    @JsonProperty("destructedLauncher")
    private List<DestructedLauncher> destructedLauncher;
    @JsonIgnore
    private War war;
    @JsonIgnore
    private Thread missileLauncherDestructorThread;
    @JsonIgnore
    private ReentrantReadWriteLock iteratorLock = new ReentrantReadWriteLock(true);
    @JsonIgnore
    private Lock lock = iteratorLock.writeLock();
    
    public Destructor_() {}

    public Destructor_(Observer observer, int id, War war) {
        int index = (int) (Math.random() * eTYPES.values().length);
        setType(eTYPES.values()[index].toString() + id);
        War.getDBservice().saveMissileLauncherDestructor(getType());
        startGame(observer, war);
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("destructedLauncher")
    public List<DestructedLauncher> getDestructedLauncher() {
        return destructedLauncher;
    }

    @JsonProperty("destructedLauncher")
    public void setDestructedLauncher(List<DestructedLauncher> destructedLauncher) {
        this.destructedLauncher = destructedLauncher;
    }

    public synchronized void destructAMissileLauncher(MissileLaunchers missileLaunchers) {
        Launcher launcher = war.getMissileLaunchers().findRandomLauncher();
        if (launcher != null) {
            DestructedLauncher dl = new DestructedLauncher(launcher.getId(), War.timeSinceGameStartedInSeconds());
            War.getDBservice().saveDestructedLauncher(dl.getId(),getType(),dl.getDestructTime());

            if (!destructedLauncher.contains(dl)) {
                lock.lock();
                destructedLauncher.add(dl);
                Collections.sort(destructedLauncher);
                lock.unlock();
                synchronized (missileLauncherDestructorThread) {
                    missileLauncherDestructorThread.notifyAll();
                }
            }
        }
    }

    @Override
    public void setupLoggerHandler() {
        try {
            FileHandler fh = new FileHandler(type + ".txt");
            fh.setFilter(new MissileLauncherDestructorFilter(this));
            fh.setFormatter(new FormattedLoggerMessage());
            war.getWarInformation().addLoggerHandler(fh);

            if(War.isConsoleGame()) {
                ConsoleHandler ch = new ConsoleHandler();
                ch.setFilter(new MissileLauncherDestructorFilter(this));
                ch.setFormatter(new FormattedLoggerMessage());
                war.getWarInformation().addLoggerHandler(ch);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MissileLauncherDestructorFilter implements Filter {

        private Destructor_ destructor;

        public MissileLauncherDestructorFilter(Destructor_ destructor) {
            this.destructor = destructor;
        }

        @Override
        public boolean isLoggable(LogRecord rec) {
            if (rec.getSourceClassName().equalsIgnoreCase("Entities.Destructor_$MissileLauncherDestructorThread") &&
                    rec.getMessage().contains(destructor.getType()))
                return true;
            else
                return false;
        }

    }

    @Override
    public void startGame(Observer observer, War war) {
        this.war = war;
        setupLoggerHandler();
        addObserver(observer);
        lock.lock();
        destructedLauncher = Collections.synchronizedList(destructedLauncher == null ? new ArrayList<>() : destructedLauncher);
        Collections.sort(destructedLauncher);
        lock.unlock();
        missileLauncherDestructorThread = new Thread(new MissileLauncherDestructorThread());
        missileLauncherDestructorThread.start();
    }

    private class MissileLauncherDestructorThread implements Runnable {
        @Override
        public void run() {

            while(!War.isGameOver()) {
                try {
                    synchronized (Thread.currentThread()) {
                        if (destructedLauncher.isEmpty()) {
                            Thread.currentThread().wait();
                        }
                        else {
                            DestructedLauncher dl = destructedLauncher.get(0);
                            Launcher launcher = war.getMissileLaunchers().findLauncher(dl.getId());
                            if (launcher != null && launcher.isHit()) {
                                destructedLauncher.remove(dl);
                                continue;
                            }
                            else if (launcher == null) {
                                destructedLauncher.remove(dl);
                                continue;
                            }

                            long time = dl.getDestructTime() * War.MILLISECOND_IN_SECOND - war.timeSinceGameStartedInSeconds() * War.MILLISECOND_IN_SECOND;
                            long timeToWait = time > 0 ? time : 1;
                            Thread.currentThread().wait(timeToWait);
                        }
                    }

                    if (!destructedLauncher.isEmpty()) {
                        DestructedLauncher dl = destructedLauncher.get(0);
                        Launcher launcher = war.getMissileLaunchers().findLauncher(dl.getId());
                        if(launcher != null && !launcher.isHit() && dl.getDestructTime() <= War.timeSinceGameStartedInSeconds()) {
                            destructedLauncher.remove(dl);

                            long flyTime = (long) (Math.random() * MAX_FLY_TIME);
                            dl.setFlyTime(flyTime);
                            Destructor_.this.setChanged();
                            Destructor_.this.notifyObservers(dl);

                            Thread.currentThread().sleep(flyTime * war.MILLISECOND_IN_SECOND);

                            String logMsg = "Launcher destructor: " + getType() + " Missile launcher: " + launcher.getId();

                            if (!launcher.getIsHidden() && !launcher.isHit() && Math.random() > 0.3) {
                                dl.setHit(true);
                                logMsg += " was destructed time: " + War.timeSinceGameStartedInSeconds();
                                launcher.setHit(true);
                                war.getMissileLaunchers().removeLauncher(launcher);
                                war.getWarInformation().incrementMissilesLaunchersDestructed();
                            }
                            else if (!launcher.getIsHidden()) {
                                logMsg += " was not destructed time: " + War.timeSinceGameStartedInSeconds();
                            }
                            else {
                                logMsg += " was not destructed (Hidden) time: " + War.timeSinceGameStartedInSeconds();
                            }

                            war.getWarInformation().getLogger().info(logMsg);
                            War.getDBservice().saveDestructLauncherResult(dl.getId(),getType(),dl.getDestructTime(),dl.isHit());

                        }
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
