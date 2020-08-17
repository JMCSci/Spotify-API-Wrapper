package user_profile_access;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Authorization {
	String state;
	String code;
	String json;
	int request = 0;
	Map<String, String> auth = new HashMap<>();
	private HashMap<String, String> refreshMap;
	private HashMap<String, String> tokens;
	private Secrets secrets;

	
	Authorization() throws Exception {
		secrets = new Secrets();
		createState();			// Create state - optional protection against attacks such as cross-site request forgery
		getRequest();			// Request authorization to access data
		authorization();		// Save authorization code and state to hash map - used to request access token
		requestAccessTokens();	// Obtains access and refresh tokens
	}
	
	// createState: Randomly generated alphanumeric string for security -- length 11
	void createState() {
		String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		int randomValue;
		StringBuilder stateString = new StringBuilder("");
		for(int i = 0; i < 11; i++) {
			randomValue = ThreadLocalRandom.current().nextInt(0, ALPHANUMERIC.length());
			stateString.append(ALPHANUMERIC.charAt(randomValue));
		}
		state = stateString.toString();
	}
	
	// getRequest: Request authorization to access data
	void getRequest() throws Exception {
		String reponseType = "code";								
		String redirect = "https://www.spotify.com/";
		String scope1 = "user-read-private";
		String scope2 = "user-read-email";			// Remove this
		String scope3 = "user-library-modify";
		String scope4 = "playlist-modify-public";
		String scope5 = "playlist-modify-private";
		String scope6 = "playlist-read-private";
		URL url = new URL("https://accounts.spotify.com/authorize?client_id=" + secrets.getClientID() + 
				"&response_type=" + reponseType + "&redirect_uri=" + redirect + 
				"&scope=" + scope1 + "%20" + scope2 + "%20" + scope3 + "%20" + scope4 +
				"%20" + scope5 + "%20" + scope6 + "&state=" + state);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.getContent();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		String line = "";
		StringBuilder appendedLine = new StringBuilder("");
		while((line = reader.readLine()) != null){
			appendedLine.append(line + "\n");
		}	
		
		// YOUR WEB DRIVER AND PATH TO FILE
		System.setProperty("YOUR WEB DRIVER HERE", "PATH TO FILE");
		FirefoxDriver driver = new FirefoxDriver();
		driver.get(conn.getURL().toString());
		
		/*  Wait for authorization */
		Thread.sleep(20000);		// 20 seconds	
		code = driver.getCurrentUrl();
		
		driver.quit();
		reader.close();
	}
	
	// authorization: Save authorization code and state to hash map - used to request access token
	void authorization() {
		if(code.contains("code")){
			String [] tokens1 = code.split("code=");
			String [] tokens2 = tokens1[1].split("&");
			String [] tokens3 = code.split("state=");
			auth.put("code",tokens2[0]);
			auth.put("state",tokens3[1]);
		} else {
			System.out.println("ERROR");
		}		
	}
	
	// requestAccessTokens: Request access and refresh tokens - saves to tokens HashMap
	void requestAccessTokens() throws Exception {
		URL url = new URL("https://accounts.spotify.com/api/token");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

		String request = "Basic " + Base64.getEncoder().encodeToString(secrets.getClientID().getBytes()) + ":" + 
				Base64.getEncoder().encodeToString(secrets.getClientSecret().getBytes()) + "&grant_type=authorization_code" + 
				"&code=" + auth.get("code") + "&redirect_uri=https://www.spotify.com/" + 
				"&client_id=" + secrets.getClientID() + "&client_secret=" + secrets.getClientSecret();
			
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(request.getBytes());
		out.close();
			
		InputStream in = conn.getErrorStream();
		if(in == null) {
			in = conn.getInputStream();
		}
			
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		String readLine = "";
		StringBuilder line = new StringBuilder("");
		while((readLine = reader.readLine()) != null) {
			line.append(readLine + "\n");
		}
						
		json = line.toString();
		tokens = jsonToMap();
			
		reader.close();	
	} 
		
	// requestRefresh: Request refreshed access tokens for (1) additional Web API request  
	void requestRefresh() throws Exception {
		URL url = new URL("https://accounts.spotify.com/api/token");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
		String request = "Basic " + Base64.getEncoder().encodeToString(secrets.getClientID().getBytes()) + ":" + 
				Base64.getEncoder().encodeToString(secrets.getClientSecret().getBytes()) + "&grant_type=refresh_token" + 
				"&refresh_token=" + tokens.get("refresh_token") + "&client_id=" + secrets.getClientID() + 
				"&client_secret=" + secrets.getClientSecret();
			
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(request.getBytes());
		out.close();
			
		InputStream in = conn.getErrorStream();
			
		if(in == null) {
			in = conn.getInputStream();
		} else {
			System.out.println("ERROR");
		}
			
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
		String readLine = "";
		StringBuilder line = new StringBuilder("");
		while((readLine = reader.readLine()) != null) {
			line.append(readLine + "\n");
		}
			
		clearJSON();
		if(refreshMap != null) {
			clearMap(refreshMap);
		}
		json = line.toString();
		refreshMap = jsonToMap();
		
		reader.close();	
		}
		
	// jsonToMap: Saves JSON string to a HashMap
	HashMap<String, String> jsonToMap() {
		HashMap<String,String> tempMap = new Gson().fromJson(json, new TypeToken<HashMap<String,String>>(){}.getType());
		return tempMap;
	}		
	
	// clearJSON: Clears json string variable
	void clearJSON() {
		json = "";
	}
	
	// clearMap: Clear hashmap -- prevents collisions
	void clearMap(HashMap<String, String> hashmap) {
		hashmap.clear();
	}
		
	// getTokens: Returns the value of requested token 
	String getToken(String tokenRequest) {
		return tokens.get(tokenRequest);
	}

}
