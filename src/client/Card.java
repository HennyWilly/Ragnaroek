package client;

import generated.CardType;
import generated.TreasureType;

public class Card extends CardType {
	public enum CardShape {
		L, T, I;
	}

	public enum Orientation {

		D0(0), D90(90), D180(180), D270(270);

		final int value;

		Orientation(int v) {
			value = v;
		}

		public int value() {
			return value;
		}

		public static Orientation fromValue(int v) {
			for (Orientation c : Orientation.values()) {
				if (c.value == v) {
					return c;
				}
			}
			throw new IllegalArgumentException(v + "");
		}
	}

	public Card(CardType c) {
		super();

		this.setOpenings(new Openings());
		this.getOpenings().setBottom(c.getOpenings().isBottom());
		this.getOpenings().setLeft(c.getOpenings().isLeft());
		this.getOpenings().setRight(c.getOpenings().isRight());
		this.getOpenings().setTop(c.getOpenings().isTop());

		this.setTreasure(c.getTreasure());
		this.setPin(new Pin());
		if(c.getPin()!=null){
			this.pin.getPlayerID().addAll(c.getPin().getPlayerID());
		}else{
			this.setPin(null);
		}
	}

	public Card(CardShape shape, Orientation o, TreasureType t) {
		super();
		this.setOpenings(new Openings());
		this.setPin(new Pin());
		this.pin.getPlayerID();
		switch (shape) {
		case I:
			switch (o) {
			case D180:
			case D0:
				this.openings.setBottom(true);
				this.openings.setTop(true);
				this.openings.setLeft(false);
				this.openings.setRight(false);
				break;
			case D270:
			case D90:
				this.openings.setBottom(false);
				this.openings.setTop(false);
				this.openings.setLeft(true);
				this.openings.setRight(true);
				break;
			default:
				// TODO Wrong Rotation
				break;
			}
			break;
		case L:
			switch (o) {
			case D180:
				this.openings.setBottom(true);
				this.openings.setTop(false);
				this.openings.setLeft(true);
				this.openings.setRight(false);
				break;
			case D270:
				this.openings.setBottom(false);
				this.openings.setTop(true);
				this.openings.setLeft(true);
				this.openings.setRight(false);
				break;
			case D90:
				this.openings.setBottom(true);
				this.openings.setTop(false);
				this.openings.setLeft(false);
				this.openings.setRight(true);
				break;
			case D0:
				this.openings.setBottom(false);
				this.openings.setTop(true);
				this.openings.setLeft(false);
				this.openings.setRight(true);
				break;
			default:
				// TODO Wrong Rotation
				break;
			}
			break;
		case T:
			switch (o) {
			case D180:
				this.openings.setBottom(false);
				this.openings.setTop(true);
				this.openings.setLeft(true);
				this.openings.setRight(true);
				break;
			case D270:
				this.openings.setBottom(true);
				this.openings.setTop(true);
				this.openings.setLeft(false);
				this.openings.setRight(true);
				break;
			case D90:
				this.openings.setBottom(true);
				this.openings.setTop(true);
				this.openings.setLeft(true);
				this.openings.setRight(false);
				break;
			case D0:
				this.openings.setBottom(true);
				this.openings.setTop(false);
				this.openings.setLeft(true);
				this.openings.setRight(true);
				break;
			default:
				// TODO Wrong Rotation
				break;
			}
			break;
		default:
			// TODO Wrong Shape

		}
		this.treasure = t;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Codeoptimierung
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Card other = new Card((CardType) obj);
		if (this.treasure != other.getTreasure()) {
			return false;
		}
		for (Integer ID : this.getPin().getPlayerID()) {
			if (!other.getPin().getPlayerID().contains(ID)) {
				return false;
			}
		}
		if(other.getShape()!=this.getShape()){
			return false;
		}
		return true;
	}

	public CardShape getShape() {
		boolean[] open = new boolean[4];
		open[0] = getOpenings().isTop();
		open[1] = getOpenings().isRight();
		open[2] = getOpenings().isBottom();
		open[3] = getOpenings().isLeft();

		int indsum = 0;
		int anzop = 0;

		for (int i = 0; i < open.length; i++) {
			if (open[i]) {
				indsum += i;
				++anzop;
			}
		}
		if (anzop == 2 && indsum % 2 == 0) {
			return CardShape.I;
		} else if (anzop == 2 && indsum % 2 == 1) {
			return CardShape.L;
		} else {
			return CardShape.T;
		}
	}
	
	public Orientation getOrientation(){
		switch(getShape()){
		case I:
			if(getOpenings().isTop()){
				return Orientation.D0;
			}else{
				return Orientation.D90;
			}
		case L:
			if(getOpenings().isTop() && getOpenings().isRight()){
				return Orientation.D0;
			}else if(getOpenings().isRight() && getOpenings().isBottom()){
				return Orientation.D90;
			}else if(getOpenings().isBottom() && getOpenings().isLeft()){
				return Orientation.D180;
			}else { //if(getOpenings().isLeft() && getOpenings().isTop()){
				return Orientation.D270;
			}
		case T:
			if(!getOpenings().isTop()){
				return Orientation.D0;
			}else if(!getOpenings().isRight()){
				return Orientation.D90;
			}else if(!getOpenings().isBottom()){
				return Orientation.D180;
			}else {//if(!getOpenings().isLeft()){
				return Orientation.D270;
			}
		default:
			return null;					
		}
	}

	public void rotateClockwise() {
		Openings openings = getOpenings();
		Openings rotated = new Openings();
		
		rotated.setBottom(openings.isRight());
		rotated.setLeft(openings.isBottom());
		rotated.setTop(openings.isLeft());
		rotated.setRight(openings.isTop());
		
		setOpenings(rotated);
	}
}
