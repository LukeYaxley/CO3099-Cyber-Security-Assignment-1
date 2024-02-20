import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {

    private byte[] content;
    private LocalDateTime timestamp;
    private String recepient;

    public Message( byte[] content, String recepient) {

        this.content = content;
        this.recepient = recepient;
        this.timestamp = LocalDateTime.now();

    }



    public String getTimestamp() {
        return timestamp.toString();
    }

    public byte[] getContent() {
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
