package GameServer;

public interface WarListener {
	
	void addMissileLauncherEvent();
	void launchMissileEvent(String destination);
	void destructMissileEvent();


}
