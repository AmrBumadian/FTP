import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Threader extends Thread {
	private final Socket clientSocket;

	private DataInputStream fromClient;
	private DataOutputStream toClient;

	public Threader(Socket ClientSocket) {
		this.clientSocket = ClientSocket;
		initializeBuffers();
	}

	private void initializeBuffers() {
		try {
			toClient = new DataOutputStream(clientSocket.getOutputStream());
			fromClient = new DataInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	private AuthenticationToken authenticate() throws IOException {
		String userPath = "";
		while (true) {
			toClient.writeUTF("Enter Username: ");
			String username = fromClient.readUTF();
			toClient.writeUTF("Enter password: ");
			String clientInputPassword = fromClient.readUTF();

			boolean found = false;
			Scanner usersFile = new Scanner(new File("users_data.txt"));
			while (usersFile.hasNext()) {
				String dataRecord = usersFile.nextLine();
				String[] userData = dataRecord.split("@");
				if (userData[0].equals(username) && userData[1].equals(clientInputPassword)) {
					userPath = userData[2];
					found = true;
					break;
				}
			}
			usersFile.close();
			if (!found) {
				toClient.writeUTF("****Wrong Username-Password Combination***");
			} else {
				break;
			}
		}
		toClient.writeUTF("1");
		return new AuthenticationToken(true, userPath);
	}

	private String showAllDirectoriesOf(final String userPath) {
		File openedFolder = null;
		try {
			File Dirs = new File(userPath);
			ArrayList<String> folders = new ArrayList<>(List.of(Dirs.list()));

			toClient.writeUTF("Folders: ");
			for (String folder : folders) {
				toClient.writeUTF(folder);
			}
			toClient.writeUTF("-1");
			toClient.writeUTF("Choose Directory: ");
			String folderName = fromClient.readUTF();

			for (String folder : folders) {
				if (folderName.equalsIgnoreCase(folder)) {
					String newPath = userPath + "\\" + folder;
					openedFolder = new File(newPath);
					break;
				}
			}

		} catch (IOException e) {
			System.out.println("***Error Accessing user files***");
		}
		return showAllFilesOf(openedFolder);
	}

	private String showAllFilesOf(File openedDirectory) {
		String filePath = "";
		try {
			ArrayList<String> files = new ArrayList<>(List.of(openedDirectory.list()));
			for (String s : files) {
				toClient.writeUTF(s);
			}
			toClient.writeUTF("-1");
			toClient.writeUTF("Enter Filename to be Downloaded: ");
			String fileName = fromClient.readUTF();
			boolean found = false;
			for (String file : files) {
				if (fileName.equalsIgnoreCase(file)) {
					filePath = openedDirectory.getAbsolutePath() + "\\" + file;
					found = true;
					break;
				}
			}
			if (!found) toClient.writeUTF("***File Not Found***");
			else toClient.writeUTF("Download will start soon :)");
		} catch (IOException e) {
			System.out.println("***Error Accessing user files***");
		}
		return filePath;
	}

	private void sendFile(String filePath) throws IOException {
		if (filePath.isEmpty()) return;
		ServerSocket FTPSocket = new ServerSocket(6000);
		Socket FTPClient = FTPSocket.accept();
		BufferedOutputStream Output = new BufferedOutputStream(FTPClient.getOutputStream());

		File file = new File(filePath);
		toClient.writeUTF("Download Started :)");
		var fileReader = new BufferedInputStream(new FileInputStream(file));
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = fileReader.read(buffer)) != -1) {
			Output.write(buffer, 0, bytesRead);
			Output.flush();
		}
		fileReader.close();
		Output.close();
		FTPClient.close();
		FTPSocket.close();
	}

	public void run() {
		try {
			toClient.writeUTF("1");
			var authenticatedToken = authenticate();
			final String userPath = authenticatedToken.user;

			while (authenticatedToken.isAuthenticated) {
				String option;
				toClient.writeUTF("Enter 'Show' or 'Close'");
				option = fromClient.readUTF();
				if (!option.equalsIgnoreCase("Show")) break;
				String filePath = showAllDirectoriesOf(userPath);
				sendFile(filePath);
				toClient.writeUTF("Enter 'Continue' or 'Close'");
				option = fromClient.readUTF();
				if (!option.equalsIgnoreCase("Continue")) break;
			}
			closeConnection();
			System.out.println("-client[" + clientSocket + "] disconnected");
		} catch (Exception e) {
			System.out.println("***Error***");
		}
	}

	public void closeConnection() {
		try {
			clientSocket.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
