import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String recepient;

    public Message(String sender, String content, String recepient) {
        this.sender = sender;
        this.content = content;
        this.recepient = recepient;
        this.timestamp = LocalDateTime.now();

    }

    public String getSender() {
        return sender;
    }

    public String getTimestamp() {
        return timestamp.toString();
    }

    public String getContent() {
        return content;
    }

    public String getRecepient() {
        return recepient;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
