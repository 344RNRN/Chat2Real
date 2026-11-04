package Bean;

public class EventParticipantBean {

    private long eventParticipantId;
    private long eventId;
    private long participantId;
    private long profileId;
    private String exchangeStatus;

    public long getEventParticipantId() {
        return eventParticipantId;
    }

    public void setEventParticipantId(long eventParticipantId) {
        this.eventParticipantId = eventParticipantId;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(long participantId) {
        this.participantId = participantId;
    }

    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(long profileId) {
        this.profileId = profileId;
    }

    public String getExchangeStatus() {
        return exchangeStatus;
    }

    public void setExchangeStatus(String exchangeStatus) {
        this.exchangeStatus = exchangeStatus;
    }
}