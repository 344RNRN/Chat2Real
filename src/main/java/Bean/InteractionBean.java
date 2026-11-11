package Bean;

import java.io.Serializable;

/** 交流申請画面（interaction.jsp）に出す内容 */
public class InteractionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long exchangeRequestId;          // 申請が無ければ 0
    private long chatId;
    private long myId;                       // 自分の event_participant_id
    private long partnerId;                  // 相手の event_participant_id
    private String partnerName;
    private String partnerPhoto;
    private long requesterId;                // 申請した人の event_participant_id（申請が無ければ 0）
    private String status;                   // 申請中 / 承認 / 辞退 / 取消（申請が無ければ null）

    /** 自分が申請して、相手の返事を待っている */
    public boolean isWaiting() {
        return "申請中".equals(status) && requesterId == myId;
    }

    /** 相手から申請が届いている */
    public boolean isIncoming() {
        return "申請中".equals(status) && requesterId != myId && requesterId > 0;
    }

    /** お互いに直接交流したい（相互承認が成立） */
    public boolean isApproved() {
        return "承認".equals(status);
    }

    /** 自分が送った申請が見送られた（相手が「今はやめておく」を選んだ） */
    public boolean isDeclinedForMe() {
        return "辞退".equals(status) && requesterId == myId;
    }

    /** 申請できる（まだ申請が無い・前の申請が辞退／取消） */
    public boolean isCanRequest() {
        return !isWaiting() && !isIncoming() && !isApproved();
    }

    public long getExchangeRequestId() { return exchangeRequestId; }
    public void setExchangeRequestId(long v) { this.exchangeRequestId = v; }
    public long getChatId() { return chatId; }
    public void setChatId(long v) { this.chatId = v; }
    public long getMyId() { return myId; }
    public void setMyId(long v) { this.myId = v; }
    public long getPartnerId() { return partnerId; }
    public void setPartnerId(long v) { this.partnerId = v; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String v) { this.partnerName = v; }
    public String getPartnerPhoto() { return partnerPhoto; }
    public void setPartnerPhoto(String v) { this.partnerPhoto = v; }
    public long getRequesterId() { return requesterId; }
    public void setRequesterId(long v) { this.requesterId = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
}
