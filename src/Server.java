import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		try {
			initialize();
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("+A new client[" + clientSocket + "] is connected");
				Threader client = new Threader(clientSocket);
				client.start();
			}
		} catch (IOException e) {
			System.out.println("Problem with the server socket");
		}
	}
	public static void initialize() throws IOException {
		serverSocket = new ServerSocket(5000);

		System.out.println("server is booted up...");
	}
}
