package client;

import java.io.IOException;
import java.net.Socket;

import ai.SimpleAI;

public class Main {
	/**
	 * Parst die übergebenen Argumente und gibt, falls erfolgreich, einen Socket
	 * zum Server zurück
	 * 
	 * @param args
	 *            Zu parsende Argumente
	 * @return Socket zum Server
	 */
	private static Socket parseArgs(String[] args) {
		if (args != null && args.length == 2) {
			String host = args[0];
			int port = -1;
			try {
				port = Integer.parseInt(args[1]);
				if (port < 0 || port > 65535) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException ex) {
				System.out.format("%s ist kein gültiger Port\n", args[1]);
				return null;
			}

			Socket sock = null;
			try {
				sock = new Socket(host, port);
			} catch (IOException e) {
				System.out.format(
						"Verbindung zu %s:%d konnte nicht hergestellt werden\n",
						host, port);
			}
			return sock;
		}

		showHelp();
		return null;
	}

	/**
	 * Gibt den Typ der benötigten Kommandozeilenparameter auf der Konsole aus.
	 */
	private static void showHelp() {
		System.out.println("Es werden genau 2 Kommandozeilenargumente benötigt");
		System.out.println("Hostname: Name oder IP-Adresse des Servers");
		System.out.println("Port: Portnummer die vom Server überwacht wird");
	}

	/**
	 * Einstiegspunkt
	 * 
	 * @param args
	 *            Kommandozeilenparameter
	 * @throws IOException
	 *             Verbindungbezogener Fehler
	 */
	public static void main(String[] args) throws IOException {
		Socket sock = parseArgs(args);
		if (sock == null)
			return;

		Game game = new Game("Ragnarök", sock);
		if(game.solve(new SimpleAI()))
			System.out.println("Congratz");
	}
}
