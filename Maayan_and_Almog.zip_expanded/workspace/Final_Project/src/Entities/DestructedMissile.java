package Entities;

import java.util.HashMap;
import java.util.Map;

import Interfaces.AnimationParametersInterface;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "destructAfterLaunch"
})
public class DestructedMissile implements Comparable<DestructedMissile>, AnimationParametersInterface {

    @JsonProperty("id")
    private String id;
    @JsonProperty("destructAfterLaunch")
    private long destructAfterLaunch;
    @JsonIgnore
    private long flyTime;
    @JsonIgnore
    private boolean hit;

    public DestructedMissile() {}

    public DestructedMissile(String id) {
        setId(id);
        setDestructAfterLaunch(War.timeSinceGameStartedInSeconds());
    }

    public long getFlyTime() {
        return flyTime;
    }

    public void setFlyTime(long flyTime) {
        this.flyTime = flyTime;
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
        this.id = id;
    }

    @JsonProperty("destructAfterLaunch")
    public long getDestructAfterLaunch() {
        return destructAfterLaunch;
    }

    @JsonProperty("destructAfterLaunch")
    public void setDestructAfterLaunch(long destructAfterLaunch) {
        this.destructAfterLaunch = destructAfterLaunch;
    }

    @Override
    public int compareTo(DestructedMissile o) {
        if (id == o.id) {
            return 0;
        }

        return (int) (this.destructAfterLaunch - o.getDestructAfterLaunch());
    }

    @Override
    public long delay() {
        return getFlyTime();
    }

    @Override
    public boolean success() {
        return isHit();
    }
}
