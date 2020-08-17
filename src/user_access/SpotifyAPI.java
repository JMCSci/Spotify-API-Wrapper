/* Spotify API Wrapper
 * Request metadata w/o user profile access
 * Only endpoints that do not access user information can be accessed. 
 * The advantage here in comparison with requests to the Web API made without an access token, is that a higher rate limit is applied.
 */

// Client Credentials Grant Flow - Server-to-server authentication

package user_access;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import org.json.JSONObject;

public class SpotifyAPI {
	private HashMap<String, String> songPreview = new HashMap<>();
	private Authorization authorization;
	private JSONObject json;
	
	SpotifyAPI() throws Exception {
		authorization = new Authorization();
	}
	
	void getTrack(String trackId) throws Exception {
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
			
		reader.close();
	}
	
	void getAlbumTracks(String albumId) throws Exception {
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
		
		reader.close();
	}
	
	void searchForItem() throws Exception {
		String request = "q=track:Honey-Dipped%20artist:Dave%20Koz&type=track&limit=1&offset=0&market=US";
//		String request = "q=track:Puerto+Banus%20artist:The+Jazzmasters&type=track&limit=1&offset=0&market=US";
		URL url = new URL("https://api.spotify.com/v1/search?" + request);
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
		
		System.out.println(line.toString());
		
		json = new JSONObject(line.toString());
		
		reader.close();
		
		
	}
	
	void getPreview() throws Exception {
		String name = json.get("name").toString();
		String link = "";
		
		link = json.get("preview_url").toString();
		
		if(link.matches("null")) {
			System.out.println("Preview link not found");
			return;
		} 
		songPreview.put(name, link);
		savePreview(name);
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

}