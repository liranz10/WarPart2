package Entities;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import Interfaces.LoggerSetupInterface;
import Interfaces.StartGameWithWarInterface;
import com.fasterxml.jackson.annotation.*;

import DAL.IDataService;

import java.util.Observable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "isHidden",
    "missile"
})
@SuppressWarnings("deprecation")
public class Launcher extends Observable implements LoggerSetupInterface, StartGameWithWarInterface {

    @JsonIgnore
    private static int idNumerator = 0;
    @JsonProperty("id")
    private String id;
    @JsonProperty("isHidden")
    private boolean isHidden;
    @JsonProperty("missile")
    private List<Missile> missile;
    @JsonIgnore
    private boolean hit = false;
    @JsonIgnore
    private Thread missileLauncherThread;
    @JsonIgnore
    private War war;
    @JsonIgnore
    private ReentrantReadWriteLock iteratorLock = new ReentrantReadWriteLock(true);
    @JsonIgnore
    private Lock lock = iteratorLock.writeLock();

    private Launcher() {}

    @SuppressWarnings("deprecation")
    public Launcher(Observer observer, boolean isHidden, War war) {
        setIsHidden(isHidden);
        setId("L" + (++idNumerator));
        War.getDBservice().saveMissileLauncher(id, isHidden);
        startGame(observer, war);
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        if (id.compareTo("L" + idNumerator) > 0) {
            idNumerator = Integer.parseInt(id.substring(1));
        }

        this.id = id;
    }

    @JsonProperty("isHidden")
    public boolean getIsHidden() {
        return isHidden;
    }

    @JsonProperty("isHidden")
    public void setIsHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    @JsonProperty("missile")
    public List<Missile> getMissile() {
        return missile;
    }

    @JsonProperty("missile")
    public void setMissile(List<Missile> missile) {
        this.missile = missile;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public synchronized void launchAMissle(String destination) {
        Missile m = new Missile(destination);
        War.getDBservice().saveMissileLauncherMissile(m.getId(),m.getDestination(),m.getLaunchTime(),m.getFlyTime(),m.getDamage(),getId());
		
        lock.lock();
        missile.add(m);
        Collections.sort(missile);
        lock.unlock();
        synchronized (missileLauncherThread) { //TODO: if there's one launcher for example, you can't add him several missiles to shoot. each time one.
            missileLauncherThread.notify();
        }
    }

    public synchronized Missile findFlyingMissile() {
        lock.lock();
        List<Missile> lst = new ArrayList<>(missile);
        lock.unlock();
        for (Missile m : lst) {
            if (m.isFlying()) {
                return m;
            }
        }
        return null;
    }

    public synchronized Missile findMissile(String id) {
        lock.lock();
        List<Missile> lst = new ArrayList<>(missile);
        lock.unlock();
        for (Missile m : lst) {
            if (m.getId().equalsIgnoreCase(id)) {
                return m;
            }
        }

        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void startGame(Observer observer, War war) {
        this.war = war;
        addObserver(observer);
        lock.lock();
        missile = Collections.synchronizedList(missile == null ? new ArrayList<>() : missile);
        Collections.sort(missile);
        lock.unlock();
        missileLauncherThread = new Thread(new MissileLauncherThread());
        missileLauncherThread.start();
        setupLoggerHandler();
    }

    private class MissileLauncherThread implements Runnable {
        @Override
        public void run() {

            while(!War.isGameOver() && !isHit()) {
                try {
                    synchronized (Thread.currentThread()) {
                        if (missile.isEmpty()) {
                            Thread.currentThread().wait();
                        }
                        else {
                            long time = missile.get(0).getLaunchTime() * War.MILLISECOND_IN_SECOND - war.timeSinceGameStartedInSeconds() * War.MILLISECOND_IN_SECOND;
                            long timeToWait = time > 0 ? time : 1;
                            Thread.currentThread().wait(timeToWait);
                        }
                    }

                    if (!missile.isEmpty()) {
                        Missile m = missile.get(0);
                        if(m.getLaunchTime() <= War.timeSinceGameStartedInSeconds()) {

                            setIsHidden(false);
                            boolean randomIsHit = Math.random() < 0.5;
                            long actualLaunchTime = War.timeSinceGameStartedInSeconds();
                            war.getWarInformation().incrementMissilesLaunched();
                            Launcher.this.setChanged();
                            Launcher.this.notifyObservers(m);
                            m.setHit(randomIsHit);
                            m.launch(Launcher.this);

                            synchronized (m) {
                                m.wait();
                            }

                            setIsHidden(true);
                            missile.remove(m);

                            String logMsg = "Launcher "+getId()+" launches Missile "+m.getId()+": desination: " + m.getDestination()
                                    + " Expected Launch time: " + m.getLaunchTime()
                                    + " Actual launch time: " + actualLaunchTime;

                            long hitTime = War.timeSinceGameStartedInSeconds();

                            if(!m.isDestructed() && randomIsHit) {
                                logMsg += " Hit time: " + hitTime +
                                        " Damage: " + m.getDamage() + " (Hit)";
                                war.getWarInformation().incrementMissilesHit();
                                war.getWarInformation().incrementTotalEconomicDamage(m.getDamage());
                            }
                            else if (m.isDestructed()){
                                logMsg += " Destruction time: " + m.getDestructionTime();
                            }
                            else {
                                war.getWarInformation().incrementMissilesMissed();
                                logMsg += " Hit time: " + hitTime + " (Missed)";
                            }
                            War.getDBservice().saveMissileResult(m.getId(),m.isHit());
                            war.getWarInformation().getLogger().info(logMsg);
                        }
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setupLoggerHandler() {
        try {
            FileHandler fh = new FileHandler(id + ".txt", false);
            fh.setFilter(new LauncherFilter(this));
            fh.setFormatter(new FormattedLoggerMessage());
            war.getWarInformation().addLoggerHandler(fh);

            if(War.isConsoleGame()) {
                ConsoleHandler ch = new ConsoleHandler();
                ch.setFilter(new LauncherFilter(this));
                ch.setFormatter(new FormattedLoggerMessage());
                war.getWarInformation().addLoggerHandler(ch);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class LauncherFilter implements Filter {

        private Launcher launcher;

        public LauncherFilter(Launcher launcher) {
            this.launcher = launcher;
        }

        @Override
        public boolean isLoggable(LogRecord rec) {
            if (rec.getSourceClassName().equalsIgnoreCase("Entities.Launcher$MissileLauncherThread") &&
                    rec.getThreadID() == launcher.missileLauncherThread.getId())
                return true;
            else
                return false;
        }

    }

}
