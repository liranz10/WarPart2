package Entities;

import Interfaces.AnimationParametersInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javafx.fxml.FXML;
import javafx.scene.layout.GridPane;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "destination",
    "launchTime",
    "flyTime",
    "damage"
})
public class Missile implements Comparable<Missile>, AnimationParametersInterface {
    @JsonIgnore
    private static final int MAX_FLY_TIME = 15;
    @JsonIgnore
    private static final int MAX_DAMAGE = 10000;
    @JsonIgnore
    private static int idNumerator = 0;
    @JsonProperty("id")
    private String id;
    @JsonProperty("destination")
    private String destination;
    @JsonProperty("launchTime")
    private long launchTime;
    @JsonProperty("flyTime")
    private int flyTime;
    @JsonProperty("damage")
    private int damage;
    @JsonIgnore
    private boolean destructed;
    @JsonIgnore
    private boolean flying;
    @JsonIgnore
    private long destructionTime;
    @JsonIgnore
    public boolean hit;

    public Missile() {}

    public Missile(String destination) {
        setDestination(destination);
        setId("M" + (++idNumerator));
        setFlyTime((int)(Math.random() * MAX_FLY_TIME));
        setDamage((int)(Math.random() * MAX_DAMAGE));
        setLaunchTime(War.timeSinceGameStartedInSeconds());
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        if (id.compareTo("M" + idNumerator) > 0) {
            idNumerator = Integer.parseInt(id.substring(1));
        }

        this.id = id;
    }

    @JsonProperty("destination")
    public String getDestination() {
        return destination;
    }

    @JsonProperty("destination")
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @JsonProperty("launchTime")
    public long getLaunchTime() {
        return launchTime;
    }

    @JsonProperty("launchTime")
    public void setLaunchTime(long launchTime) {
        this.launchTime = launchTime;
    }

    @JsonProperty("flyTime")
    public int getFlyTime() {
        return flyTime;
    }

    @JsonProperty("flyTime")
    public void setFlyTime(int flyTime) {
        this.flyTime = flyTime;
    }

    @JsonProperty("damage")
    public int getDamage() {
        return damage;
    }

    @JsonProperty("damage")
    public void setDamage(int damage) {
        this.damage = damage;
    }

    @Override
    public int compareTo(Missile o) {

        if (this.launchTime - o.launchTime == 0) {
            return id.compareTo(o.id);
        }

        return (int)(this.launchTime - o.launchTime);
    }

    public synchronized boolean isDestructed() {
        return destructed;
    }

    public synchronized void setDestructed(boolean destructed) {

        if(destructed) {
            destructionTime = War.timeSinceGameStartedInSeconds();
        }

        this.destructed = destructed;
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public long getDestructionTime() {
        return destructionTime;
    }

    public void launch(Launcher launcher) {
        new Thread(new LaunchMissile(launcher)).start();
    }

    @Override
    public long delay() {
        return getFlyTime();
    }

    @Override
    public boolean success() {
        return isHit() && !isDestructed();
    }


    private class LaunchMissile implements Runnable {

        private Launcher launcher;

        public LaunchMissile(Launcher launcher) {
            this.launcher = launcher;
        }

        @Override
        public void run() {

            try {
                setFlying(true);
                Thread.sleep(getFlyTime() * War.MILLISECOND_IN_SECOND);

                setFlying(false);

                synchronized (Missile.this) {
                    Missile.this.notify();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
