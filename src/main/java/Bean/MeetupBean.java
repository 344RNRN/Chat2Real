
package Bean;

public class MeetupBean {

    private long meetupId;
    private long eventParticipantId;
    private long exchangeRequestId;
    private String meetupResult;

    public long getMeetupId() {
        return meetupId;
    }

    public void setMeetupId(long meetupId) {
        this.meetupId = meetupId;
    }

    public long getEventParticipantId() {
        return eventParticipantId;
    }

    public void setEventParticipantId(long eventParticipantId) {
        this.eventParticipantId = eventParticipantId;
    }

    public long getExchangeRequestId() {
        return exchangeRequestId;
    }

    public void setExchangeRequestId(long exchangeRequestId) {
        this.exchangeRequestId = exchangeRequestId;
    }

    public String getMeetupResult() {
        return meetupResult;
    }

    public void setMeetupResult(String meetupResult) {
        this.meetupResult = meetupResult;
    }
}
