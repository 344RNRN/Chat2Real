package Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 終了レポート画面（EventReport）の表示内容をまとめて持つ Bean
 * EventReportDAO で作り、EventReportAction から eventReport.jsp に渡す
 */
public class EventReportBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long eventParticipantId;   // ログイン中の参加者（t006_events_participant）
    private long eventId;
    private String eventName;
    private String eventStatus;        // 「終了」のときだけ表示する
    private int meetupCount;           // ①② 直接交流できた人数
    private boolean answered;          // すでにアンケートに回答したか

    /** 運営者がイベント作成画面で作った質問（select / text） */
    private List<Question> questions = new ArrayList<>();

    /** 固定質問（イベント作成時に自動で作られる） */
    private long ratingQuestionId;      // ⑩ 満足度 ★1～5
    private long complaintQuestionId;   // 不満点

    /** 質問1問分（t004_survey_questions） */
    public static class Question implements Serializable {
        private static final long serialVersionUID = 1L;

        private long questionId;
        private String questionText;
        private String questionType;   // select / text
        private boolean required;
        private String options;        // 選択肢「A／B／C」

        /** 選択肢を「／」または「/」で区切ってリストにする */
        public List<String> getOptionList() {
            List<String> list = new ArrayList<>();
            if (options == null) {
                return list;
            }
            for (String s : options.split("[／/]")) {
                if (!s.trim().isEmpty()) {
                    list.add(s.trim());
                }
            }
            return list;
        }

        /** 選択式で、選択肢が1つ以上あるとき true */
        public boolean isSelect() {
            return "select".equals(questionType) && !getOptionList().isEmpty();
        }

        public long getQuestionId() { return questionId; }
        public void setQuestionId(long questionId) { this.questionId = questionId; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }

        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }
    }

    /** 回答1件分（t004_2_survey_answers に登録する） */
    public static class Answer implements Serializable {
        private static final long serialVersionUID = 1L;

        private long questionId;
        private String answerText;

        public Answer() {
        }

        public Answer(long questionId, String answerText) {
            this.questionId = questionId;
            this.answerText = answerText;
        }

        public long getQuestionId() { return questionId; }
        public void setQuestionId(long questionId) { this.questionId = questionId; }

        public String getAnswerText() { return answerText; }
        public void setAnswerText(String answerText) { this.answerText = answerText; }
    }

    public long getEventParticipantId() { return eventParticipantId; }
    public void setEventParticipantId(long eventParticipantId) { this.eventParticipantId = eventParticipantId; }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventStatus() { return eventStatus; }
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }

    public int getMeetupCount() { return meetupCount; }
    public void setMeetupCount(int meetupCount) { this.meetupCount = meetupCount; }

    public boolean isAnswered() { return answered; }
    public void setAnswered(boolean answered) { this.answered = answered; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public long getRatingQuestionId() { return ratingQuestionId; }
    public void setRatingQuestionId(long ratingQuestionId) { this.ratingQuestionId = ratingQuestionId; }

    public long getComplaintQuestionId() { return complaintQuestionId; }
    public void setComplaintQuestionId(long complaintQuestionId) { this.complaintQuestionId = complaintQuestionId; }
}