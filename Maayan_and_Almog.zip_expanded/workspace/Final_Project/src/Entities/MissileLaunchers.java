package Entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import BL.GUIWarGameImpl;
import Interfaces.StartGameWithWarInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "launcher"
})
public class MissileLaunchers implements StartGameWithWarInterface {

    @JsonProperty("launcher")
    private List<Launcher> launcher;
    @JsonIgnore
    private static MissileLaunchers missileLaunchers;
    @JsonIgnore
    private War war;
    @JsonIgnore
    @SuppressWarnings("deprecation")
    private Observer observer;
    @JsonIgnore
    private ReentrantReadWriteLock iteratorLock = new ReentrantReadWriteLock(true);
    @JsonIgnore
    private Lock lock = iteratorLock.writeLock();

    @JsonProperty("launcher")
    public List<Launcher> getLauncher() {
        return launcher;
    }

    @JsonProperty("launcher")
    public void setLauncher(List<Launcher> launcher) {
        this.launcher = launcher;
    }

    public void addMissileLauncher() {
        boolean isHidden = Math.random() > 0.5;
        launcher.add(new Launcher(observer, isHidden, war));
    }

    public synchronized void removeLauncher(Launcher launcher) {
        lock.lock();
        this.launcher.remove(launcher);
        lock.unlock();
    }

    public synchronized Missile findRandomFlyingMissile() {
        lock.lock();
        List<Launcher> lst = new ArrayList<>(launcher);
        lock.unlock();
        Missile m = null;
        for (Launcher l : lst) {
            m = l.findFlyingMissile();
            if (m != null) {
                return m;
            }
        }

        return null;
    }

    public synchronized Missile findMissile(String id) {
        lock.lock();
        List<Launcher> lst = new ArrayList<>(launcher);
        lock.unlock();
        for (Launcher l : lst) {
            Missile m = l.findMissile(id);
            if (m != null) {
                return m;
            }
        }

        return null;
    }

    public synchronized Launcher findRandomLauncher() {
        lock.lock();
        List<Launcher> lst = new ArrayList<>(launcher);
        lock.unlock();
        for (Launcher l : launcher) {
            if (l != null) {
                return l;
            }
        }

        return null;
    }

    public synchronized Launcher findLauncher(String id) {
        lock.lock();
        List<Launcher> lst = new ArrayList<>(launcher);
        lock.unlock();
        for (Launcher l : lst) {
            if (l != null && l.getId().equals(id)) {
                return l;
            }
        }

        return null;
    }

    public synchronized void launchAMissle(String destination) {

        try {
            Launcher lncher;

            do {
                int index =  launcher.size() == 1 ? 0 : (int) (Math.random() * launcher.size());
                lock.lock();
                lncher = launcher.get(index);
                lock.unlock();
            } while (lncher.isHit());

         lncher.launchAMissle(destination);
        }
        catch (IndexOutOfBoundsException e) {
            if (war.isConsoleGame()) {
                lock.unlock();
                War.stdOut.println("There is no missile launcher");
            }
            else {
                lock.unlock();
                GUIWarGameImpl.showAlert("Missile Launcher",
                        "There is no missile launcher");
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void startGame(Observer observer, War war) {
        this.war = war;
        this.observer = observer;
        lock.lock();
        launcher = Collections.synchronizedList(launcher == null ? new ArrayList<>() : launcher);
        launcher.forEach(s -> s.startGame(observer, war));
        lock.unlock();
    }
}
