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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import DAL.SqlDataService;

import org.aspectj.lang.annotation.Aspect;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "destructedMissile"
})
@SuppressWarnings("deprecation")
public class Destructor extends Observable implements LoggerSetupInterface, StartGameWithWarInterface {
    @JsonIgnore
    private static final int MAX_FLY_TIME = 15;
    @JsonIgnore
    private static int idNumerator = 0;
    @JsonProperty("id")
    private String id;
    @JsonProperty("destructedMissile")
    private List<DestructedMissile> destructedMissile;
    @JsonIgnore
    private War war;
    @JsonIgnore
    private Thread missileDestructorThread;
    @JsonIgnore
    private ReentrantReadWriteLock iteratorLock = new ReentrantReadWriteLock(true);
    @JsonIgnore
    private Lock lock = iteratorLock.writeLock();

    private Destructor() {}

    public Destructor(Observer observer, int id, War war) {
        setId("D" + id);
        SqlDataService.getInstance().saveMissileDestructor(getId());
        startGame(observer, war);
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        if (id.compareTo("D" + idNumerator) > 0) {
            idNumerator = Integer.parseInt(id.substring(1));
        }

        this.id = id;
    }

    @JsonProperty("destructedMissile")
    public List<DestructedMissile> getDestructedMissile() {
        return destructedMissile;
    }

    @JsonProperty("destructedMissile")
    public void setDestructedMissile(List<DestructedMissile> destructedMissile) {
        this.destructedMissile = destructedMissile;
        lock.lock();
        Collections.sort(destructedMissile);
        lock.unlock();
    }

    public synchronized void destructAMissile() {
        Missile m = war.getMissileLaunchers().findRandomFlyingMissile();

        if (m != null && !destructedMissile.contains(m)) {
            lock.lock();
            DestructedMissile dm = new DestructedMissile(m.getId());
            destructedMissile.add(dm);
			SqlDataService.getInstance().saveDestructedMissile(this.getId(),dm.getId(),dm.getDestructAfterLaunch());

            Collections.sort(destructedMissile);
            lock.unlock();
            synchronized (missileDestructorThread) {
                missileDestructorThread.notifyAll();
            }
        }
    }

    @Override
    public void startGame(Observer observer, War war) {
        this.war = war;
        addObserver(observer);
        lock.lock();
        destructedMissile = Collections.synchronizedList(destructedMissile == null ? new ArrayList<>() : destructedMissile);
        Collections.sort(destructedMissile);
        lock.unlock();
        missileDestructorThread = new Thread(new MissileDestructorThread());
        missileDestructorThread.start();
        setupLoggerHandler();
    }

    private class MissileDestructorThread implements Runnable {
        @Override
        public void run() {

            while(!War.isGameOver()) {
                try {
                    synchronized (Thread.currentThread()) {
                        if (destructedMissile.isEmpty()) {
                            Thread.currentThread().wait();
                        }
                        else {
                            DestructedMissile dm = destructedMissile.get(0);
                            Missile m = war.getMissileLaunchers().findMissile(dm.getId());
                            if ((m != null && m.isDestructed()) || dm.isHit()) {
                                destructedMissile.remove(dm);
                                continue;
                            }
                            else if (m == null) {
                                destructedMissile.remove(dm);
                                continue;
                            }

                            long time = dm.getDestructAfterLaunch() * War.MILLISECOND_IN_SECOND - war.timeSinceGameStartedInSeconds() * War.MILLISECOND_IN_SECOND;
                            long timeToWait = time > 0 ? time : 1;
                            Thread.currentThread().wait(timeToWait);
                        }
                    }
                    if (!destructedMissile.isEmpty()) {
                        DestructedMissile dm = destructedMissile.get(0);
                        Missile m = war.getMissileLaunchers().findMissile(dm.getId());
                        if(m != null && !m.isDestructed() && dm.getDestructAfterLaunch() <= War.timeSinceGameStartedInSeconds()) {
                            destructedMissile.remove(dm);

                            long flyTime = (long) (Math.random() * MAX_FLY_TIME);
                            dm.setFlyTime(flyTime);
                            Destructor.this.setChanged();
                            Destructor.this.notifyObservers(dm);

                            Thread.currentThread().sleep(flyTime * war.MILLISECOND_IN_SECOND);
                            String logMsg = "Missile destructor: " + getId();

                            // randomize if missile was destructed successfully
                            if (m != null && m.isFlying() && !m.isDestructed() && Math.random() > 0.3) {
                                m.setDestructed(true);
                                synchronized (m) {
                                    m.notifyAll();
                                }
                                dm.setHit(true);
                                destructedMissile.remove(dm);
                                logMsg += " Missile: " + m.getId() + " was destructed time: " + War.timeSinceGameStartedInSeconds();
                                war.getWarInformation().incrementMissilesDestructed();
                            }
                            else if (m != null) {
                                logMsg += " Missile: " + m.getId() + " was not destructed, Damage: " + m.getDamage()+ " time: " + War.timeSinceGameStartedInSeconds();
                            }
                            else {
                                logMsg += " Missile: " + dm.getId() + " was not destructed ! (Not flying)" + " time: " + War.timeSinceGameStartedInSeconds();
                            }

                            war.getWarInformation().getLogger().info(logMsg);
                        	SqlDataService.getInstance().saveDestructMissileResult(getId(),dm.getId(),dm.getDestructAfterLaunch(),dm.success());

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
            FileHandler fh = new FileHandler(id + ".txt");
            fh.setFilter(new MissileDestructorFilter(this));
            fh.setFormatter(new FormattedLoggerMessage());
            war.getWarInformation().addLoggerHandler(fh);

            if(War.isConsoleGame()) {
                ConsoleHandler ch = new ConsoleHandler();
                ch.setFilter(new MissileDestructorFilter(this));
                ch.setFormatter(new FormattedLoggerMessage());
                war.getWarInformation().addLoggerHandler(ch);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MissileDestructorFilter implements Filter {

        private Destructor destructor;

        public MissileDestructorFilter(Destructor destructor) {
            this.destructor = destructor;
        }

        @Override
        public boolean isLoggable(LogRecord rec) {
            if (rec.getSourceClassName().equalsIgnoreCase("Entities.Destructor$MissileDestructorThread") &&
                    rec.getMessage().contains(destructor.getId()))
                return true;
            else
                return false;
        }

    }

}
