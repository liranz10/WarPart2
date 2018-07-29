package DAL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Entities.DestructedLauncher;

public class SqlDataService implements IDataService {

	private static SqlDataService theService = null;
	private static Object mutex = new Object();
	private Connection conn = null;

	private SqlDataService() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/war?user=root&password=adva6767");
			// reset db
//		    PreparedStatement stmt = conn.prepareStatement("SELECT Concat('TRUNCATE TABLE ',table_schema,'.',TABLE_NAME, ';') as Command \r\n" + 
//		    		"FROM INFORMATION_SCHEMA.TABLES where  table_schema in ('war');");
//		    ResultSet rs = stmt.executeQuery();
//		    while(rs.next()) {
//		     conn.prepareStatement("SET FOREIGN_KEY_CHECKS = 0; "+rs.getString("Command")+" SET FOREIGN_KEY_CHECKS = 1;").executeUpdate();
//		    			
//		    }
		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public  IDataService getInstance() {
		SqlDataService result = theService;
		if (result == null) {
			synchronized (mutex) {
				result = theService;
				if (result == null)
					theService = result = new SqlDataService();
			}
		}
		return result;
	}

	@Override
	public void saveMissileLauncher(String id, boolean isHidden) {
		try {
			PreparedStatement stmt = conn
					.prepareStatement("INSERT INTO `missilelaunchers`(`id`, `isHidden`) VALUES (?,?)");
			stmt.setString(1, id);
			stmt.setBoolean(2, isHidden);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveMissileDestructor(String id) {
		try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO `missiledestructors`(`id`) VALUES (?)");
			stmt.setString(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveMissileLauncherDestructor(String type) {
		try {
			PreparedStatement stmt = conn
					.prepareStatement("INSERT INTO `missilelauncherdestructors`(`type`) VALUES (?)");
			stmt.setString(1, type);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveMissileLauncherMissile(String id, String destination, long launchTime, int flyTime, int damage,
			String launcherId) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO `missile`(`id`,`destination`,`launchTime`,`flyTime`,`damage`,`missileLauncherId`) VALUES (?,?,?,?,?,?)");
			stmt.setString(1, id);
			stmt.setString(2, destination);
			stmt.setInt(3, (int) launchTime);
			stmt.setInt(4, flyTime);
			stmt.setInt(5, damage);
			stmt.setString(6, launcherId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void saveDestructedLauncher(String id, String type, long destructTime) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO `destructedlauncherconn`(`missileLauncherID`,`destructorType`,`destructTime`) VALUES (?,?,?)");
			stmt.setString(1, id);
			stmt.setString(2, type);
			stmt.setInt(3, (int) destructTime);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveDestructedMissile(String missileDestructorID, String missileID, long destructAfterLaunch) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"INSERT INTO `destructdmissilesconn`(`missileDestructorID`,`missileID`,`destructAfterLaunch`) VALUES (?,?,?)");
			stmt.setString(1, missileDestructorID);
			stmt.setString(2, missileID);
			stmt.setInt(3, (int) destructAfterLaunch);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveDestructLauncherResult(String missileLauncherID, String destructorType, long destructTime,
			boolean success) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"UPDATE `destructedlauncherconn` SET `success`=(?) WHERE missileLauncherID=(?) AND destructorType=(?) AND destructTime=(?) ");
			stmt.setBoolean(1, success);
			stmt.setString(2, missileLauncherID);
			stmt.setString(3, destructorType);
			stmt.setInt(4, (int) destructTime);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveDestructMissileResult(String missileDestructorID, String missileID, long destructAfterLaunch,
			boolean success) {
		try {
			PreparedStatement stmt = conn.prepareStatement(
					"UPDATE `destructdmissilesconn` SET `success`=(?) WHERE missileDestructorID=(?) AND missileID=(?) AND destructAfterLaunch=(?) ");
			stmt.setBoolean(1, success);
			stmt.setString(2, missileDestructorID);
			stmt.setString(3, missileID);
			stmt.setInt(4, (int) destructAfterLaunch);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void saveMissileResult(String id, boolean isHit) {
		try {
			PreparedStatement stmt = conn.prepareStatement("UPDATE `missile` SET `isHit`=(?) WHERE id=(?) ");
			stmt.setBoolean(1, isHit);
			stmt.setString(2, id);

			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
