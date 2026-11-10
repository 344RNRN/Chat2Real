package Bean;

/**
 * チャット相手（または自分）の表示用情報。
 * t006_events_participant + t007_profiles をまとめたもの。
 * chat.jsp からは partner.id / partner.displayName / partner.photoUrl で参照する。
 */
public class ChatPartnerBean {

    private long eventParticipantId;
    private long eventId;
    private long participantId;
    private String nickname;
    private String realName;
    private String photoUrl;

    public long getEventParticipantId() { return eventParticipantId; }
    public void setEventParticipantId(long eventParticipantId) { this.eventParticipantId = eventParticipantId; }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public long getParticipantId() { return participantId; }
    public void setParticipantId(long participantId) { this.participantId = participantId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    // ---- JSP表示用 ----
    /** ${partner.id}  ＝ event_participant_id（URLの partnerId に使う） */
    public long getId() { return eventParticipantId; }

    /** ${partner.initial}  アイコン写真がないときに出す頭文字 */
    public String getInitial() {
        String n = getDisplayName();
        return n.substring(0, n.offsetByCodePoints(0, 1));
    }

    /** ${partner.displayName}  ニックネーム（未登録なら「参加者」） */
    public String getDisplayName() {
        return (nickname == null || nickname.isEmpty()) ? "参加者" : nickname;
    }
}
