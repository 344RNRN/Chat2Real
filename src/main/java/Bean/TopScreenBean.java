package Bean;

import java.io.Serializable;

/** イベントルーム（トップ画面 topScreen.jsp）に出す内容 */
public class TopScreenBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long eventParticipantId;
    private long eventId;
    private String eventName;
    private String eventDate;          // 例：2026年11月9日（月）13:00〜17:00
    private String venue;
    private int participantCount;
    private int wantCount;             // 「交流したい」の人数
    private String noticeTitle;        // 最新のお知らせ（無ければ null）
    private String noticeText;
    private String exchangeStatus;     // 交流したい / 今は交流したくない
    private String displayName;        // nickname / realName
    private String nickname;
    private String realName;

    public long getEventParticipantId() { return eventParticipantId; }
    public void setEventParticipantId(long v) { this.eventParticipantId = v; }

    public long getEventId() { return eventId; }
    public void setEventId(long v) { this.eventId = v; }

    public String getEventName() { return eventName; }
    public void setEventName(String v) { this.eventName = v; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String v) { this.eventDate = v; }

    public String getVenue() { return venue; }
    public void setVenue(String v) { this.venue = v; }

    public int getParticipantCount() { return participantCount; }
    public void setParticipantCount(int v) { this.participantCount = v; }

    public int getWantCount() { return wantCount; }
    public void setWantCount(int v) { this.wantCount = v; }

    public String getNoticeTitle() { return noticeTitle; }
    public void setNoticeTitle(String v) { this.noticeTitle = v; }

    public String getNoticeText() { return noticeText; }
    public void setNoticeText(String v) { this.noticeText = v; }

    public String getExchangeStatus() { return exchangeStatus; }
    public void setExchangeStatus(String v) { this.exchangeStatus = v; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String v) { this.displayName = v; }

    public String getNickname() { return nickname; }
    public void setNickname(String v) { this.nickname = v; }

    public String getRealName() { return realName; }
    public void setRealName(String v) { this.realName = v; }

    /** 本名が登録されているか（無ければ「本名」ボタンは押せない） */
    public boolean hasRealName() {
        return realName != null && !realName.trim().isEmpty();
    }
}
