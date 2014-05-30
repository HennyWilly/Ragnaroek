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

	private MoveMessageType getAppropriateRadiusMove(Board board, int playerID,
			PositionType playerPos, PositionType targetPos, int radius,
			int maxRadius) {
		PositionType forbiddenPos = board.getForbidden();
		Card shiftCard = new Card(board.getShiftCard());

		Position probeShiftPos = null;
		Board shadowBoard;

		boolean samePos = false;

		MoveMessageType move = new MoveMessageType();

		Position[] radiusPositions = getRadiusPositions(targetPos,
				radius);
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

					for (int k = 0; k < shiftCard.getDifferentRotationCount(); k++) {					
						move.setShiftPosition(probeShiftPos);
						move.setNewPinPos(wanted);
						move.setShiftCard(shiftCard);

						// FIXME
						// Not sure, if complete or not
						shadowBoard = (Board) board.clone();
						if (shadowBoard.validateTransition(move, playerID)) {
							if (radius > 0
									|| shadowBoard.proceedTurn(move, playerID)) {
								// Use probeShiftPos as shiftPosition
								return move;
							}
						}

						shiftCard = shiftCard.rotateClockwise();
					}
				}
			}
		}

		if (!samePos && radius < maxRadius)
			return getAppropriateRadiusMove(board, playerID, playerPos,
					targetPos, radius + 1, maxRadius);

		move.setShiftPosition(null);
		return move;
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
	 * @param playerPos
	 *            current player position
	 * @param wantedCardPos
	 *            current wanted card position
	 * @return a shift-position that either creates a viable path to the
	 *         treasure or preserves an already existing one in case of
	 *         solveability
	 */
	private MoveMessageType getAppropriateMove(Board board, int playerID,
			PositionType playerPos) {
		PositionType forbiddenPos = board.getForbidden();
		MoveMessageType message = null;

		PositionType treasurePos = board.getTreasurePos();
		if (treasurePos == null) {
			// TODO Treasure on ShiftCard

			int radius = 0;
			
			outer:
			while (radius <= 6) {
				Position probeShiftPos = null;
				for (int i = 1; i < 6; i += 2) {
					for (int j = 0; j < 4; j++) {
						probeShiftPos = Position.getValidShiftPos(i, j);

						if (probeShiftPos.equals(forbiddenPos))
							continue;

						// FIXME Still buggy
						message = getAppropriateRadiusMove(board, playerID,
								playerPos, probeShiftPos, radius, radius);
						break outer;
					}
				}
				
				radius++;
			}
		} else {
			message = getAppropriateRadiusMove(board, playerID, playerPos,
					board.getTreasurePos(), 0, 6);
		}

		if (message.getShiftPosition() == null) {
			int index;
			int side;
			do {
				index = 2 * rand.nextInt(3) + 1; // ->1,3,5
				side = rand.nextInt(4); // ->0,1,2,3

				message.setShiftPosition(Position.getValidShiftPos(index, side));
			} while (message.getShiftPosition().equals(forbiddenPos));
			message.setNewPinPos(playerPos);
			message.setShiftCard(board.getShiftCard());
		}

		return message;
	}

	@Override
	public MoveMessageType move(int playerID, AwaitMoveMessageType data) {
		Board board = new Board(data.getBoard(), data.getTreasure());
		PositionType playerPos = board.findPlayer(playerID);
		MoveMessageType move = this.getAppropriateMove(board, playerID,
				playerPos);
		if (!board.validateTransition(move, playerID)) {
			System.out.println("Oops, da klappt was nicht");
		}

		return move;
	}
}
