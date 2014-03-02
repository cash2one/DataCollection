package com.datacollection.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
//import twitter4j.User;
/**
 * assume that messageList are constructed in time order.
 * @author Tengyu
 *
 */
public class Conversation implements Comparable<Conversation> {
	private String cID;
	private List<Message> messageList;
	private Date startTime;
	private Date endTime;
	private String startID;
	private String endID;
	private int count;
	/*
	 * 0 stands for sms, 1 for tweets, 2 for facebook
	 */
	private int type;

	public void setStartEndTime() {
		Collections.sort(messageList);
		startTime = messageList.get(0).getCreateTime();
		endTime = messageList.get(messageList.size() - 1).getCreateTime();
	}

	public Conversation(int type) {
		setMessageList(new ArrayList<Message>());
		this.setType(type);
	}

	public Conversation(List<Message> messageList, int type) {
		this.setMessageList(messageList);
		this.setType(type);

	}

	public List<Message> getMessageList() {
		return messageList;
	}
	
	public void addMessage(Message msg){
		messageList.add(msg);
	}

	public void setMessageList(List<Message> messageList) {
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
	
	public List<String> getMessageIDList(){
		List<String> messageIDList=new ArrayList<String> ();
		for(int i=0;i<messageList.size();i++){
			messageIDList.add(messageList.get(i).getmID());
		}
		return messageIDList;
	}
	
	/*public Date getParticipateTime(){
		String pID;
		if(type==0) pID=Global.user.getPhoneNum();
		else if(type==1) pID=Global.user.getTweetID();
		else pID=Global.user.getFacebookID();
		for(int i=0;i<messageList.size();i++){
			if(messageList.get(i).getSourcePID().equals(pID)||messageList.get(i).getDestiPIDList().contains(pID)){
				return messageList.get(i).getCreateTime();
			}
		}
		// should never happen
		return new Date();
	}*/
	
	public List<User> getParticipantList(){
		Set<User> s=new LinkedHashSet<User>();
		for(int i=0;i<messageList.size();i++){
			s.add(messageList.get(i).getSender());
			s.addAll(messageList.get(i).getRecipients());
		}
		List<User> particpantList=new ArrayList<User>();
		particpantList.addAll(s);
		return particpantList;
	}

	@Override
	public int compareTo(Conversation another) {
		// TODO Auto-generated method stub
		return another.getEndTime().compareTo(this.getEndTime());
	}

/*	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("<Conversation>\n");
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		String strDate = dateFormat.format(this.endTime);
		sb.append("<End_Time>").append(strDate).append("</End_Time>\n");
		for(int i=0;i<messageList.size();i++){
			sb.append(messageList.get(i).toString());
		}
		sb.append("</Conversation>\n");
		return sb.toString();
	}*/
	
	public JSONObject getJSONRepresentation(){
		JSONObject conversJSON=new JSONObject();
		JSONArray mess=new JSONArray();
		for(Message m:messageList){
			mess.put(m.getJSONRepresentation());
		}
		try {
			conversJSON.put("CID", cID);
			conversJSON.put("StartTime", startTime);
			conversJSON.put("EndTime", endTime);
			conversJSON.put("StartID", startID);
			conversJSON.put("EndID", endID);
			conversJSON.put("MessageCount", count);
			conversJSON.put("messages", mess);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conversJSON;
	}

	public String getStartID() {
		return startID;
	}

	public void setStartEndIDCount() {
		this.startID = messageList.get(0).getmID();
		this.endID=messageList.get(messageList.size()-1).getmID();
		this.count=messageList.size();
		this.cID=this.startID+"_"+this.endID;
	}

	public String getEndID() {
		return endID;
	}
	
	

}
