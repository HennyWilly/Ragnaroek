package ai;

import java.util.List;
import java.util.Random;

import client.Board;
import client.Card;
import generated.AwaitMoveMessageType;
import generated.BoardType;
import generated.CardType;
import generated.MoveMessageType;
import generated.PositionType;
import generated.TreasureType;
import generated.BoardType.Row;

public class SimpleAI implements AI {
	private static Random rand;

	static {
		rand = new Random();
	}

	private CardType doShift(BoardType board, PositionType shift) {
		int col = shift.getCol();
		int row = shift.getRow();

		CardType card = null;

		if (col == 0) {
			card = board.getRow().get(row).getCol().get(6);
		} else if (col == 6) {
			card = board.getRow().get(row).getCol().get(0);
		} else if (row == 0) {
			card = board.getRow().get(6).getCol().get(col);
		} else if (row == 6) {
			card = board.getRow().get(0).getCol().get(col);
		} else {
			// Falsche Position
			// TODO Fehlerbehandlung
		}

		return card;
	}

	/**
	 * Executes probing on a clone of the current board with all possible
	 * shift-positions until it finds an appropriate one that either creates a
	 * viable path to the treasure or preserves an already existing one. In case
	 * of the scenario being without a solution, the method returns an arbitrary
	 * (last one probed) position.
	 * 
	 * @param board
	 *            current board
	 * @param playerPos
	 *            current player position
	 * @param wantedCardPos
	 *            current wanted card position
	 * @return a shift-position that either creates a viable path to the
	 *         treasure or preserves an already existing one in case of
	 *         solvability
	 */
	private PositionType getAppropriateShiftPos(Board board,
			PositionType playerPos, PositionType wantedCardPos) {
		PositionType probeShiftPos = new PositionType();
		MoveMessageType probeMove = new MoveMessageType();
		Board shadowBoard;

		for (int i = 1; i < 6; i += 2) {
			for (int j = 0; j < 4; j++) {
				switch (j) {
				// Top
				case 0:
					probeShiftPos.setCol(i);
					probeShiftPos.setRow(0);
					break;
				// Left
				case 1:
					probeShiftPos.setCol(0);
					probeShiftPos.setRow(i);
					break;
				// Bottom
				case 2:
					probeShiftPos.setCol(i);
					probeShiftPos.setRow(6);
					break;
				// Right
				case 3:
					probeShiftPos.setCol(6);
					probeShiftPos.setRow(i);
					break;
				}

				// FIXME
				// Not sure, if complete or not
				shadowBoard = (Board) board.clone();
				probeMove.setShiftPosition(probeShiftPos);
				probeMove.setShiftCard(doShift(shadowBoard, probeShiftPos));

				if (shadowBoard.pathpossible(playerPos, wantedCardPos)) {
					// Use probeShiftPos as shiftPosition
					return probeShiftPos;
				}
			}
		}
		return probeShiftPos;
	}

	@Override
	public MoveMessageType move(int playerID, AwaitMoveMessageType data) {
		Board board = new Board(data.getBoard(), data.getTreasure());
		Card shiftCard = new Card(board.getShiftCard());
		PositionType playerPos = board.findPlayer(playerID);
		PositionType forbiddenPos = board.getForbidden();

		// TODO Mit Werten fuellen
		PositionType newPinPos = new PositionType();
		PositionType shiftPosition = new PositionType();

		if (board.getTreasure() == null) {
			// Keine Karte mehr auf dem Stapel des Spielers
			// => Alle Karten gesammelt
			// TODO Nun zum Startpunkt zurueckkehren
		}

		if (shiftCard.getTreasure() != null
				&& shiftCard.getTreasure().equals(board.getTreasure())) {
			// Schatz auf Shift-Karte
			// TODO Schiebe auf naechst moegliches Feld
		} else {
			// Schatz auf dem Spielfeld
			PositionType wantedCardPos = board.getTreasurePos();
			if (playerPos.equals(wantedCardPos)) {
				// Spieler steht bereits auf Schatz, z.B. wenn herausgeschoben
				// TODO Random Shift

				int index = 2 * (rand.nextInt() % 3) + 1; // ->1,3,5
				int side = rand.nextInt(4); // ->0,1,2,3

				int col = -1;
				int row = -1;
				switch (side) {
				case 0: // Oben
					row = 0;
					col = index;
					break;
				case 1: // Rechts
					row = index;
					col = 6;
					break;
				case 2: // Unten
					row = 6;
					col = index;
					break;
				case 3: // Links
					row = index;
					col = 0;
					break;
				}

				shiftPosition.setCol(col);
				shiftPosition.setRow(row);

				// TODO
				newPinPos = playerPos;
			} else {
				// Spieler muss noch zum Schatz gehen
				// TODO Die wirkliche Arbeit
				boolean reachable = board
						.pathpossible(playerPos, wantedCardPos);
				// Es existiert ein Weg zum Schatz
				// TODO Shift, sodass Weg nicht kaputt geht
				// TODO Shift, dass wenn moeglich ein Weg erzeugt wird
				shiftPosition = this.getAppropriateShiftPos(board, playerPos, wantedCardPos);
			}
		}

		MoveMessageType move = new MoveMessageType();
		move.setNewPinPos(newPinPos);
		move.setShiftPosition(shiftPosition);
		move.setShiftCard(doShift(board, shiftPosition));

		if (!board.validateTransition(move, playerID)) {
			System.out.println("Oops, da klappt was nicht");
		}

		return move;
	}
}
