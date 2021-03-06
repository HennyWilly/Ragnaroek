package client;

import generated.BoardType;
import generated.CardType;
import generated.CardType.Openings;
import generated.CardType.Pin;
import generated.MoveMessageType;
import generated.PositionType;
import generated.TreasureType;

import java.util.ArrayList;
import java.util.List;

public class Board extends BoardType {
	public static final Position[] SHIFTPOSITIONS;
	static {
		List<Position> list = new ArrayList<Position>(12);
		
		for (int i = 1; i < 6; i += 2) {
			for (int j = 0; j < 4; j++) {
				list.add(Position.getValidShiftPos(i, j));
			}
		}
		
		SHIFTPOSITIONS = list.toArray(new Position[list.size()]);
	}
	
	private TreasureType currentTreasure;

	public Board(BoardType old, TreasureType treasure) {
		currentTreasure = treasure;
		forbidden = old.getForbidden();
		shiftCard = old.getShiftCard();

		for (int i = 0; i < 7; i++) {
			this.getRow().add(i, new Row());
			for (int j = 0; j < 7; j++) {
				CardType card = old.getRow().get(i).getCol().get(j);
				this.getRow().get(i).getCol().add(j, new Card(card));
			}
		}
	}

	protected void setCard(int row, int col, Card c) {
		// Muss ueberschrieben werden, daher zuerst entfernen und dann...
		this.getRow().get(row).getCol().remove(col);
		// ...hinzufuegen
		this.getRow().get(row).getCol().add(col, c);
	}

	public Card getCard(int row, int col) {
		return (Card) this.getRow().get(row).getCol().get(col);
	}

	// Fuehrt nur das Hereinschieben der Karte aus!!!
	protected void proceedShift(MoveMessageType move) {
		Position sm = new Position(move.getShiftPosition());
		if (sm.getCol() % 6 == 0) { // Col=6 oder 0
			if (sm.getRow() % 2 == 1) {
				// horizontal schieben
				int row = sm.getRow();
				int start = (sm.getCol() + 6) % 12; // Karte die rausgenommen
													// wird
				setShiftCard(getCard(row, start));

				if (start == 6) {
					for (int i = 6; i > 0; --i) {
						setCard(row, i, new Card(getCard(row, i - 1)));
					}
				} else {// Start==0
					for (int i = 0; i < 6; ++i) {
						setCard(row, i, new Card(getCard(row, i + 1)));
					}
				}
			}
		} else if (sm.getRow() % 6 == 0) {
			if (sm.getCol() % 2 == 1) {
				// vertikal schieben
				int col = sm.getCol();
				int start = (sm.getRow() + 6) % 12; // Karte die rausgenommen
													// wird
				setShiftCard(getCard(start, col));
				if (start == 6) {
					for (int i = 6; i > 0; --i) {
						setCard(i, col, new Card(getCard(i - 1, col)));
					}
				} else {// Start==0
					for (int i = 0; i < 6; ++i) {
						setCard(i, col, new Card(getCard(i + 1, col)));
					}
				}

			}
		}
		forbidden = sm.getOpposite();
		Card c = new Card(move.getShiftCard());
		// Wenn Spielfigur auf neuer shiftcard steht,
		// muss dieser wieder aufs Brett gesetzt werden
		// Dazu wird Sie auf die gerade hereingeschoben
		// Karte gesetzt
		if (!shiftCard.getPin().getPlayerID().isEmpty()) {
			// Figur zwischenspeichern
			Pin temp = shiftCard.getPin();
			// Figur auf SchiebeKarte loeschen
			shiftCard.setPin(new Pin());
			// Zwischengespeicherte Figut auf
			// neuer Karte plazieren
			c.setPin(temp);
		}
		setCard(sm.getRow(), sm.getCol(), c);
	}

	// gibt zurueck ob mit dem Zug der aktuelle Schatz erreicht wurde
	public boolean proceedTurn(MoveMessageType move, Integer currPlayer) {
		// XXX ACHTUNG wird nicht mehr auf Richtigkeit ueberprueft!!!
		this.proceedShift(move);
		Position target = new Position(move.getNewPinPos());
		movePlayer(target, currPlayer);
		Card c = new Card(getCard(target.getRow(), target.getCol()));
		return (c.getTreasure() == currentTreasure);

	}

	public boolean movePlayer(PositionType newPos, Integer playerID) {
		Position oldPos = findPlayer(playerID);
		
		if(!pathpossible(oldPos, newPos))
			return false;
		
		getCard(oldPos.getRow(), oldPos.getCol()).getPin().getPlayerID()
				.remove(playerID);
		getCard(newPos.getRow(), newPos.getCol()).getPin().getPlayerID()
				.add(playerID);
		
		return true;
	}

	public Board fakeShift(MoveMessageType move) {
		Board fake = (Board) this.clone();
		fake.proceedShift(move);
		return fake;
	}

	public Board fakeShift(PositionType shiftPos, CardType shiftCard) {
		MoveMessageType move = new MoveMessageType();

		move.setShiftCard(shiftCard);
		move.setShiftPosition(shiftPos);

		return fakeShift(move);
	}

	@Override
	public Object clone() {
		Board clone = new Board(this, currentTreasure);
		if (forbidden == null) {
			clone.forbidden = null;
		} else {
			clone.forbidden = new Position(this.forbidden);
		}
		clone.shiftCard = new Card(this.shiftCard);

		return clone;
	}

	public boolean validateTransition(MoveMessageType move, Integer playerID) {
		// Ueberpruefen ob das Reinschieben der Karte gueltig ist

		Position sm = new Position(move.getShiftPosition());
		if (!sm.isInsertablePosition() || sm.equals(forbidden)) {
			System.err.println("Warning: verbotene Position der Schiebekarte");
			return false;
		}
		Card sc = new Card(move.getShiftCard());
		if (!sc.equals(shiftCard)) {
			System.err
					.println("Warning: Schiebekarte wurde illegal veraendert");
			return false;
		}
		// Ueberpruefen ob der Spielzug gueltig ist
		Board fake = this.fakeShift(move);
		Position playerPosition = new Position(fake.findPlayer(playerID));
		return fake.pathpossible(playerPosition, move.getNewPinPos());
	}

	public boolean pathpossible(PositionType oldPos, PositionType newPos) {
		if (oldPos == null || newPos == null)
			return false;
		Position oldP = new Position(oldPos);
		Position newP = new Position(newPos);
		return getAlleEreichbarenNachbarn(oldP).contains(newP);
	}

	public List<Position> getAlleEreichbarenNachbarn(Position position) {
		List<Position> erreichbarePositionen = new ArrayList<Position>();
		int[][] erreichbar = new int[7][7];
		erreichbar[position.getRow()][position.getCol()] = 1;
		erreichbar = getAlleErreichbarenNachbarnMatrix(position, erreichbar);
		for (int i = 0; i < erreichbar.length; i++) {
			for (int j = 0; j < erreichbar[0].length; j++) {
				if (erreichbar[i][j] == 1) {
					erreichbarePositionen.add(new Position(i, j));
				}
			}
		}
		return erreichbarePositionen;
	}

	private int[][] getAlleErreichbarenNachbarnMatrix(PositionType position,
			int[][] erreichbar) {
		for (PositionType p1 : getDirektErreichbareNachbarn(position)) {
			if (erreichbar[p1.getRow()][p1.getCol()] == 0) {
				erreichbar[p1.getRow()][p1.getCol()] = 1;
				getAlleErreichbarenNachbarnMatrix(p1, erreichbar);
			}
		}
		return erreichbar;
	}

	private List<PositionType> getDirektErreichbareNachbarn(
			PositionType position) {
		List<PositionType> positionen = new ArrayList<PositionType>();
		CardType k = this.getCard(position.getRow(), position.getCol());
		Openings openings = k.getOpenings();
		if (openings.isLeft()) {
			if (position.getCol() - 1 >= 0
					&& getCard(position.getRow(), position.getCol() - 1)
							.getOpenings().isRight()) {
				positionen.add(new Position(position.getRow(), position
						.getCol() - 1));
			}
		}
		if (openings.isTop()) {
			if (position.getRow() - 1 >= 0
					&& getCard(position.getRow() - 1, position.getCol())
							.getOpenings().isBottom()) {
				positionen.add(new Position(position.getRow() - 1, position
						.getCol()));
			}
		}
		if (openings.isRight()) {
			if (position.getCol() + 1 <= 6
					&& getCard(position.getRow(), position.getCol() + 1)
							.getOpenings().isLeft()) {
				positionen.add(new Position(position.getRow(), position
						.getCol() + 1));
			}
		}
		if (openings.isBottom()) {
			if (position.getRow() + 1 <= 6
					&& getCard(position.getRow() + 1, position.getCol())
							.getOpenings().isTop()) {
				positionen.add(new Position(position.getRow() + 1, position
						.getCol()));
			}
		}
		return positionen;
	}

	public Position findPlayer(Integer PlayerID) {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				Pin pinsOnCard = getCard(i, j).getPin();
				for (Integer pin : pinsOnCard.getPlayerID()) {
					if (pin == PlayerID) {
						return new Position(i, j);
					}
				}
			}

		}
		// Pin nicht gefunden
		return null;
	}

	public TreasureType getTreasure() {
		return currentTreasure;
	}

	public Position getTreasurePos() {
		List<Row> rows = getRow();
		List<CardType> cols;
		Row row;
		CardType card;
		TreasureType cardTreasure;

		for (int i = 0; i < rows.size(); i++) {
			row = rows.get(i);
			cols = row.getCol();
			for (int j = 0; j < cols.size(); j++) {
				card = cols.get(j);
				cardTreasure = card.getTreasure();
				if (cardTreasure != null
						&& cardTreasure.equals(getTreasure())) {
					return new Position(i, j);
				}
			}
		}

		return null;
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null)
			return false;

		if (!BoardType.class.isAssignableFrom(other.getClass()))
			return false;

		Board board = (Board) other;
		if (!this.getShiftCard().equals(board.getShiftCard())
				|| !this.getTreasure().equals(board.getTreasure())
				|| !this.getTreasurePos().equals(board.getTreasurePos()))
			return false;

		for (int i = 0; i < getRow().size(); i++) {
			for (int j = 0; j < getRow().get(i).getCol().size(); j++) {
				if (!this.getCard(i, j).equals(board.getCard(i, j)))
					return false;
			}
		}

		return true;
	}
	
	public Card getCard(PositionType pos) {
		return getCard(pos.getRow(), pos.getCol());
	}
}
