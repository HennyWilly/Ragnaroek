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

	// gibt die gegenüberliegende
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
		
		if(!PositionType.class.isAssignableFrom(obj.getClass()))
			return false;
		
		Position other = new Position((PositionType) obj);
		return row == other.row && 
				col == other.col;
	}

	public String toString() {
		return String.format("row = %d; col = %d", this.row, this.col);
	}
}
