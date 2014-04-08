package edu.uiowa.datacollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A conversation is all messages between unique group of users containing: list
 * of messages, time of first and last message, and a list of all users
 * involved.
 * 
 */
public class Conversation implements Comparable<Conversation> {
	private List<MessageU> messageList;
	private Date startTime;
	private Date endTime;
	private boolean hasParticipant = false;
	private List<String> particpantList;

	private int type;

	public void setStartEndTime() {
		Collections.sort(messageList);
		startTime = messageList.get(0).getCreateTime();
		endTime = messageList.get(messageList.size() - 1).getCreateTime();
	}

	public Conversation(int type) {
		setMessageList(new ArrayList<MessageU>());
		this.setType(type);
	}

	public Conversation(List<MessageU> messageList, int type) {
		this.setMessageList(messageList);
		this.setType(type);

	}

	public List<MessageU> getMessageList() {
		return messageList;
	}

	public void addMessage(MessageU msg) {
		messageList.add(msg);
	}

	public void setMessageList(List<MessageU> messageList) {
		this.messageList = messageList;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List<String> getMessageIDList() {
		List<String> messageIDList = new ArrayList<String>();
		for (int i = 0; i < messageList.size(); i++) {
			messageIDList.add(messageList.get(i).getmID());
		}
		return messageIDList;
	}

	public Date getParticipateTime() {
		String pID;
		pID = messageList.get(0).getSourcePID();
		for (int i = 0; i < messageList.size(); i++) {
			if (messageList.get(i).getSourcePID().equals(pID)
					|| messageList.get(i).getDestiPIDList().contains(pID)) {
				return messageList.get(i).getCreateTime();
			}
		}
		// should never happen
		return new Date();
	}

	public List<String> getParticipantList() {
		if (!hasParticipant) {
			Set<String> s = new LinkedHashSet<String>();
			for (int i = 0; i < messageList.size(); i++) {
				s.add(messageList.get(i).getSourcePID());
				s.addAll(messageList.get(i).getDestiPIDList());
			}
			particpantList = new ArrayList<String>();
			particpantList.addAll(s);
			Collections.sort(particpantList);
			hasParticipant = true;
			return particpantList;
		}
		return particpantList;
	}

	@Override
	public int compareTo(Conversation another) {
		return another.getEndTime().compareTo(this.getEndTime());
	}

	public JSONObject toJson() {
		JSONObject j = new JSONObject();
		try {
			j.put("participant", this.getParticipantList().toString());
			this.setStartEndTime();
			j.put("endTime", this.endTime.toString());
			JSONArray list = new JSONArray();
			for (MessageU message : messageList) {
				list.put(message.tojson());
			}
			j.put("messages", list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return j;
	}

}
