import json

def function(text):
    jsonData = json.loads(text)

    streamData = jsonData.get("stream_data")

    for item in streamData:
        print("----------------------------------------")
        postID = item.get("post_id")
        print("postID", postID);
        
        updatedTime = item.get("updated_time")
        print("updatedTime", updatedTime)
        
        sourceID = item.get("source_id")
        print("sourceID", sourceID)
        
        description = item.get("description")
        print("description", description)
        
        message = item.get("message")
        print("message", message)
        
        actorID = item.get("actor_id")
        print("actorID", actorID)
        
        isPrimaryPost = len(item.get("Comments")) != 0
        print("isPrimaryPost", isPrimaryPost)
        
        if isPrimaryPost:
            commentJSON = item.get("Comments")
            for comment in commentJSON:
                print("----------")
                fromID = comment.get("fromid")
                print("fromID", fromID)
                
                text = comment.get("text")
                print("text", text)
                
                commentID = comment.get("id")
                print("commentID", commentID)

