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

import DAL.SqlDataService;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "destructTime"
})
public class DestructedLauncher implements Comparable<DestructedLauncher>, AnimationParametersInterface {

    @JsonProperty("id")
    private String id;
    @JsonProperty("destructTime")
    private long destructTime;
    @JsonIgnore
    private boolean hit;
    @JsonIgnore
    private long flyTime;

    public DestructedLauncher() {}

    public DestructedLauncher(String id, long destructTime) {
        setId(id);
        setDestructTime(destructTime);

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

    @JsonProperty("destructTime")
    public long getDestructTime() {
        return destructTime;
    }

    @JsonProperty("destructTime")
    public void setDestructTime(long destructTime) {
        this.destructTime = destructTime;
    }

    @Override
    public int compareTo(DestructedLauncher o) {
        if (id == o.id) {
            return 0;
        }
        return (int)(getDestructTime() - o.getDestructTime());
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
