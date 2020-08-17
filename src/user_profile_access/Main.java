package user_profile_access;

public class Main {
	public static void main(String[] args) throws Exception {
		SpotifyAPI api = new SpotifyAPI();		
		api.createPlaylist();
		api.userPlaylists();
	}

}
