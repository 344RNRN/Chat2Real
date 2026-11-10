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
 * QRコード画面（qrCode.jsp）の内容
 *   参加者がスマホで読み取ると、そのイベントの参加登録画面が開く
 */
public class QRCodeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== イベント（t003_events） =====
    private long eventId;
    private String eventName;
    private Timestamp eventDatetime;
    private String venue;

    // ===== QRコードにする URL =====
    private String registerUrl;              // 例：http://192.168.1.23:8080/Chat2Real/participant/AccountRegister.action?eventId=1
    private String selectedHost;             // 今使っている IP アドレス（ホスト名）
    private List<String[]> hostOptions = new ArrayList<>();   // {IPアドレス, 表示名}
    private boolean localhostOnly;           // スマホから開けるアドレスが見つからなかった

    /** 「2026/10/18 (日) 19:00〜」 */
    public String getDateText() {
        if (eventDatetime == null) {
            return "";
        }
        LocalDateTime d = eventDatetime.toLocalDateTime();
        String week = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPANESE);
        return d.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + " (" + week + ") "
             + d.format(DateTimeFormatter.ofPattern("HH:mm")) + "〜";
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Timestamp getEventDatetime() { return eventDatetime; }
    public void setEventDatetime(Timestamp eventDatetime) { this.eventDatetime = eventDatetime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getRegisterUrl() { return registerUrl; }
    public void setRegisterUrl(String registerUrl) { this.registerUrl = registerUrl; }

    public String getSelectedHost() { return selectedHost; }
    public void setSelectedHost(String selectedHost) { this.selectedHost = selectedHost; }

    public List<String[]> getHostOptions() { return hostOptions; }
    public void setHostOptions(List<String[]> hostOptions) { this.hostOptions = hostOptions; }

    public boolean isLocalhostOnly() { return localhostOnly; }
    public void setLocalhostOnly(boolean localhostOnly) { this.localhostOnly = localhostOnly; }
}
