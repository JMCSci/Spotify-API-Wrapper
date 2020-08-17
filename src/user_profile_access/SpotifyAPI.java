/* Spotify Web API Wrapper
 * Request w/o user profile access
 * Uses Firefox WebDriver and Selenium API
 * This flow is suitable for long-running applications in which the user grants permission only once. 
 * It provides an access token that can be refreshed. 
 * Since the token exchange involves sending your secret key, perform this on a secure location, like a backend service
 * Do not use from a client such as a browser or from a mobile app.
 */

// Authorization Grant Flow - Refreshable User Authorization

package user_profile_access;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SpotifyAPI {
	private HashMap<String, String> songPreview = new HashMap<>();
	private LinkedHashMap<String, String> currentUser = new LinkedHashMap<>();
	private Authorization authorization;
	private int requests = 0;
	private JSONObject json;
	
	SpotifyAPI() throws Exception {
		authorization = new Authorization();	// Request Spotify Web API authorization
		addCurrentUser();						// Retrieve current user data
	}
	
	// addCurrentUser: Adds current user data to HashMap
	void addCurrentUser() throws Exception {
		if(requests >= 1) {			// Automatically ask for for refresh tokens after first API request
			authorization.requestRefresh();
		}
		if(currentUser.isEmpty()) {
			URL url = new URL("https://api.spotify.com/v1/me/");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(false);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", authorization.getToken("token_type") + 
					" " + authorization.getToken("access_token"));
			
			// Read the response - JSON
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String readLine = "";
			StringBuilder line = new StringBuilder();
			while((readLine = reader.readLine()) != null){
				line.append(readLine);
			}
			
			clearJSON();
			json = new JSONObject(line.toString());
			JSONObject nestedFollow = new JSONObject(json.optString("followers"));	// access nested data under this category
			
			currentUser.put("id", json.get("id").toString());
			currentUser.put("display_name", json.get("display_name").toString());
			currentUser.put("product", json.get("product").toString());
			currentUser.put("type", json.get("type").toString());
			currentUser.put("followers", nestedFollow.get("total").toString());
			System.out.println(currentUser);
			reader.close();
		} else {
			System.out.println("Current user data has already been retrieved");
		}
		requests++;
	}
	
	void createPlaylist() throws Exception {
		if(requests >= 1) {			// Automatically ask for for refresh tokens after first API request
			authorization.requestRefresh();
		}
		System.out.println(authorization.getToken("token_type") + "\n" + authorization.getToken("access_token"));
		URL url = new URL("https://api.spotify.com/v1/users/" + currentUser.get("id") + "/playlists");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", authorization.getToken("token_type") + " " +
				authorization.getToken("access_token"));
		
		String postRequest = "{\n\"name\":\"Pandora Liked Songs\",\n" + 
				"\"public\":false\n" + "}";
		
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(postRequest.getBytes());
		out.close();
		
		InputStream in = conn.getInputStream();
		
		String readLine = "";
		StringBuilder line = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		while((readLine = reader.readLine()) != null) {
			line.append(readLine);
		}
		
		json = new JSONObject(line.toString());
		
		System.out.println(json.toString(1));
	
		reader.close();
	}
	
	void userPlaylists() throws Exception {
		URL url = new URL("https://api.spotify.com/v1/me/playlists");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", authorization.getToken("token_type") + 
				" " + authorization.getToken("access_token"));
		
		String readline = "";
		StringBuilder line = new StringBuilder();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		while((readline = reader.readLine())!= null) {
			line.append(readline);
		}
		
		json = new JSONObject(line.toString());
		
		System.out.println(json.toString(1));
	
		parseJsonArray(json);
		
		reader.close();
	}
	
	void parseJsonArray(JSONObject json) {
		String playlistID = "";
		JSONArray arr = new JSONArray(json.get("items").toString());				// Create JSON array from JSON object
		int size = arr.length();													// Get the length of the array
		for(int i = 0; i < size; i++) {
			JSONObject obj = new JSONObject(arr.get(i).toString());	   			    // Add array object to JSON object
			if(obj.get("name").toString().matches("Pandora Liked Songs")) {		  	// Get the name in this JSON
				playlistID = obj.get("id").toString();								// Get the playlist id
			} 
		}
		currentUser.put("playlist_id", playlistID);									// add playlist id to hash map
		System.out.println(playlistID);
	}
	
	
	void getTrack(String trackId) throws Exception {
		if(requests >= 1) {			// Automatically ask for for refresh tokens after first API request
			authorization.requestRefresh();
		}
		URL url = new URL("https://api.spotify.com/v1/tracks/" + trackId);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", authorization.getToken("token_type") + " " + authorization.getToken("access_token"));

		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));	
		String readLine = "";
		StringBuilder line = new StringBuilder("");
		while((readLine = reader.readLine()) != null) {
			line.append(readLine);
		}
		
		clearJSON();
		
		json = new JSONObject(line.toString());
		System.out.println(json.toString(1));
		requests++;
		reader.close();
	}
	
	void addToPlaylist() throws Exception {
		if(requests >= 1) {		// Automatically ask for for refresh tokens after first API request
			authorization.requestRefresh();
		}
		URL url = new URL("https://api.spotify.com/v1/playlists/" + "40atSHkX65iOxci7wUACyj" + "/tracks");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", authorization.getToken("token_type") + " "
				+ authorization.getToken("access_token"));
		
		String postRequest = "{\"uris\":[\"spotify:track:4YohGkPPpo7YoZBLu5Yfux\",\"spotify:track:6dRCHL8tSG8yuZ53ZKGwdI\",\"spotify:track:2bKsGQ1zCGuUK2bjvovZ92\"]}";
		
		DataOutputStream out = new DataOutputStream(conn.getOutputStream());
		out.write(postRequest.getBytes());
		out.close();
		
		System.out.println(conn.getResponseCode());
	}
	
	void getAlbumTracks(String albumId) throws Exception {
		if(requests >= 1) {		// Automatically ask for for refresh tokens after first API request
			authorization.requestRefresh();
		}
		URL url = new URL("https://api.spotify.com/v1/albums/" + albumId + "/tracks");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", authorization.getToken("token_type") + " " + authorization.getToken("access_token"));
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String readLine = "";
		StringBuilder line = new StringBuilder();
		
		while((readLine = reader.readLine()) != null) {
			line.append(readLine);
		}
		
		json = new JSONObject(line.toString());
		System.out.println(json.toString(1));
		requests++;
		reader.close();
	}
	
	String searchForItem(String request) throws Exception {
		String query = "q=track:Honey-Dipped%20artist:Dave%20Koz&type=track&limit=1&offset=0&market=US";
		
		URL url = new URL("https://api.spotify.com/v1/search?" + query);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Authorization", authorization.getToken("token_type") + " " + authorization.getToken("access_token"));

		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String readLine = "";
		StringBuilder line = new StringBuilder("");
		
		while((readLine = reader.readLine()) != null) {
			line.append(readLine);
		}
		
		String jsonString = line.toString();
		
		reader.close();
		return jsonString;
	}
	
	void getPreview() throws Exception {
		if(requests >= 1) {		// Automatically ask for for refresh tokens after first API request
			authorization.requestRefresh();
		}

		String name = json.get("name").toString();
		String link = "";
		
		link = json.get("preview_url").toString();
		
		if(link.matches("null")) {
			System.out.println("Preview link not found");
			return;
		} 
		songPreview.put(name, link);
		savePreview(name);
		requests++;
	}
	
	void savePreview(String name) throws Exception {
		if(songPreview.containsKey((name))) {
			URL url = new URL(songPreview.get(name));
			InputStream in = url.openStream();
			FileOutputStream out = new FileOutputStream(name + ".mp3");
			in.transferTo(out);
			System.out.println("Preview downloaded");
			in.close();
			out.close();
			return;
		}
		System.out.println("Preview link not found");
	}
	
	void clearJSON() {
		json = null;
	}
	
	void clearMap(HashMap<String, String> hashmap) {
		hashmap.clear();
	}

}
