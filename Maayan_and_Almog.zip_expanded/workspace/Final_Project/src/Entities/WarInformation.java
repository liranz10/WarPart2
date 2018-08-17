package Entities;

import java.util.Observable;

@SuppressWarnings("deprecation")
public class WarInformation extends Observable {
	private int missilesLaunched ;
	private int missilesDestructed;
	private int missilesHit;
	private int missilesMissed;
	private int missilesLaunchersDestructed;
	private int totalEconomicDamage;
	public enum eCALLER_FUNCTION {
		INCREMENT_MISSILES_LAUNCHED,
		INCREMENT_MISSILES_MISSED,
		INCREMENT_MISSILES_DESTRUCTED,
		INCREMENT_MISSILES_HIT,
		INCREMENT_MISSILES_LAUNCHERS_DESTRUCTED,
		INCREMENT_TOTAL_ECONOMIC_DAMAGE
	}

	public WarInformation() {
	}

	public void init() {
		missilesLaunched = 0;
		missilesDestructed = 0;
		missilesHit = 0;
		missilesMissed = 0;
		missilesLaunchersDestructed = 0;
		totalEconomicDamage = 0;		
	}

	public synchronized void incrementMissilesLaunched() {
		missilesLaunched++;
		notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_LAUNCHED);
	}

	public synchronized void incrementMissilesMissed() {
		missilesMissed++;
		notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_MISSED);
	}

	public synchronized void incrementMissilesDestructed() {
		missilesDestructed++;
		notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_DESTRUCTED);
	}

	public synchronized void incrementMissilesHit() {
		missilesHit++;
		notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_HIT);
	}

	public synchronized void incrementMissilesLaunchersDestructed() {
		missilesLaunchersDestructed++;
		notifyAllObserver(eCALLER_FUNCTION.INCREMENT_MISSILES_LAUNCHERS_DESTRUCTED);
	}

	public synchronized void incrementTotalEconomicDamage(int damageCost) {
		if (damageCost >= 0) {
			totalEconomicDamage += damageCost;
		}

		notifyAllObserver(eCALLER_FUNCTION.INCREMENT_TOTAL_ECONOMIC_DAMAGE);
	}

	public void notifyAllObserver(eCALLER_FUNCTION callerFunction) {
		setChanged();
		notifyObservers(callerFunction);
	}

	@Override
	public String toString() {
		return  "\nMissiles Launched:              " + missilesLaunched            +
				"\nMissiles Destructed:            " + missilesDestructed          +
				"\nMissiles Hit:                   " + missilesHit                 +
				"\nMissile Missed:                 " + missilesMissed              +
				"\nMissiles Launchers Destructed:  " + missilesLaunchersDestructed +
				"\nTotal Economic Damage:          " + totalEconomicDamage         +
				"\n";
	}

	public int getMissilesLaunched() {
		return missilesLaunched;
	}

	public int getMissilesDestructed() {
		return missilesDestructed;
	}

	public int getMissilesHit() {
		return missilesHit;
	}

	public int getMissilesLaunchersDestructed() {
		return missilesLaunchersDestructed;
	}

	public int getTotalEconomicDamage() {
		return totalEconomicDamage;
	}

	public int getMissilesMissed() {
		return missilesMissed;
	}



	public void Destructorinfo(String message) {

	}
	public void Launcherinfo(String message) {

	}
	public void Destructor_info(String message) {


	}
	public void setupDestructor_(Destructor_ d) {    
	}
	public void setupDestructor(Destructor d) {

	}
	public void setupLauncher(Launcher l) {

	}

}



