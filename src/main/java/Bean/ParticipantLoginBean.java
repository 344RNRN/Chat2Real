package Bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * 参加者ログイン画面（participantLogin.jsp）
 *   すでに参加登録した人が、ニックネーム＋本名で自分のアカウントに戻る
 */
public class ParticipantLoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== ログインするイベント（t003_events） =====
    private long eventId;
    private String eventName;
    private Timestamp eventDatetime;
    private String venue;

    // ===== 入力 =====
    private String nickname = "";
    private String realName = "";

    // ===== ログインできた参加者 =====
    private long eventParticipantId;
    private long participantId;

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

    /**
     * 本名の比べ方をそろえる：前後と途中の空白（全角スペースも）を取る
     * 「山田 友紀」「山田　友紀」「山田友紀」を同じ人として扱う
     */
    public static String normalizeName(String s) {
        return (s == null) ? "" : s.replaceAll("[\\s\\u3000]+", "");
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
    public void setNickname(String nickname) { this.nickname = nickname == null ? "" : nickname; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName == null ? "" : realName; }

    public long getEventParticipantId() { return eventParticipantId; }
    public void setEventParticipantId(long eventParticipantId) { this.eventParticipantId = eventParticipantId; }

    public long getParticipantId() { return participantId; }
    public void setParticipantId(long participantId) { this.participantId = participantId; }
}
