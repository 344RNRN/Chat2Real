package Bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * アンケート結果画面（surveyResult.jsp）の内容
 */
public class SurveyResultBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long eventId;
    private String eventName;
    private int participantCount;              // 参加者数
    private int[] ratingCounts = new int[6];   // [1]～[5] に ★1～★5 の件数
    private List<Row> rows = new ArrayList<>(); // 回答した人ごと（新しい順）

    /** 回答した人1人分 */
    public static class Row implements Serializable {
        private static final long serialVersionUID = 1L;

        private long eventParticipantId;
        private Timestamp answeredAt;
        private Integer rating;                                   // ★（無ければ null）
        private List<String[]> answers = new ArrayList<>();       // {質問文, 回答}（運営者の質問）
        private String complaint;                                 // 不満点

        public String getAnsweredAtText() {
            return answeredAt == null ? ""
                    : answeredAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
        }

        public long getEventParticipantId() { return eventParticipantId; }
        public void setEventParticipantId(long eventParticipantId) { this.eventParticipantId = eventParticipantId; }

        public Timestamp getAnsweredAt() { return answeredAt; }
        public void setAnsweredAt(Timestamp answeredAt) { this.answeredAt = answeredAt; }

        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }

        public List<String[]> getAnswers() { return answers; }
        public void setAnswers(List<String[]> answers) { this.answers = answers; }

        public String getComplaint() { return complaint; }
        public void setComplaint(String complaint) { this.complaint = complaint; }
    }

    /** 回答数（回答した人数） */
    public int getAnswerCount() {
        return rows.size();
    }

    /** ★の回答数 */
    public int getRatingTotal() {
        int total = 0;
        for (int i = 1; i <= 5; i++) total += ratingCounts[i];
        return total;
    }

    /** 回答率（%）。参加者がいなければ「－」 */
    public String getAnswerRateText() {
        return participantCount == 0 ? "－"
                : Math.round(getAnswerCount() * 100.0 / participantCount) + "%";
    }

    /** 満足度の平均（小数1桁）。回答が無ければ「－」 */
    public String getSatisfactionText() {
        int total = getRatingTotal();
        if (total == 0) return "－";
        int sum = 0;
        for (int i = 1; i <= 5; i++) sum += i * ratingCounts[i];
        return String.format("%.1f", (double) sum / total);
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public int[] getRatingCounts() { return ratingCounts; }
    public void setRatingCounts(int[] ratingCounts) { this.ratingCounts = ratingCounts; }

    public List<Row> getRows() { return rows; }
    public void setRows(List<Row> rows) { this.rows = rows; }
}