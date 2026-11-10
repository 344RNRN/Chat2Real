package Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 資料・全体チャット画面（lounge.jsp）に出す内容
 *   PC：左に資料、右に全体チャット / スマホ：タブで切り替え
 */
public class LoungeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long eventParticipantId;
    private long participantId;
    private long eventId;
    private String eventName;
    private String exchangeStatus;
    private int memberCount;            // イベントの参加者数
    private long groupChatId;

    private List<Item> items = new ArrayList<>();          // お知らせ・資料（新しい順）
    private List<Message> messages = new ArrayList<>();    // 全体チャット（古い順）

    /** お知らせ・資料の1行 */
    public static class Item implements Serializable {
        private static final long serialVersionUID = 1L;

        private boolean notice;         // true＝お知らせ / false＝資料ファイル
        private String title;           // お知らせのタイトル or ファイル名
        private String text;            // お知らせの本文（資料は null）
        private String filePath;        // 資料・添付ファイルのパス（無ければ null）
        private String fileName;        // 資料・添付ファイルの元の名前
        private String fileType;
        private String timeText;

        /** アイコンの文字：告知 / PDF / IMG / FILE */
        public String getKindLabel() {
            if (notice) return "告知";
            return getFileLabel();
        }

        /** ファイルの種類：PDF / IMG / FILE */
        public String getFileLabel() {
            String t = (fileType == null) ? "" : fileType.toLowerCase();
            String n = (fileName == null) ? "" : fileName.toLowerCase();
            if (t.contains("pdf") || n.endsWith(".pdf")) return "PDF";
            if (t.startsWith("image") || n.matches(".*\\.(png|jpe?g|gif|webp)$")) return "IMG";
            return "FILE";
        }

        public boolean isNotice() { return notice; }
        public void setNotice(boolean notice) { this.notice = notice; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public boolean hasFile() { return filePath != null && !filePath.isEmpty(); }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getTimeText() { return timeText; }
        public void setTimeText(String timeText) { this.timeText = timeText; }
    }

    /** 全体チャットのメッセージ1件 */
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;

        private long messageId;
        private String nickname;
        private String text;
        private String timeText;
        private boolean mine;

        public long getMessageId() { return messageId; }
        public void setMessageId(long messageId) { this.messageId = messageId; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getTimeText() { return timeText; }
        public void setTimeText(String timeText) { this.timeText = timeText; }
        public boolean isMine() { return mine; }
        public void setMine(boolean mine) { this.mine = mine; }
    }

    public long getEventParticipantId() { return eventParticipantId; }
    public void setEventParticipantId(long v) { this.eventParticipantId = v; }
    public long getParticipantId() { return participantId; }
    public void setParticipantId(long v) { this.participantId = v; }
    public long getEventId() { return eventId; }
    public void setEventId(long v) { this.eventId = v; }
    public String getEventName() { return eventName; }
    public void setEventName(String v) { this.eventName = v; }
    public String getExchangeStatus() { return exchangeStatus; }
    public void setExchangeStatus(String v) { this.exchangeStatus = v; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int v) { this.memberCount = v; }
    public long getGroupChatId() { return groupChatId; }
    public void setGroupChatId(long v) { this.groupChatId = v; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public boolean isWant() {
        return "交流したい".equals(exchangeStatus);
    }
}
