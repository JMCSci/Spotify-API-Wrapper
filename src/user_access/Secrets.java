package user_access;

import java.io.File;
import java.util.Scanner;

public class Secrets {
	private String clientID;
	private String clientSecret;
	
	Secrets() throws Exception {
		getClientInfo();	// Retrieves client id and client secret from storage device
	}
	
	
	void getClientInfo() throws Exception {
		Scanner sc = new Scanner(System.in);
		String fileLocation = "";
		System.out.print("Enter location of your client id and client secret: ");
		fileLocation = sc.nextLine();
		File file = new File(fileLocation);
		sc.close();
		if(file.exists()) {
			Scanner fileInput = new Scanner(file);
			clientID = fileInput.nextLine();
			clientSecret = fileInput.nextLine();
			fileInput.close();
		} else {
			System.out.println("File not found");
			System.exit(-1);
		}
	}
	
	String getClientID() {
		return clientID;
	}
	
	String getClientSecret() {
		return clientSecret;
	}

}