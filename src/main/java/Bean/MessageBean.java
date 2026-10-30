
package Bean;

import java.sql.Timestamp;

public class MessageBean {

    private long messageId;
    private long chatId;
    private long senderParticipantId;
    private String messageText;
    private Timestamp sentAt;

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getSenderParticipantId() {
        return senderParticipantId;
    }

    public void setSenderParticipantId(long senderParticipantId) {
        this.senderParticipantId = senderParticipantId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }
}
