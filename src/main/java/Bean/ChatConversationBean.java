package Bean;

import java.io.Serializable;

/** 個人チャット画面の左の「チャット一覧」1行（自分がチャットしたことのある相手） */
public class ChatConversationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long partnerId;          // 相手の event_participant_id（/chat?partnerId=… に使う）
    private long chatId;
    private String nickname;
    private String photoUrl;
    private String lastMessage;      // 最後のメッセージ（まだ無ければ null）
    private String timeText;         // 今日なら「14:12」、それ以外は「11/9」
    private boolean ended;           // チャットが「終了」
    private int unreadCount;         // 未読（相手から来て、まだ見ていないメッセージ）

    public long getPartnerId() { return partnerId; }
    public void setPartnerId(long partnerId) { this.partnerId = partnerId; }

    public long getChatId() { return chatId; }
    public void setChatId(long chatId) { this.chatId = chatId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getTimeText() { return timeText; }
    public void setTimeText(String timeText) { this.timeText = timeText; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public boolean isEnded() { return ended; }
    public void setEnded(boolean ended) { this.ended = ended; }

    /** 一覧に出す名前（ニックネームが無ければ「参加者」） */
    public String getDisplayName() {
        return (nickname == null || nickname.isEmpty()) ? "参加者" : nickname;
    }

    /** 一覧に出す一言 */
    public String getPreview() {
        if (lastMessage == null || lastMessage.isEmpty()) {
            return ended ? "チャットは終了しました" : "会話を開始しました";
        }
        return lastMessage;
    }
}
