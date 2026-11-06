package Bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * イベントダッシュボード上部（event-info）に出すイベント情報
 */
public class EventDashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long eventId;
    private String eventName;
    private Timestamp eventDatetime;
    private Timestamp endDatetime;
    private String venue;
    private String eventStatus;

    // ===== KPI（EventDashboardDAO.loadKpi で入れる） =====
    private int participantCount;    // 参加者数
    private int chatUserCount;       // チャットを1回以上送った人
    private int approvedUserCount;   // 交流申請が承認された人（申請した側・された側どちらでも）
    private int meetupUserCount;     // 直接交流が成立した人
    private Double satisfactionAvg;  // 満足度の平均（回答が無ければ null）
    private int ratingCount;         // 満足度の回答数

    /** 割合（%）。参加者がいなければ「－」 */
    private String rate(int count) {
        if (participantCount == 0) {
            return "－";
        }
        return String.valueOf(Math.round(count * 100.0 / participantCount));
    }

    public String getChatRateText()     { return rate(chatUserCount); }
    public String getApprovedRateText() { return rate(approvedUserCount); }
    public String getMeetupRateText()   { return rate(meetupUserCount); }

    /** 満足度の平均（小数1桁）。回答が無ければ「－」 */
    public String getSatisfactionText() {
        return (satisfactionAvg == null || ratingCount == 0) ? "－" : String.format("%.1f", satisfactionAvg);
    }

    /** まだ誰とも直接交流できていない参加者の数 */
    public int getNoMeetupCount() {
        return Math.max(0, participantCount - meetupUserCount);
    }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }

    public int getChatUserCount() { return chatUserCount; }
    public void setChatUserCount(int chatUserCount) { this.chatUserCount = chatUserCount; }

    public int getApprovedUserCount() { return approvedUserCount; }
    public void setApprovedUserCount(int approvedUserCount) { this.approvedUserCount = approvedUserCount; }

    public int getMeetupUserCount() { return meetupUserCount; }
    public void setMeetupUserCount(int meetupUserCount) { this.meetupUserCount = meetupUserCount; }

    public Double getSatisfactionAvg() { return satisfactionAvg; }
    public void setSatisfactionAvg(Double satisfactionAvg) { this.satisfactionAvg = satisfactionAvg; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    /** 「2026/10/18 (日) 19:00〜21:00」の形にする */
    public String getDateText() {
        if (eventDatetime == null) {
            return "";
        }
        LocalDateTime start = eventDatetime.toLocalDateTime();
        String week = start.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);

        StringBuilder sb = new StringBuilder();
        sb.append(start.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
          .append(" (").append(week).append(") ")
          .append(start.format(DateTimeFormatter.ofPattern("HH:mm")));

        if (endDatetime != null) {
            sb.append("〜").append(endDatetime.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            sb.append("〜");
        }
        return sb.toString();
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Timestamp getEventDatetime() { return eventDatetime; }
    public void setEventDatetime(Timestamp eventDatetime) { this.eventDatetime = eventDatetime; }

    public Timestamp getEndDatetime() { return endDatetime; }
    public void setEndDatetime(Timestamp endDatetime) { this.endDatetime = endDatetime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getEventStatus() { return eventStatus; }
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }
}