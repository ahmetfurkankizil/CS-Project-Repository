package MessagesGUI;

import SignupAndLogin.LoginFrame;
import UserRelated.User;

import java.util.ArrayList;

public class MessageConnection {
    private User otherUser;
    private User currentUser;
    private ArrayList<Message> messages;
    int port;
    int id;

    public MessageConnection(User currentUser, User otherUser,int port , boolean isItNew) {
        this.otherUser = otherUser;
        this.currentUser = currentUser;
        this.messages = new ArrayList<>();
        this.id = currentUser.getId()+otherUser.getId();
        this.port = port;
        if (isItNew){
            currentUser.insertToMessageConnectionTable(this.id,currentUser,otherUser,port);
            currentUser.createMessageHistory(this.id);
        }
    }
    public void setMessages(){

    }
    public User getCurrentUser() {
        return currentUser;
    }
    public User getOtherUser() {
        return otherUser;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }
    public void addMessages(Message message, boolean isItNew) {
        messages.add(message);
        if (isItNew && !(LoginFrame.isTrial))
            currentUser.insertToMessageHistoryTable(this.id,message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageConnection connection){
            return connection.getOtherUser().getId() == otherUser.getId();
        }
        return false;

    }
}



