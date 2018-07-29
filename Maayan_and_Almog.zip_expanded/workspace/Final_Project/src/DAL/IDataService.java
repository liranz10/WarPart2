package DAL;

public interface IDataService {

	IDataService getInstance();
	void saveMissileLauncher(String id, boolean isHidden);

	void saveMissileDestructor(String id);

	void saveMissileLauncherDestructor(String type);

	void saveMissileLauncherMissile(String id, String destination, long launchTime, int flyTime, int damage,
			String launcherId);

	void saveDestructedLauncher(String id, String type, long destructTime);

	void saveDestructedMissile(String missileDestructorID, String missileID, long destructAfterLaunch);

	void saveDestructLauncherResult(String missileLauncherID, String destructorType, long destructTime,
			boolean success);

	void saveDestructMissileResult(String missileDestructorID, String missileID, long destructAfterLaunch,
			boolean success);

	void saveMissileResult(String id, boolean isHit);
}
