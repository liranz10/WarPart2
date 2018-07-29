package DAL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import Entities.DestructedLauncher;

public class MongoDataService implements IDataService {

	private static MongoDataService theService = null;
	private static Object mutex = new Object();
	private Connection conn = null;
	private MongoClient mongoClient;
	private MongoDatabase database;

	private MongoDataService() {
		
			 mongoClient = new MongoClient("localhost", 27017);
			 database = mongoClient.getDatabase("war");
			 database.getCollection("destructdmissilesconn").deleteMany(new Document());
			 database.getCollection("destructedlauncherconn").deleteMany(new Document());
			 database.getCollection("missile").deleteMany(new Document());
			 database.getCollection("missilelaunchers").deleteMany(new Document());
			 database.getCollection("missiledestructors").deleteMany(new Document());
			 database.getCollection("missilelauncherdestructors").deleteMany(new Document());



	}

	public  IDataService getInstance() {
		MongoDataService result = theService;
		if (result == null) {
			synchronized (mutex) {
				result = theService;
				if (result == null)
					theService = result = new MongoDataService();
			}
		}
		return result;
	}

	@Override
	public void saveMissileLauncher(String id, boolean isHidden) {
		MongoCollection<Document> collection = database.getCollection("missilelaunchers");
		Document doc = new Document("_id", id)
		                .append("isHidden",isHidden);		                
		collection.insertOne(doc);

	}

	@Override
	public void saveMissileDestructor(String id) {
		MongoCollection<Document> collection = database.getCollection("missiledestructors");
		Document doc = new Document("_id", id);		                
		collection.insertOne(doc);

	}

	@Override
	public void saveMissileLauncherDestructor(String type) {
		MongoCollection<Document> collection = database.getCollection("missilelauncherdestructors");
		Document doc = new Document("_id", type);		                
		collection.insertOne(doc);

	}

	@Override
	public void saveMissileLauncherMissile(String id, String destination, long launchTime, int flyTime, int damage,
			String launcherId) {
		MongoCollection<Document> collection = database.getCollection("missile");
		Document doc = new Document("_id", id)
				.append("destination", destination)
				.append("launchTime", launchTime)
				.append("flyTime", flyTime)
				.append("damage", damage)
				.append("isHit", false);		                
		collection.insertOne(doc);
	
	}

	@Override
	public void saveDestructedLauncher(String id, String type, long destructTime) {
		MongoCollection<Document> collection = database.getCollection("destructedlauncherconn");
		Document doc = new Document("missileLauncherID", id)
				.append("type", type)
				.append("destructTime", destructTime)
				.append("success", false);		                
		collection.insertOne(doc);

	}

	@Override
	public void saveDestructedMissile(String missileDestructorID, String missileID, long destructAfterLaunch) {
		MongoCollection<Document> collection = database.getCollection("destructdmissilesconn");
		Document doc = new Document("missileDestructorID", missileDestructorID)
				.append("missileID", missileID)
				.append("destructAfterLaunch", destructAfterLaunch)
				.append("success", false);		                
		collection.insertOne(doc);

	}

	@Override
	public void saveDestructLauncherResult(String missileLauncherID, String destructorType, long destructTime,
			boolean success) {
		MongoCollection<Document> collection = database.getCollection("destructedlauncherconn");
		collection.updateMany(Filters.and(Filters.eq("missileLauncherID", missileLauncherID), Filters.eq("type", destructorType),Filters.eq("destructTime",destructTime)),
				Updates.set("success", success));    


	}

	@Override
	public void saveDestructMissileResult(String missileDestructorID, String missileID, long destructAfterLaunch,
			boolean success) {
		MongoCollection<Document> collection = database.getCollection("destructdmissilesconn");
		collection.updateMany(Filters.and(Filters.eq("missileDestructorID", missileDestructorID), Filters.eq("missileID", missileID),Filters.eq("destructAfterLaunch",destructAfterLaunch)),
				Updates.set("success", success));  

	}

	@Override
	public void saveMissileResult(String id, boolean isHit) {
		MongoCollection<Document> collection = database.getCollection("missile");
		collection.updateMany(Filters.eq("_id", id),
				Updates.set("isHit", isHit));  
	}

}
