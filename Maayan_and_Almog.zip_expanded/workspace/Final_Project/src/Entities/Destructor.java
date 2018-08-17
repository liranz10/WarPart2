package Entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import Interfaces.StartGameWithWarInterface;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "destructedMissile"
})
@SuppressWarnings("deprecation")
public class Destructor extends Observable implements StartGameWithWarInterface {
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
        War.getDBservice().saveMissileDestructor(getId());
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
            War.getDBservice().saveDestructedMissile(this.getId(),dm.getId(),dm.getDestructAfterLaunch());

            Collections.sort(destructedMissile);
            lock.unlock();
            synchronized (missileDestructorThread) {
                missileDestructorThread.notifyAll();
            }
        }
    }
    @Override
    public String toString() {
    	return id;
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
        war.getWarInformation().setupDestructor(this);
    }

    private class MissileDestructorThread implements Runnable {
    	String logMsg;
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

                            Thread.sleep(flyTime * war.MILLISECOND_IN_SECOND);
                            logMsg = "Missile destructor: " + getId();

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

                            war.getWarInformation().Destructorinfo(logMsg);
                            War.getDBservice().saveDestructMissileResult(getId(),dm.getId(),dm.getDestructAfterLaunch(),dm.success());

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
