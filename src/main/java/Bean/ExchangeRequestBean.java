package Bean;

public class ExchangeRequestBean {

    private long exchangeRequestId;
    private long chatId;
    private long requesterEventParticipantId;
    private long targetEventParticipantId;
    private String requestStatus;

    public long getExchangeRequestId() {
        return exchangeRequestId;
    }

    public void setExchangeRequestId(long exchangeRequestId) {
        this.exchangeRequestId = exchangeRequestId;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getRequesterEventParticipantId() {
        return requesterEventParticipantId;
    }

    public void setRequesterEventParticipantId(
            long requesterEventParticipantId) {
        this.requesterEventParticipantId = requesterEventParticipantId;
    }

    public long getTargetEventParticipantId() {
        return targetEventParticipantId;
    }

    public void setTargetEventParticipantId(
            long targetEventParticipantId) {
        this.targetEventParticipantId = targetEventParticipantId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
}