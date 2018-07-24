package Entities;

import java.util.*;

import BL.GUIWarGameImpl;
import Interfaces.MissileDestructorWithObjectInterface;
import Interfaces.StartGameWithWarInterface;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javafx.scene.control.Alert;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "destructor"
})
public class MissileDestructors implements MissileDestructorWithObjectInterface, StartGameWithWarInterface {

    @JsonProperty("destructor")
    private List<Destructor> destructor;
    @JsonIgnore
    private War war;
    @JsonIgnore
    @SuppressWarnings("deprecation")
    private Observer observer;

    @JsonProperty("destructor")
    public List<Destructor> getDestructor() {
        return destructor;
    }

    @JsonProperty("destructor")
    public void setDestructor(List<Destructor> destructor) {
        this.destructor = destructor;
    }

    @Override
    public void addMissileDestructor() {
        Destructor d = new Destructor(observer,destructor.size() + 1, war);
        destructor.add(d);
    }

    @Override
    public void destructAMissile(MissileLaunchers missileLaunchers) {
        try {
            int index = destructor.size() == 1 ? 0 : (int)(Math.random() * destructor.size()); // TODO: CHANGED
            destructor.get(index).destructAMissile();
        }
        catch (IndexOutOfBoundsException e) {
            if (war.isConsoleGame()) {
                War.stdOut.println("There is no missile destructor !");
            }
            else {
                GUIWarGameImpl.showAlert("Missile Destructor",
                        "There is no missile destructor");
            }

        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void startGame(Observer observer, War war) {
        this.war = war;
        this.observer = observer;
        destructor = Collections.synchronizedList(destructor == null ? new ArrayList<>() : destructor);
        destructor.forEach(s -> s.startGame(observer, war));
    }
}
