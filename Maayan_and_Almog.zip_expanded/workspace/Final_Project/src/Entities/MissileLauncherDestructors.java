package Entities;

import java.util.*;

import BL.GUIWarGameImpl;
import Interfaces.MissileLauncherDestructorInterface;
import Interfaces.MissileLauncherDestructorWithObjectInterface;
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
public class MissileLauncherDestructors implements MissileLauncherDestructorWithObjectInterface, StartGameWithWarInterface {

    @JsonProperty("destructor")
    private List<Destructor_> destructor;
    @JsonIgnore
    private War war;
    @JsonIgnore
    @SuppressWarnings("deprecation")
    private Observer observer;

    @JsonProperty("destructor")
    public List<Destructor_> getDestructor() {
        return destructor;
    }

    @JsonProperty("destructor")
    public void setDestructor(List<Destructor_> destructor) {
        this.destructor = destructor;
    }

    @Override
    public void addMissileLauncherDestructor() {
        Destructor_ d = new Destructor_(observer, destructor.size() + 1, war);
        destructor.add(d);
    }

    @Override
    public void destructAMissileLauncher(MissileLaunchers missileLaunchers) {
        try {
            int index =  destructor.size() == 1 ? 0 : (int)(Math.random() * destructor.size()); //TODO: CHANGED
            destructor.get(index).destructAMissileLauncher(missileLaunchers);
        }
        catch (IndexOutOfBoundsException e) {
            if (war.isConsoleGame()) {
                War.stdOut.println("There is no missile launcher destructor !");
            }
            else {
                GUIWarGameImpl.showAlert("Missile Launcher Destructor",
                        "There is no missile launcher destructor");
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
