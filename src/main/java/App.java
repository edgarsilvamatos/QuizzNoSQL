import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class App {
	public static void main(String[] args) {
		ConnectionString connectionString = new ConnectionString("mongodb://root:root@localhost");
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.serverApi(ServerApi.builder()
						.version(ServerApiVersion.V1)
						.build())
				.build();
		MongoClient mongoClient = MongoClients.create(settings);
		MongoDatabase database = mongoClient.getDatabase("QuizzNoSQL");

		MongoCollection<Document> collection = database.getCollection("GeoCountries");
		MongoCollection<Document> userResultsCollection = database.getCollection("UserResults");

		Scanner scanner = new Scanner(System.in);
		System.out.println("What is your name?");
		String name = scanner.nextLine();

		System.out.println("Choose a category:");
		System.out.println("1. Population");
		System.out.println("2. Surface Area");
		System.out.println("3. GDP");
		System.out.print("Enter your choice (1-3): ");
		int userChoice = scanner.nextInt();

		int score = 0;
		long startTime = System.currentTimeMillis();

		for (int question = 1; question <= 3; question++) {
			List<String> randomCountryNames = getRandomCountryNames(collection, 3);
			System.out.println("---");
			System.out.println("Question " + question + ": Choose the country with the biggest " +
					getPropertyDescription(userChoice) + " among the following:");

			for (int i = 0; i < randomCountryNames.size(); i++) {
				System.out.println((i + 1) + ". " + randomCountryNames.get(i));
			}

			System.out.print("Enter your choice (1-3): ");
			int userCountryChoice = scanner.nextInt();

			String chosenCountryName = randomCountryNames.get(userCountryChoice - 1);
			Double chosenPropertyValue = getProperty(collection, chosenCountryName, userChoice);

			if (isHighest(chosenPropertyValue, randomCountryNames, collection, userChoice)) {
				System.out.println(" ");
				System.out.println("Correct! " + chosenCountryName + " has the highest " +
						getPropertyDescription(userChoice) + ".");
				System.out.println(" ");
				score++;
			} else {
				System.out.println(" ");
				System.out.println("Incorrect. " + chosenCountryName + " does not have the highest " +
						getPropertyDescription(userChoice) + ".");
				System.out.println(" ");
			}

			for (int i = 0; i < randomCountryNames.size(); i++) {
				String countryName = randomCountryNames.get(i);
				Double propertyValue = getProperty(collection, countryName, userChoice);
				System.out.println(countryName + " - " + getPropertyDescription(userChoice) + ": " + propertyValue);
			}
		}

		long endTime = System.currentTimeMillis();
		long elapsedTime = (endTime - startTime) ;

		Document userResultDocument = new Document()
				.append("username", name)
				.append("score", score)
				.append("time_taken", elapsedTime);
		userResultsCollection.insertOne(userResultDocument);

		System.out.println(" ");
		System.out.println("======");
		System.out.println("Quiz completed, " + name + "! Your final score is: " + score);
		System.out.println("Time taken: " + elapsedTime + " milliseconds");
		System.out.println("======");


		System.out.println(" ");
		System.out.println("Top 3 User Results:");

		List<Document> topUserResults = getTopUserResults(userResultsCollection, 3);
		for (int i = 0; i < topUserResults.size(); i++) {
			Document result = topUserResults.get(i);
			String username = result.getString("username");
			int userScore = result.getInteger("score");
			long userTimeTaken = result.getLong("time_taken");

			System.out.println((i + 1) + ". Username: " + username + ", Score: "
					+ userScore + ", Time Taken: " + userTimeTaken + " milliseconds");
		}

		scanner.close();
	}

	private static List<Document> getTopUserResults(MongoCollection<Document> userResultsCollection, int limit) {
		List<Document> topUserResults = new ArrayList<>();
		userResultsCollection.find().sort(new Document("score", -1).append("time_taken", 1)).limit(limit).into(topUserResults);
		return topUserResults;
	}

	private static List<String> getRandomCountryNames(MongoCollection<Document> collection, int count) {
		List<String> countryNames = new ArrayList<>();
		MongoIterable<String> allCountryNames = collection.distinct("name", String.class);
		for (String name : allCountryNames) {
			countryNames.add(name);
		}

		List<String> randomCountryNames = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < count; i++) {
			int randomIndex = random.nextInt(countryNames.size());
			randomCountryNames.add(countryNames.get(randomIndex));
		}

		return randomCountryNames;
	}

	private static Double getProperty(MongoCollection<Document> collection, String countryName, int userChoice) {
		Document document = collection.find(new Document("name", countryName)).first();
		if (document != null) {
			switch (userChoice) {
				case 1:
					return document.getDouble("population");
				case 2:
					return document.getDouble("surface_area");
				case 3:
					return document.getDouble("gdp");
				default:
					return 0.0;
			}
		}
		return 0.0;
	}

	private static boolean isHighest(Double chosenPropertyValue, List<String> randomCountryNames,
									 MongoCollection<Document> collection, int userChoice) {
		for (String countryName : randomCountryNames) {
			Double propertyValue = getProperty(collection, countryName, userChoice);
			if (propertyValue > chosenPropertyValue) {
				return false;
			}
		}
		return true;
	}

	private static String getPropertyDescription(int userChoice) {
		switch (userChoice) {
			case 1:
				return "population";
			case 2:
				return "surface area";
			case 3:
				return "GDP";
			default:
				return "";
		}
	}
}



