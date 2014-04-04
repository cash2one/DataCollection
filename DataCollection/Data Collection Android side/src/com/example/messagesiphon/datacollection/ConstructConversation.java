package com.example.messagesiphon.datacollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ConstructConversation has the method construct which is used to construct
 * conversations from a list of messages. A message is included in a
 * conversation if it has the same participants(source + destination) as the
 * conversation.
 * 
 */

public class ConstructConversation
{
	/*
	 * Runs through each messages in list checking if its participants match an
	 * existing conversation, if then do it gets add to that conversation if not
	 * a new conversation is made. Last it sets the end time of the
	 * conversation.
	 */
	public List<Conversation> construct(List<MessageU> messageList)
	{
		List<Conversation> conversList = new ArrayList<Conversation>();
		boolean existingConversation = false;
		for (int i = 0; i < messageList.size(); i++)
		{
			MessageU msg = messageList.get(i);
			List<String> participants = new ArrayList<String>(
					msg.getDestiPIDList());
			participants.add(msg.getSourcePID());
			Collections.sort(participants);
			for (int j = 0; j < conversList.size(); j++)
			{
				existingConversation = false;
				if (conversList.get(j).getParticipantList()
						.equals(participants))
				{
					existingConversation = true;
					conversList.get(j).addMessage(msg);
					break;
				}

			}
			if (!existingConversation)
			{
				Conversation c = new Conversation(0);
				c.addMessage(msg);
				conversList.add(c);
			}
		}
		for (int i = 0; i < conversList.size(); i++)
		{
			conversList.get(i).setStartEndTime();
		}
		return conversList;
	}

}
