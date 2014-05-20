package ai;

import generated.AwaitMoveMessageType;
import generated.MoveMessageType;

public interface AI {
	MoveMessageType move(int playerID, AwaitMoveMessageType data);
}
