package networking;

import generated.CardType;
import generated.LoginMessageType;
import generated.MazeCom;
import generated.MazeComType;
import generated.MoveMessageType;
import generated.ObjectFactory;
import generated.PositionType;

public class MazeComMessageFactory {
	static private ObjectFactory of = new ObjectFactory();
	
	public MazeCom createLoginMessage(String name) {
		LoginMessageType type = new LoginMessageType();
		type.setName(name);
		
		MazeCom mc = of.createMazeCom();
		mc.setMcType(MazeComType.LOGIN);
		mc.setLoginMessage(type);
		
		return mc;
	}
	
	public MazeCom createMoveMessage(int playerID, PositionType newPinPos, PositionType shiftPosition, CardType shiftCard) {
		MoveMessageType type = new MoveMessageType();
		type.setNewPinPos(newPinPos);
		type.setShiftPosition(shiftPosition);
		type.setShiftCard(shiftCard);
		
		return createMoveMessage(playerID, type);
	}
	
	public MazeCom createMoveMessage(int playerID, MoveMessageType moveMessage) {
		MazeCom mc = of.createMazeCom();
		mc.setId(playerID);
		mc.setMcType(MazeComType.MOVE);
		mc.setMoveMessage(moveMessage);
		
		return mc;
	}
}
