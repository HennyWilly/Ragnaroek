package ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	private List<Position> getRadiusPositions(Position originalPos, int radius) {
		List<Position> positions = new ArrayList<Position>();

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

		return positions;
	}

	private Map<MoveMessageType, Board> getAppropriateMoveAfterShift(
			Board board, int playerID, Position targetPos, Position shiftPos,
			boolean shiftTarget) {
		PositionType forbiddenPos = board.getForbidden();

		if (!shiftPos.isInsertablePosition())
			throw new IllegalArgumentException("Invalid position of shiftcard");

		if (shiftPos.equals(forbiddenPos))
			throw new IllegalArgumentException(
					"Shiftcard mustn't be inserted at forbidden position");

		Card shiftCard = new Card(board.getShiftCard());

		MoveMessageType move = null;
		Board shadowBoard = null;

		Map<MoveMessageType, Board> possibleMoves = new HashMap<MoveMessageType, Board>();

		Position shiftedTarget = shiftTarget ? targetPos
				.getPositionAfterShift(shiftPos) : targetPos;

		if (shiftedTarget == null)
			return null;

		for (int k = 0; k < shiftCard.getDifferentRotationCount(); k++) {
			move = new MoveMessageType();
			move.setShiftPosition(shiftPos);
			move.setNewPinPos(shiftedTarget);
			move.setShiftCard(shiftCard);

			shadowBoard = (Board) board.clone();
			if (shadowBoard.validateTransition(move, playerID)) {
				// always call proceedTurn to apply changes to the
				// shadowBoard !!!
				shadowBoard.proceedTurn(move, playerID);

				possibleMoves.put(move, shadowBoard);
			}

			shiftCard = shiftCard.rotateClockwise();
		}

		return possibleMoves;
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
			Position playerPos, Position targetPos, int radius, int maxRadius) {
		HashMap<MoveMessageType, Board> possibleMoves = new HashMap<MoveMessageType, Board>();

		PositionType forbiddenPos = board.getForbidden();
		Card shiftCard = new Card(board.getShiftCard());

		boolean samePos = false;

		List<Position> radiusPositions = getRadiusPositions(targetPos, radius);
		for (Position wanted : radiusPositions) {
			if (wanted.equals(playerPos)) {
				// Break if current position is the same as the wanted position
				samePos = true;
				break;
			}

			for (Position probeShiftPos : Board.SHIFTPOSITIONS) {
				if (probeShiftPos.equals(forbiddenPos))
					continue;

				for (int k = 0; k < shiftCard.getDifferentRotationCount(); k++) {
					Map<MoveMessageType, Board> innerResult = getAppropriateMoveAfterShift(
							board, playerID, wanted, probeShiftPos, true);

					if (innerResult != null)
						possibleMoves.putAll(innerResult);
					shiftCard = shiftCard.rotateClockwise();
				}
			}
		}

		if (possibleMoves.size() > 0) {
			if (radius >= 1) {
				MoveMessageType possibleNext = null;

				Board possibleBoard = null;
				Position possibleTreasurePos = null;
				Position possiblePlayerPos = null;

				for (Entry<MoveMessageType, Board> entry : possibleMoves
						.entrySet()) {
					possibleBoard = entry.getValue();
					possibleTreasurePos = possibleBoard.getTreasurePos();
					if (possibleTreasurePos == null)
						continue;

					possiblePlayerPos = possibleBoard.findPlayer(playerID);

					possibleNext = getAppropriateRadiusMove(possibleBoard,
							playerID, possiblePlayerPos, possibleTreasurePos,
							0, radius - 1);

					if (possibleNext.getShiftPosition() != null) {
						return entry.getKey();
					}
				}
			}

			// randomly select one possible move to avoid selecting (row = 0,
			// col = 1) as shift position if there is already an existing path

			int randIndex = rand.nextInt(possibleMoves.size());
			int i = 0;
			for (MoveMessageType moveEntry : possibleMoves.keySet()) {
				if (randIndex == i)
					return moveEntry;
				i++;
			}
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
		MoveMessageType message = new MoveMessageType();

		Position treasurePos = board.getTreasurePos();
		if (treasurePos == null) {
			// Treasure on shift card

			Map<MoveMessageType, Board> moveMap = null;

			outer: for (Position probeShiftPos : Board.SHIFTPOSITIONS) {
				if (probeShiftPos.equals(forbiddenPos))
					continue;

				moveMap = getAppropriateMoveAfterShift(board, playerID,
						probeShiftPos, probeShiftPos, false);

				if (moveMap.size() > 0) {
					int randIndex = rand.nextInt(moveMap.size());
					int i = 0;
					for (MoveMessageType moveEntry : moveMap.keySet()) {
						if (randIndex == i) {
							message = moveEntry;
							break outer;
						}
						i++;
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
