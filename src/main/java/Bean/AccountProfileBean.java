package Bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 参加者のプロフィール（登録完了画面・プロフィール画面で使う）
 */
public class AccountProfileBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 参加しているイベント（t003_events） =====
    private long eventId;
    private String eventName;
    private Timestamp eventDatetime;
    private String venue;

    // ===== イベント参加（t006_events_participant） =====
    private long eventParticipantId;
    private long participantId;
    private String exchangeStatus;   // 交流したい / 今は交流したくない

    // ===== プロフィール（t007_profiles） =====
    private String nickname;
    private String realName;
    private String ageGroup;
    private String selfIntroduction;
    private String profileImageUrl;

    // ===== 興味（t009_participant_interests） =====
    private List<String> interests = new ArrayList<>();

    /** 「2026年10月18日(日) 19:00～」 */
    public String getDateText() {
        if (eventDatetime == null) {
            return "";
        }
        LocalDateTime d = eventDatetime.toLocalDateTime();
        String week = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
        return d.format(DateTimeFormatter.ofPattern("yyyy年M月d日")) + "(" + week + ") "
             + d.format(DateTimeFormatter.ofPattern("HH:mm")) + "～";
    }

    /** 写真が無いときにアイコンに出す1文字 */
    public String getInitial() {
        return (nickname == null || nickname.isEmpty()) ? "？" : nickname.substring(0, 1);
    }

    /** 「交流したい」のとき true（ほかの参加者に公開されている） */
    public boolean isWantToMeet() {
        return "交流したい".equals(exchangeStatus);
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Timestamp getEventDatetime() { return eventDatetime; }
    public void setEventDatetime(Timestamp eventDatetime) { this.eventDatetime = eventDatetime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public long getEventParticipantId() { return eventParticipantId; }
    public void setEventParticipantId(long eventParticipantId) { this.eventParticipantId = eventParticipantId; }

    public long getParticipantId() { return participantId; }
    public void setParticipantId(long participantId) { this.participantId = participantId; }

    public String getExchangeStatus() { return exchangeStatus; }
    public void setExchangeStatus(String exchangeStatus) { this.exchangeStatus = exchangeStatus; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getSelfIntroduction() { return selfIntroduction; }
    public void setSelfIntroduction(String selfIntroduction) { this.selfIntroduction = selfIntroduction; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}
