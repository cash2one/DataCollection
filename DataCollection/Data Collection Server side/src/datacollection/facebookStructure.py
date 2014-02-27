class User:
    def __init__(self, phoneNumber, accessToken, jsonData):
        self.phoneNumber = phoneNumber
        self.accessToken = accessToken
        self.conversations = self.createConversations(jsonData.get("conversation_data"))




    #end def

    def createConversations(self, conversationJSON):
        result = []
        for convo in conversationJSON:
            result.append(Conversation(convo))
        return result

#end User class

class Conversation:
    def __init__(self, jsonData):
        self.messageCount = jsonData.get("message_count")
        self.messages = self.createMessages(jsonData.get("messages"))
        self.threadID = jsonData.get("thread_id")
        self.updatedTime = jsonData.get("updated_time")
        self.recipients = []
        for fbID in jsonData.get("recipients"):
            self.recipients.append(fbID)
    #end def

    def createMessages(self, messageJSON):
        result = []
        for message in messageJSON:
            result.append(Message(message))
        return result
#end Conversation class

class Message:
    def __init__(self, jsonData):
        self.authorID = jsonData.get("author_id")
        self.body = jsonData.get("body")
        self.createdTime = jsonData.get("created_time")
