import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private static Scanner console = new Scanner(System.in);
	private static Socket serverSocket;
	private static InetAddress serverIP;
	private static DataInputStream Input;
	private static DataOutputStream Output;

	private static void initialize() throws IOException {
		console = new Scanner(System.in);
		serverIP = InetAddress.getByName("localhost");
		serverSocket = new Socket(serverIP, 5000);
		Input = new DataInputStream(serverSocket.getInputStream());
		Output = new DataOutputStream(serverSocket.getOutputStream());
	}

	private static AuthenticationToken authenticate() throws IOException {
		String serverReply;
		serverReply = Input.readUTF(); //Enter Username:
		System.out.println(serverReply);
		String userName = console.nextLine();
		Output.writeUTF(userName);

		serverReply = Input.readUTF(); //Enter Password:
		System.out.println(serverReply);
		String Password = console.nextLine();
		Output.writeUTF(Password);

		serverReply = Input.readUTF(); //Logged in

		if (serverReply.equals("1")) {
			System.out.println("Logged in Successfully");
			return new AuthenticationToken(true, userName);
		} else {
			System.out.println(serverReply);
			return new AuthenticationToken(false, "");
		}
	}

	private static String chooseFileToDownload() throws IOException {
		String option;
		String serverReply;
		serverReply = Input.readUTF();
		//Printed Dirs
		while (!serverReply.equalsIgnoreCase("-1")) {
			System.out.println(serverReply);
			serverReply = Input.readUTF();
		}
		serverReply = Input.readUTF();
		System.out.println(serverReply);

		option = console.nextLine();
		Output.writeUTF(option);
		serverReply = Input.readUTF();

		//Printed Dirs2
		while (!serverReply.equalsIgnoreCase("-1")) {
			System.out.println(serverReply);
			serverReply = Input.readUTF();
		}
		serverReply = Input.readUTF();
		System.out.println(serverReply);

		String fileName = console.nextLine();
		Output.writeUTF(fileName);
		serverReply = Input.readUTF();
		System.out.println(serverReply);
		if (serverReply.equalsIgnoreCase("***File Not Found***")) {
			return "";
		} else {
			return fileName;
		}
	}

	private static void download(String userName, String filename) throws IOException {
		if (filename.isEmpty()) return;
		Socket FTPSocket = new Socket(serverIP, 6000);
		InputStream InputBytes = FTPSocket.getInputStream();
		BufferedInputStream inputFile = new BufferedInputStream(InputBytes);
		String serverReply = Input.readUTF(); // Download Started
		System.out.println(serverReply);

//		String filePath = ".\\" + userName + "Downloads\\" + filename;

		var outFileStream = new FileOutputStream(filename);
		BufferedOutputStream outFile = new BufferedOutputStream(outFileStream);

		byte[] buffer = new byte[1024];
		int byteRead;
		while ((byteRead = inputFile.read(buffer)) != -1) {
			outFile.write(buffer, 0, byteRead);
			outFile.flush();
		}
		System.out.println("File was successfully downloaded");
		outFile.close();
		FTPSocket.close();
	}

	static public void main(String[] args) {
		try {
			initialize();
			String serverReply = Input.readUTF();
			if (serverReply.equalsIgnoreCase("1")) {
				System.out.println("Connected to Server");
				String option = "Continue";
				var authenticatedToken = authenticate();
				while (!authenticatedToken.isAuthenticated) {
					authenticatedToken = authenticate();
				}
				while (option.equalsIgnoreCase("Continue")) {

					serverReply = Input.readUTF(); //Show Dir or Close
					System.out.println(serverReply);
					option = console.nextLine();
					if (!option.equalsIgnoreCase("Show")) {
						Output.writeUTF("Close");
						break;
					}
					Output.writeUTF("Show");
					String fileName = chooseFileToDownload();
					download(authenticatedToken.user, fileName);

					serverReply = Input.readUTF();
					System.out.println(serverReply);
					option = console.nextLine();
					if (serverSocket.isConnected()) Output.writeUTF(option);
				}
				Input.close();
				Output.close();
				serverSocket.close();
				System.out.println("The connection with server is closed");
			} else {
				System.out.println("Failed to connect to the Server");
			}
		} catch (UnknownHostException e) {
			System.out.println("Unknown Host");
		} catch (IOException e) {
			System.out.println("Problem in I/O of client socket.");
		}
	}
}
