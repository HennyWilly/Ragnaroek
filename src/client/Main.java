package client;

import java.io.IOException;
import java.net.Socket;

import ai.AI;
import ai.SimpleAI;

public class Main {
	public static final String NAME = "Ragnaroek";

	/**
	 * Parst die uebergebenen Argumente und gibt, falls erfolgreich, einen Socket
	 * zum Server zurueck
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
				System.out.format("%s ist kein gueltiger Port\n", args[1]);
				return null;
			}

			Socket sock = null;
			try {
				sock = new Socket(host, port);
			} catch (IOException e) {
				System.out
						.format("Verbindung zu %s:%d konnte nicht hergestellt werden\n",
								host, port);
			}
			return sock;
		}

		showHelp();
		return null;
	}

	/**
	 * Gibt den Typ der benoetigten Kommandozeilenparameter auf der Konsole aus.
	 */
	private static void showHelp() {
		System.out
				.println("Es werden genau 2 Kommandozeilenargumente benoetigt");
		System.out.println("Hostname: Name oder IP-Adresse des Servers");
		System.out.println("Port: Portnummer die vom Server ueberwacht wird");
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

		AI solver = new SimpleAI();
		String name = String.format("%s.%s", NAME, solver.getClass()
				.getSimpleName());

		Game game = new Game(name, sock);
		int gameResult = game.solve(solver);
		if(gameResult == game.getID())			
			System.out.println("Congratz");
		else if(gameResult >= 1)
			System.out.format("gg to player %d\n", gameResult);
	}
}
