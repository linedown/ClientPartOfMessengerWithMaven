package serializable;

import java.awt.*;
import java.io.Serializable;

public class Message implements Serializable {

    private int id;

    private String senderName;
    private String receiverName;

    private String text;

    public Message(String senderName, String receiverName, String text) {
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderName='" + senderName + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
