package Interfaces;

public interface WarGameInterface extends MissileDestructorWithoutObjectInterface,
                                          MissileLauncherDestructorWithoutObjectInterface,
                                          MissileLauncherInterface{

    public void showStatistics();

}
