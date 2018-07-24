package Interfaces;

import BL.GUIWarGameImpl;
import Entities.Destructor_;

public interface AnimationsInterface {

    public void missileAnimation(GUIWarGameImpl.eDirection direction, AnimationParametersInterface api);
    public void housesAnimation();
    public void addMissileLauncherAnimation(int index);
    public void addMissileLauncherDestructorAnimation(int index, Destructor_ destructor);
    public void addMissileDestructorAnimation(int index);
    public void createGuiAnimation();
    public void boomAnimation(double targetX, double targetY);
    public void destructLauncherAnimation(int launcherIndex);
}
