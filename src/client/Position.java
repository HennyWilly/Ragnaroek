package client;

import generated.PositionType;

public class Position extends PositionType {

	public Position() {
		super();
		row = -1;
		col = -1;
	}

	public Position(PositionType p) {
		super();
		row = p.getRow();
		col = p.getCol();
	}

	public Position(int row, int col) {
		this.row = row;
		this.col = col;
	}

	// checkt ob an dieser Stelle ein Schieben moeglich ist
	public boolean isLoosePosition() {
		return ((row % 6 == 0 && col % 2 == 1) || (col % 6 == 0 && row % 2 == 1));
	}

	public boolean isOppositePosition(PositionType otherPosition) {
		Position op = new Position(otherPosition);
		return this.getOpposite().equals(op);
	}

	// gibt die gegenueberliegende
	// Position auf dem Spielbrett wieder
	public Position getOpposite() {
		if (row % 6 == 0) {
			return new Position((row + 6) % 12, col);
		} else if (col % 6 == 0) {
			return new Position(row, (col + 6) % 12);
		} else {
			return null;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;

		if (!PositionType.class.isAssignableFrom(obj.getClass()))
			return false;

		PositionType other = (PositionType) obj;
		return row == other.getRow() && col == other.getCol();
	}

	public String toString() {
		return String.format("row = %d; col = %d", this.row, this.col);
	}

	/**
	 * Returns a valid position which represents a shiftable position on the
	 * board on the specified side.
	 * 
	 * @param index
	 *            column or rowindex on the board depending on the specified
	 *            side
	 * @param side
	 *            0 = Top; 1 = Left; 2 = Bottom; 3 = Right
	 * @return a valid shift-position to be directly used in the calculations
	 */
	public static Position getValidShiftPos(int index, int side) {
		if (index < 0 || index > 6 || index % 2 == 0)
			throw new IllegalArgumentException("Invalid index to insert.");

		if (side < 0 || side >= 4)
			throw new IllegalArgumentException("Invalid side of board");

		Position probeShiftPos = new Position();

		switch (side) {
		// Top
		case 0:
			probeShiftPos.setCol(index);
			probeShiftPos.setRow(0);
			break;
		// Left
		case 1:
			probeShiftPos.setCol(0);
			probeShiftPos.setRow(index);
			break;
		// Bottom
		case 2:
			probeShiftPos.setCol(index);
			probeShiftPos.setRow(6);
			break;
		// Right
		case 3:
			probeShiftPos.setCol(6);
			probeShiftPos.setRow(index);
			break;
		}

		return probeShiftPos;
	}

	/**
	 * Returns a new position which represents the current after the shift
	 * 
	 * @param oldPos
	 *            position to be shifted
	 * @param shiftPos
	 *            shift position
	 * @return a new position after the shift, or null if the new position isn't
	 *         on the board
	 */
	public Position getPositionAfterShift(Position shiftPos) {
		int col = this.getCol();
		int row = this.getRow();
		if (col % 2 != 1 && row % 2 != 1)
			return new Position(this);

		int shiftCol = shiftPos.getCol();
		int shiftRow = shiftPos.getRow();

		if (shiftCol % 6 == 0 && row == shiftRow) {
			// Shiftcard inserted left or right
			col += Math.signum(col - shiftCol);
			if (col > 6 || col < 0)
				return null;
		} else if (shiftRow % 6 == 0 && col == shiftCol) {
			// Shiftcard inserted at top or bottom
			row += Math.signum(row - shiftRow);
			if (row > 6 || row < 0)
				return null;
		}

		return new Position(row, col);
	}

	/**
	 * Returns a new position which represents the current position after the
	 * shift. If the pin left the board, this method returns the opposite of its
	 * former position.
	 * 
	 * @param shiftPos
	 *            shift position
	 * @return a new position after the shift
	 */
	public Position getPinPositionAfterShift(Position shiftPos) {
		Position newPos = this.getPositionAfterShift(shiftPos);

		if (newPos == null) {
			newPos = this.getOpposite();
		}

		return newPos;
	}

}
