package client;

import generated.AwaitMoveMessageType;
import generated.ErrorType;
import generated.MazeCom;
import generated.MazeComType;
import generated.MoveMessageType;
import generated.WinMessageType;

import java.io.IOException;
import java.net.Socket;

import ai.AI;

import networking.Connection;
import networking.MazeComMessageFactory;

public class Game {
	private MazeComMessageFactory factory;
	private Connection connection;
	private int clientID;

	public Game(String name, Socket sock) throws IOException {
		factory = new MazeComMessageFactory();
		connection = new Connection(sock);

		MazeCom antwort = sendMessage(factory.createLoginMessage(name));
		switch (antwort.getMcType()) {
		case ACCEPT: // Falsche Nachricht
						// TODO Fehlerbehandlung
			break;
		case DISCONNECT: // Zu viele Login-Versuche
							// TODO Fehlerbehandlung
			break;
		case LOGINREPLY:
			clientID = antwort.getLoginReplyMessage().getNewID();
			break;
		default: // Unerwartete Nachricht
					// TODO Fehlerbehandlung
			break;
		}
	}
	
	int getID() {
		return clientID;
	}

	public Game(String name, String host, int port) throws IOException {
		this(name, new Socket(host, port));
	}

	private MazeCom sendMessage(MazeCom message) {
		connection.sendMessage(message);
		return connection.receiveMessage();
	}

	public int solve(AI solver) {
		MazeCom msg = null;

		do {
			msg = connection.receiveMessage();
			if (msg.getMcType() == MazeComType.AWAITMOVE) {
				// Speichere Daten des Spielfeldes
				AwaitMoveMessageType awaitMove = msg.getAwaitMoveMessage();

				// Errechne Zug aus Spielfeld-Daten
				MoveMessageType move = solver.move(clientID, awaitMove);

				// TODO remove dummy check
				Board board = new Board(awaitMove.getBoard(),
						awaitMove.getTreasure());
				if (!board.validateTransition(move, clientID)) {
					System.out.println("Oops, da klappt was nicht");
				}

				MazeCom result = sendMessage(factory.createMoveMessage(
						clientID, move));
				if (result == null) {
					System.err.println("Verbindung zum Server unterbrochen");
					return -1;
				}

				MazeComType type = result.getMcType();
				if (type == MazeComType.ACCEPT
						&& !result.getAcceptMessage().isAccept()) {
					ErrorType error = result.getAcceptMessage().getErrorCode();
					if (error == ErrorType.AWAIT_MOVE) {
						// Falsche Nachricht gesendet
						// TODO Fehlerbehandlung
					} else if (error == ErrorType.ILLEGAL_MOVE) {
						// Inkorrekter Zug
						// TODO Fehlerbehandlung
					}
				} else if (type == MazeComType.DISCONNECT) {
					// Zu viele Versuche
					// TODO Fehlerbehandlung
				}
			} else if (msg.getMcType() == MazeComType.DISCONNECT) {
				System.out
						.println("Die Verbindung wurde Serverseitig beendet.");
				System.out.format("Grund: %s\n", msg.getDisconnectMessage()
						.getErroCode());

				return -1;
			}
		} while (msg.getMcType() != MazeComType.WIN);

		WinMessageType winMessage = msg.getWinMessage();
		return winMessage.getWinner().getId();
	}
}
