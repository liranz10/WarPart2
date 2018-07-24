package Interfaces;

import BL.GUIWarGameImpl;
import Entities.War;

import java.util.Observer;

public interface StartGameWithWarInterface {

    @SuppressWarnings("deprecation")
    public void startGame(Observer observer, War war);

}
