package Bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 参加者一覧画面（participantList.jsp）の内容
 */
public class ParticipantListBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== 自分・イベント =====
    private long myEventParticipantId;
    private long eventId;
    private String eventName;
    private boolean canChat;              // 自分が「交流したい」のときだけ true
    private List<String> myInterests = new ArrayList<>();   // 共通の興味に印を付けるため

    // ===== 件数 =====
    private int wantCount;                // 交流したい参加者（自分以外・絞り込み前）
    private int hitCount;                 // 絞り込み後の人数
    private int otherCount;               // 表示しきれなかった人数（「すべて表示」で見られる）

    // ===== 一覧 =====
    private List<Participant> participants = new ArrayList<>();

    // ===== 絞り込み（画面の選択肢・入力値） =====
    private List<InterestBean> interestOptions = new ArrayList<>();
    private List<String> ageGroupOptions = new ArrayList<>();
    private String keyword = "";
    private String ageGroup = "";
    private long interestId;

    /** 参加者1人分（カード） */
    public static class Participant implements Serializable {
        private static final long serialVersionUID = 1L;

        private long eventParticipantId;
        private long participantId;
        private String nickname;
        private String ageGroup;
        private String selfIntroduction;
        private String profileImageUrl;
        private List<String> interests = new ArrayList<>();

        /** 写真が無いときにアイコンに出す1文字 */
        public String getInitial() {
            return (nickname == null || nickname.isEmpty()) ? "？" : nickname.substring(0, 1);
        }

        /** アイコンの色（人によって変える） */
        public String getAvatarClass() {
            String[] classes = { "avatar-orange", "avatar-green", "avatar-blue" };
            return classes[(int) (eventParticipantId % classes.length)];
        }

        public long getEventParticipantId() { return eventParticipantId; }
        public void setEventParticipantId(long eventParticipantId) { this.eventParticipantId = eventParticipantId; }

        public long getParticipantId() { return participantId; }
        public void setParticipantId(long participantId) { this.participantId = participantId; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public String getAgeGroup() { return ageGroup; }
        public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

        public String getSelfIntroduction() { return selfIntroduction; }
        public void setSelfIntroduction(String selfIntroduction) { this.selfIntroduction = selfIntroduction; }

        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

        public List<String> getInterests() { return interests; }
        public void setInterests(List<String> interests) { this.interests = interests; }
    }

    /** 検索・絞り込みをしているか */
    public boolean isFiltered() {
        return !keyword.isEmpty() || !ageGroup.isEmpty() || interestId > 0;
    }

    public long getMyEventParticipantId() { return myEventParticipantId; }
    public void setMyEventParticipantId(long myEventParticipantId) { this.myEventParticipantId = myEventParticipantId; }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public boolean isCanChat() { return canChat; }
    public void setCanChat(boolean canChat) { this.canChat = canChat; }

    public List<String> getMyInterests() { return myInterests; }
    public void setMyInterests(List<String> myInterests) { this.myInterests = myInterests; }

    public int getWantCount() { return wantCount; }
    public void setWantCount(int wantCount) { this.wantCount = wantCount; }

    public int getHitCount() { return hitCount; }
    public void setHitCount(int hitCount) { this.hitCount = hitCount; }

    public int getOtherCount() { return otherCount; }
    public void setOtherCount(int otherCount) { this.otherCount = otherCount; }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }

    public List<InterestBean> getInterestOptions() { return interestOptions; }
    public void setInterestOptions(List<InterestBean> interestOptions) { this.interestOptions = interestOptions; }

    public List<String> getAgeGroupOptions() { return ageGroupOptions; }
    public void setAgeGroupOptions(List<String> ageGroupOptions) { this.ageGroupOptions = ageGroupOptions; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword == null ? "" : keyword; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup == null ? "" : ageGroup; }

    public long getInterestId() { return interestId; }
    public void setInterestId(long interestId) { this.interestId = interestId; }
}
