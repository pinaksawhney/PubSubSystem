/******************************************************************************************************************
* File:MessageManager.java
* Course: 17655
* Project: Assignment A2
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 February 2009 - Initial rewrite of original assignment 2 (ajl).
*
* Description: This class is the message manager responsible for receiving and distributing messages from participants
*			   and all associated house keeping chores. Communication with participants is via RMI. There are
*			   a number of RMI methods that allow participants to register, post messages, get messages,
*
* Parameters: None
*
* Internal Methods: None
*
******************************************************************************************************************/
import MessagePackage.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;

public class MessageManager extends UnicastRemoteObject implements RMIMessageManagerInterface
{
	static Vector<MessageQueue> MessageQueueList;	// This is the list of message queues.
	static RequestLogger l;  					// This is a request logger - Logger is a private inner class
	static HashMap<String, Vector<MessageQueue>> topicQueueMap;	// Hash Map for a topic lists all the queue of messages (devices)

	public MessageManager() throws RemoteException
	{
		super();										// Required by RMI
		l = new RequestLogger();						// Screen logging object
		MessageQueueList = new Vector<MessageQueue>(15, 1);	// Queue for storing messages
		topicQueueMap = new HashMap<String, Vector<MessageQueue>>();	// hashMap for storing topics and queue relationship
	} // Constructor

	/***************************************************************************
	* Main
	****************************************************************************/

	public static void main(String args[])
	{
		try
    	{
			// Here we start up the server. We first must instantiate a class of type PolicyDB

			InetAddress LocalHostAddress = InetAddress.getLocalHost();
			String MessageManagerIpAddress = LocalHostAddress.getHostAddress();

			MessageManager em = new MessageManager();
	      	Naming.bind("MessageManager", em);

	     	// Finally we notify the user that the server is ready.

			l.DisplayStatistics( "Server IP address::" + MessageManagerIpAddress + ". Message manager ready." );

		} // try

		// Potential exceptions

		catch (Exception e)
		{
			l.DisplayStatistics( "Message manager startup error: " + e );

		} // catch

	} // main

	/***************************************************************************
	* Remote METHOD:: Register
	* Purpose: This method registers participants with the message manager.
	*
	* Arguments: None.
	*
	* Returns: long integer - the participants id
	*
	* Exceptions: None
	*
	****************************************************************************/

	synchronized public long Register() throws RemoteException
	{
		// Create a new queue and add it to the list of message queues.

		MessageQueue mq = new MessageQueue();
		MessageQueueList.add( mq );

		l.DisplayStatistics( "Register message. Issued ID = " + mq.GetId() );
		return mq.GetId();

	} // Register

	// ToDo- Added new method
	synchronized public long Subscribe(MessageQueue mq, String topic) throws RemoteException
	{
		// Create a new queue and add it to the list of message queues.
		if (!MessageQueueList.contains(mq)) {
			System.out.println("Participant not registered, Hence can not subscribe");
			return -1;
		}
		Vector<MessageQueue> topicQueue;
		if (topicQueueMap.containsKey(topic)) {
			topicQueue = topicQueueMap.get(topic);
		} else {
			topicQueue = new Vector<MessageQueue>();
		} topicQueue.add(mq);
		topicQueueMap.put(topic, topicQueue);

		l.DisplayStatistics( "Subscribed to topic  = " + topic + "for id: " + mq.GetId() );
		return mq.GetId();

	} // Subscribe

	/***************************************************************************
	* Remote METHOD:: UnRegister
	* Purpose: This method unregisters participants with the message manager.
	*
	* Arguments: long integer - the participants id
	*
	* Returns: None
	*
	* Exceptions: None
	*
	****************************************************************************/

	synchronized public void UnRegister(long id) throws RemoteException
	{
		MessageQueue mq;
		boolean found = false;

		// Find the queue for id.

		for ( int i = 0; i < MessageQueueList.size(); i++ )
		{
			//Get the queue for id and remove it from the list.

			mq =  MessageQueueList.get(i);

			if (mq.GetId() == id)
			{
				mq = MessageQueueList.remove(i);
				found = true;
			} // if

		} // for

		if (found)
			l.DisplayStatistics( "Unregistered ID::" + id );
		else
			l.DisplayStatistics( "Unregister error. ID:"+ id + " not found.");
	} // UnRegister


	// ToDO- Added this newMethod
	synchronized public void UnSubscribe(long id, String topic) throws RemoteException
	{
		MessageQueue mq;
		boolean found = false;

		// Find the queue for id.

		for ( int i = 0; i < MessageQueueList.size(); i++ )
		{
			//Get the queue for id and remove it from the list.

			mq =  MessageQueueList.get(i);

			if (mq.GetId() == id)
			{
				found = true;

				Vector<MessageQueue> topicQueue = topicQueueMap.get(topic);
				for (int j = 0; j < topicQueue.size(); i++) {
					if (topicQueue.get(j) == mq) {
						topicQueue.remove(j);
					}
				}
				topicQueueMap.put(topic, topicQueue);

			} // if

		} // for

		if (found)
			l.DisplayStatistics( "Unsubscribed ID::" + id + "from topic: " + topic);
		else
			l.DisplayStatistics( "Unsubscribe error. ID: " + id + " not found or topic does not exist");
	} // UnSubscribe

	/***************************************************************************
	* Remote METHOD:: SendMessage
	* Purpose: This method allows participants to send messages to the message
	*		   manager.
	*
	* Arguments: Message
	*
	* Returns: None
	*
	* Exceptions: None
	*
	****************************************************************************/

	synchronized public void SendMessage(Message m, String topic ) throws RemoteException
	{
		if (!topicQueueMap.containsKey(topic)) {
			System.out.println("Can't publish to the topic, invalid topic!");
			return;
		}
		MessageQueue mq;

		// For every queue on the list, add the message.
		Vector<MessageQueue> topicQueue = topicQueueMap.get(topic);
		Vector<MessageQueue> newTopicQueue = new Vector<MessageQueue>();
		for (MessageQueue messageQueue : topicQueue) {
			mq = messageQueue;
			mq.AddMessage(m);
			newTopicQueue.add(mq);
		} // for
		topicQueueMap.remove(topic);
		topicQueueMap.put(topic, newTopicQueue);

		l.DisplayStatistics( "Incoming message posted from ID: " + m.GetSenderId() + " to topic " + topic);

	} // SendMessage

	/***************************************************************************
	* Remote METHOD:: GetMessage
	* Purpose: Get the message queue for a participant (id).
	*
	* Arguments: long id - participants id
	*
	* Returns: MessageQueue
	*
	* Exceptions: None
	*
	****************************************************************************/

	synchronized public Vector<MessageQueue> GetMessageQueue( long id ) throws RemoteException
	{
		MessageQueue mq, temp =  null;
		boolean found = false;
		Vector<MessageQueue> allMessages = new Vector<MessageQueue>();

		// Find the queue for id.
		for (Map.Entry<String, Vector<MessageQueue>> topicQueue : topicQueueMap.entrySet()) {
			for (int i = 0; i < topicQueue.getValue().size(); i++) {
				mq =  topicQueue.getValue().get(i);
				if (mq.GetId() == id) {
					mq = topicQueue.getValue().get(i);
					temp = mq.GetCopy();
					allMessages.add(temp);
					mq.ClearMessageQueue();
					found = true;
				}
			}

		}

		if (found)
				l.DisplayStatistics( "Get message queue request from ID: " + id + ". Message queue returned.");
		else
				l.DisplayStatistics( "Get message queue request from ID: " + id + " ID not found or subscribed to no topic ");

		return allMessages;

	} // GetMessageList

	/***************************************************************************
	* INNER CLASS:: Logger
	* Purpose: This class longs requests by displaying them on the server with
	* 		   general statics after each remote call for services. This method
	*		   increments the number of service request from participants,
	*		   counts the number of active queues (registered participants), and
	*		   displays this information on the terminal.
	*
	* Arguments: None.
	*
	* Returns: None
	*
	* Exceptions: None
	*
	****************************************************************************/

	private class RequestLogger
	{
		int RequestsServiced = 0;	// This is the number of requests seviced

		void DisplayStatistics( String message )
		{
			RequestsServiced++;

			if ( message.length() == 0 )
			{
				System.out.println( "-------------------------------------------------------------------------------");
				System.out.println( "Number of requests: " + RequestsServiced );
				System.out.println( "Number of registered participants: " + MessageQueueList.size() );
				System.out.println( "-------------------------------------------------------------------------------");

			} else {

				System.out.println( "-------------------------------------------------------------------------------");
				System.out.println( "Message:: " + message );
				System.out.println( "Number of requests: " + RequestsServiced );
				System.out.println( "Number of registered participants: " + MessageQueueList.size() );
				System.out.println( "-------------------------------------------------------------------------------");

			} // if

		} // Register

	} // logger

} // MessageManger class