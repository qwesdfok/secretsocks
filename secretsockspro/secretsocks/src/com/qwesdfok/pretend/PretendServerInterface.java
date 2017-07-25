package com.qwesdfok.pretend;

import java.net.Socket;

public interface PretendServerInterface
{
	void pretend(Socket socket, ListenerInterface.TriggerType triggerType,PolicyManager policyManager);
}
