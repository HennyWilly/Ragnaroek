package networking;

import generated.MazeCom;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Connection {
	private Socket clientSocket;
	private XmlOutStream os;
	private XmlInStream is;
	
	public Connection(Socket sock) throws IOException {
		clientSocket = sock;
		
		is = new XmlInStream(clientSocket.getInputStream());
		os = new XmlOutStream(clientSocket.getOutputStream());
	}
	
	public void sendMessage(MazeCom message) {
		os.write(message);
	}
	
	public MazeCom receiveMessage() {
		try {
			return is.readMazeCom();
		} catch (EOFException e) {
			//TODO Richtige Fehlerbehandlung
			return null;
		} catch (SocketException e) {
			//TODO Richtige Fehlerbehandlung
			return null;
		}
	}
}
