package Bean;

public class SurveyAnswerBean {

    private long answerId;
    private long eventParticipantId;
    private long questionId;
    private String answerText;

    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

    public long getEventParticipantId() {
        return eventParticipantId;
    }

    public void setEventParticipantId(long eventParticipantId) {
        this.eventParticipantId = eventParticipantId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }
}