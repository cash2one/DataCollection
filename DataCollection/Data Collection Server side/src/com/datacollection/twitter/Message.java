package com.datacollection.twitter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserMentionEntity;

public class Message implements Comparable<Message> {
	private String mID;
	private User from;
	private List<User> to;
	private String text;
	private Date createTime;
	private String inReplytoMessageID;
	/*
	 * 0 for SMS message, 1 for tweet, 2 for facebook
	 */
	private int type;

	
	public Message(Status status){
		mID=String.valueOf(status.getId());
		from=status.getUser();
		to=new ArrayList<User>();
		UserMentionEntity[] toArray=status.getUserMentionEntities();
		for(int i=0;i<toArray.length;i++){
			//User a =new User();
		}
		text=status.getText();
		createTime=status.getCreatedAt();
		if(status.getInReplyToStatusId()!=-1){
			inReplytoMessageID=String.valueOf(status.getInReplyToStatusId());
		}
	}
	
	public Message(DirectMessage msg){
		mID=String.valueOf(msg.getId());
		from=msg.getSender();
		to=new ArrayList<User>();
		to.add(msg.getRecipient());
		text=msg.getText();
		createTime=msg.getCreatedAt();
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getmID() {
		return mID;
	}

	public void setmID(String mID) {
		this.mID = mID;
	}

	public String getInReplytoMessageID() {
		return inReplytoMessageID;
	}

	public void setInReplytoMessageID(String inReplytoMessageID) {
		this.inReplytoMessageID = inReplytoMessageID;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public User getSender(){
		return from;
	}
	
	public List<User> getRecipients(){
		return to;
	}

	@Override
	public int compareTo(Message arg0) {
		return this.getCreateTime().compareTo(arg0.getCreateTime());
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Message))
			return false;
		Message a=(Message)o;
		return this.mID.equals(a.mID);
	}

	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("Message: ").append(mID).append("\n");
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		String strDate = dateFormat.format(createTime);
		sb.append("Created at: ").append(strDate).append("\n");
		sb.append("Sender: [").append(from.getScreenName()).append("]\n");
		sb.append("Recipients: [");
		for(int i=0;i<to.size();i++){ 
			sb.append(to.get(i).getScreenName());
			if(i!=to.size()-1)
				sb.append(",");
		}
		sb.append("]\n");
		sb.append("Text: ").append(text).append("\n");;
		return sb.toString();
	}
	
	
	
}
