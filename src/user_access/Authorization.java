package user_access;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Authorization {
	private HashMap<String, String> tokens;
	private Secrets secrets;
	private String json = "";

	Authorization() throws Exception {
		secrets = new Secrets();	// Retrieves client id and client secret from a file on your storage device
		requestAuthorization();		// Requests access token and saves it to a HashMap
	}
	
	void requestAuthorization() throws Exception {
		URL url = new URL("https://accounts.spotify.com/api/token");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		
		String request = "Basic " + Base64.getEncoder().encodeToString(secrets.getClientID().getBytes()) + ":" + 
				Base64.getEncoder().encodeToString(secrets.getClientSecret().getBytes()) + "&grant_type=client_credentials" + 
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
		
		in.close();
		reader.close();
	}
	
	// getTokens: Returns the value of requested token 
	String getToken(String tokenRequest) {
		return tokens.get(tokenRequest);
	}
	
	// jsonToMap: Converts a JSON string to a HashMap
	HashMap<String, String> jsonToMap() {
		HashMap<String,String> temp = new Gson().fromJson(json, new TypeToken<HashMap<String,String>>(){}.getType());
		return temp;
	}
	
	
		

}