package ai;

import java.util.ArrayList;
import java.util.Random;

import client.Board;
import client.Card;
import client.Position;
import generated.AwaitMoveMessageType;
import generated.MoveMessageType;
import generated.PositionType;

public class SimpleAI implements AI {
	private static Random rand;

	static {
		rand = new Random();
	}

	/**
	 * Returns all positions that have the specified distance to the specified
	 * position.
	 * 
	 * @param originalPos
	 *            target position
	 * @param radius
	 *            radius around the target position
	 * @return All Position-objects that have the specified distance to the
	 *         specified position
	 */
	private Position[] getRadiusPositions(PositionType originalPos, int radius) {
		ArrayList<Position> positions = new ArrayList<Position>();

		int col = originalPos.getCol();
		int row = originalPos.getRow();

		for (int i = col - radius; i <= col + radius; i++) {
			if (i < 0 || i > 6)
				continue;

			for (int j = row - radius; j <= row + radius; j++) {
				if (j < 0 || j > 6)
					continue;

				if (i != col - radius && i != col + radius) {
					if (j != row - radius && j != row + radius)
						continue;
				}

				positions.add(new Position(j, i));
			}
		}

		return positions.toArray(new Position[positions.size()]);
	}

	/**
	 * Executes probing on a clone of the current board with all possible
	 * shift-positions until it finds an appropriate one that creates a viable
	 * path to a position within the specified radius to the treasure or
	 * preserves an already existing one. This method calls itself recursivly,
	 * until the specified radius reaches the specified maximal radius.
	 * 
	 * @param board
	 *            current board
	 * @param playerID
	 *            current player ID
	 * @param playerPos
	 *            current player position
	 * @param targetPos
	 *            current wanted card position
	 * @param radius
	 *            distance of the target position to the treasure
	 * @param maxRadius
	 *            maximum distance to limit recursive calls
	 * @return a move-message that either creates a viable path to a position
	 *         within a radius to the treasure or preserves an already existing
	 *         one in case of solveability.
	 */
	private MoveMessageType getAppropriateRadiusMove(Board board, int playerID,
			PositionType playerPos, PositionType targetPos, int radius,
			int maxRadius) {
		ArrayList<MoveMessageType> possibleMoves = new ArrayList<MoveMessageType>();

		PositionType forbiddenPos = board.getForbidden();
		Card shiftCard = new Card(board.getShiftCard());

		Position probeShiftPos = null;
		Position shiftedWanted = null;
		Board shadowBoard;

		boolean samePos = false;

		MoveMessageType move = null;

		Position[] radiusPositions = getRadiusPositions(targetPos, radius);
		for (Position wanted : radiusPositions) {
			if (wanted.equals(playerPos)) {
				// Break if current position is the same as the wanted position
				samePos = true;
				break;
			}

			for (int i = 1; i < 6; i += 2) {
				for (int j = 0; j < 4; j++) {
					probeShiftPos = Position.getValidShiftPos(i, j);

					if (probeShiftPos.equals(forbiddenPos))
						continue;

					shiftedWanted = wanted.getPositionAfterShift(probeShiftPos);
					if (shiftedWanted == null)
						continue;

					for (int k = 0; k < shiftCard.getDifferentRotationCount(); k++) {
						move = new MoveMessageType();
						move.setShiftPosition(probeShiftPos);
						move.setNewPinPos(shiftedWanted);
						move.setShiftCard(shiftCard);

						shadowBoard = (Board) board.clone();
						if (shadowBoard.validateTransition(move, playerID)) {
							if (radius > 0
									|| shadowBoard.proceedTurn(move, playerID)) {
								possibleMoves.add(move);
							}
						}

						shiftCard = shiftCard.rotateClockwise();
					}
				}
			}
		}

		if (possibleMoves.size() > 0) {
			// randomly select one possible move to avoid selecting (row = 0,
			// col = 1) as shift position if there is already an existing path

			return possibleMoves.get(rand.nextInt(possibleMoves.size()));
		}

		if (!samePos && radius < maxRadius)
			return getAppropriateRadiusMove(board, playerID, playerPos,
					targetPos, radius + 1, maxRadius);

		return new MoveMessageType();
	}

	/**
	 * Executes probing on a clone of the current board with all possible
	 * shift-positions until it finds an appropriate one that either creates a
	 * viable path to the treasure or preserves an already existing one. If
	 * there is no valid path, the algorithm tries to find a path to the closest
	 * neighbor of the treasure. In case of the scenario being without a
	 * solution, the method returns a random position.
	 * 
	 * @param board
	 *            current board
	 * @param playerID
	 *            current player ID
	 * @param playerPos
	 *            current player position
	 * @param wantedCardPos
	 *            current wanted card position
	 * @return a move-message that either creates a viable path to the treasure
	 *         or preserves an already existing one in case of solveability.
	 */
	private MoveMessageType getAppropriateMove(Board board, int playerID,
			Position playerPos) {
		PositionType forbiddenPos = board.getForbidden();
		MoveMessageType message = null;

		PositionType treasurePos = board.getTreasurePos();
		if (treasurePos == null) {
			// Treasure on shift card
			
			Position probeShiftPos = null;

			outer: 
			for(int radius = 0; radius <= 6; radius++) {
				for (int i = 1; i < 6; i += 2) {
					for (int j = 0; j < 4; j++) {
						probeShiftPos = Position.getValidShiftPos(i, j);

						if (probeShiftPos.equals(forbiddenPos))
							continue;

						message = getAppropriateRadiusMove(board, playerID,
								playerPos, probeShiftPos, radius, radius);

						if (message.getShiftPosition() != null)
							break outer;
					}
				}
			}
		} else {
			message = getAppropriateRadiusMove(board, playerID, playerPos,
					board.getTreasurePos(), 0, 6);
		}

		if (message.getShiftPosition() == null) {
			Position shiftPos = null;
			Position shiftedPlayerPos = null;

			int index;
			int side;
			do {
				index = 2 * rand.nextInt(3) + 1; // ->1,3,5
				side = rand.nextInt(4); // ->0,1,2,3

				shiftPos = Position.getValidShiftPos(index, side);
				shiftedPlayerPos = playerPos.getPinPositionAfterShift(shiftPos);
			} while (shiftPos.equals(forbiddenPos));

			message.setShiftPosition(shiftPos);
			message.setNewPinPos(shiftedPlayerPos);
			message.setShiftCard(board.getShiftCard());
		}

		return message;
	}

	@Override
	public MoveMessageType move(int playerID, AwaitMoveMessageType data) {
		Board board = new Board(data.getBoard(), data.getTreasure());
		Position playerPos = board.findPlayer(playerID);
		MoveMessageType move = this.getAppropriateMove(board, playerID,
				playerPos);

		return move;
	}
}
