package com.example.messagesiphon.datacollection;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A messageU is a single message between users containing: message ID, senders
 * address, destination or recipients address(es), text of the message, and the
 * date the message was sent.
 */
public class MessageU implements Comparable<MessageU> {
	private String mID; // Message ID
	/**
	 * Phone Number of Sender
	 */
	private String sPID;

	/**
	 * phone number of destination if sms or list of phone numbers if MMS
	 */
	private List<String> dPIDList;
	private String text;
	private Date createTime;
	private int type;// 0 for SMS/MMS message

	public MessageU(String mID, String sPID, List<String> dPIDList,
			String text, Date createTime) {
		this.setmID(mID);
		this.setSourcePID(sPID);
		this.setDestiPIDList(dPIDList);
		this.setText(text);
		this.setCreateTime(createTime);
	}

	public MessageU() {
	}

	public String getSourcePID() {
		return sPID;
	}

	public void setSourcePID(String sPID) {
		this.sPID = sPID;
	}

	public List<String> getDestiPIDList() {
		return dPIDList;
	}

	public void setDestiPIDList(List<String> dPIDList) {
		this.dPIDList = dPIDList;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public int compareTo(MessageU arg0) {
		return this.getCreateTime().compareTo(arg0.getCreateTime());
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof MessageU))
			return false;
		MessageU a = (MessageU) o;
		return this.mID.equals(a.mID);
	}

	public JSONObject tojson() {
		JSONObject j = new JSONObject();
		try {
			j.put("mID", this.getmID());
			j.put("sPID", this.getSourcePID());
			j.put("dPID", this.getDestiPIDList());
			j.put("text", this.getText());
			j.put("createTime", this.getCreateTime());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return j;
	}

}
