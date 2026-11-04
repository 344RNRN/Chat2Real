package Bean;

public class ParticipantInterestBean {

    private long participantInterestId;
    private long eventId;
    private long participantId;
    private long interestId;

    public long getParticipantInterestId() {
        return participantInterestId;
    }

    public void setParticipantInterestId(long participantInterestId) {
        this.participantInterestId = participantInterestId;
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

    public long getInterestId() {
        return interestId;
    }

    public void setInterestId(long interestId) {
        this.interestId = interestId;
    }
}