/******************************************************************************************************************
* File:MessageManagerInterface.java
* Course: 17655
* Project: Assignment A2
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 February 2009 - Initial rewrite of original assignment 2 (ajl).
*
* Description: This class is the interface definition for the event manager services that are available to
* 			   participants
*
* Parameters: None
*
* Internal Methods: None
*
******************************************************************************************************************/
package MessagePackage;

import java.rmi.*;
import java.util.Vector;

public interface RMIMessageManagerInterface extends Remote
{

	/***************************************************************************
	* INTERFACE:: Register
	* Purpose: This interface is used to access the participant registration
	* 		   service on the MessageManager
	*
	* Arguments: None
	*
	* Returns: long integer registration number
	*
	* Exceptions: RemoteException
	*
	****************************************************************************/

	public long Register() throws java.rmi.RemoteException;

	/***************************************************************************
	* INTERFACE:: UnRegister
	* Purpose:This interface is used to access the participant un-registration
	* 		   service on the MessageManager
	*
	* Arguments: long integer registration number
	*
	* Returns: None
	*
	* Exceptions: RemoteException
	*
	****************************************************************************/

	public void UnRegister(long SenderID) throws java.rmi.RemoteException;

	/***************************************************************************
	* INTERFACE:: SendMessage
	* Purpose: This interface is used by participant to access the message sending
	* 		   service on the MessageManager
	*
	* Arguments: Message object (see the class: Message.java)
	*
	* Returns: None
	*
	* Exceptions: RemoteException
	*
	****************************************************************************/

	public void SendMessage(Message m, String topic ) throws  java.rmi.RemoteException;

	/***************************************************************************
	* INTERFACE:: GetMessage
	* Purpose: This interface is used to allow the participant access the message
	*		   queue on the MessageManager
	*
	* Arguments: long integer registration number
	*
	* Returns: MessageQueue object (see the class: MessageQueue.java)
	*
	* Exceptions: RemoteException
	*
	****************************************************************************/

	public Vector<MessageQueue> GetMessageQueue(long id ) throws java.rmi.RemoteException;

	public long Subscribe(MessageQueue mq, String topic) throws java.rmi.RemoteException;

	public void UnSubscribe(long id, String topic) throws java.rmi.RemoteException;

} // class
