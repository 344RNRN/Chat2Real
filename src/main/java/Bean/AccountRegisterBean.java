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
 * 参加者アカウント登録画面（accountRegister.jsp）
 *   画面の上：どのイベントに参加するか（イベント名・日時・会場）
 *   フォーム：プロフィール・興味
 */
public class AccountRegisterBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 参加するイベント（t003_events） =====
    private long eventId;
    private String eventName;
    private Timestamp eventDatetime;
    private String venue;

    // ===== 入力内容（t007_profiles） =====
    private String nickname;           // 20文字まで
    private String realName;           // 30文字まで（運営者のみ閲覧）
    private String ageGroup;           // 「20代」など
    private String selfIntroduction;   // 一言・参加目的（50文字まで）
    private String profileImageUrl;    // 保存したプロフィール写真のパス（任意）

    // ===== 興味（t009_participant_interests） =====
    private List<Long> interestIds = new ArrayList<>();       // チェックしたタグ
    private List<String> otherInterests = new ArrayList<>();  // 「その他」に書いたもの（、や , で区切る）

    // ===== 登録後に入る =====
    private long participantId;
    private long profileId;
    private long eventParticipantId;

    /** 「2026年10月18日(日) 19:00～」の形 */
    public String getDateText() {
        if (eventDatetime == null) {
            return "";
        }
        LocalDateTime d = eventDatetime.toLocalDateTime();
        String week = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
        return d.format(DateTimeFormatter.ofPattern("yyyy年M月d日")) + "(" + week + ") "
             + d.format(DateTimeFormatter.ofPattern("HH:mm")) + "～";
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Timestamp getEventDatetime() { return eventDatetime; }
    public void setEventDatetime(Timestamp eventDatetime) { this.eventDatetime = eventDatetime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

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

    public List<Long> getInterestIds() { return interestIds; }
    public void setInterestIds(List<Long> interestIds) { this.interestIds = interestIds; }

    public List<String> getOtherInterests() { return otherInterests; }
    public void setOtherInterests(List<String> otherInterests) { this.otherInterests = otherInterests; }

    public long getParticipantId() { return participantId; }
    public void setParticipantId(long participantId) { this.participantId = participantId; }

    public long getProfileId() { return profileId; }
    public void setProfileId(long profileId) { this.profileId = profileId; }

    public long getEventParticipantId() { return eventParticipantId; }
    public void setEventParticipantId(long eventParticipantId) { this.eventParticipantId = eventParticipantId; }
}