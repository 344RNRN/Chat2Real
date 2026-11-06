package Bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * イベント作成画面（EventCreate）の入力内容をまとめて持つ Bean
 * EventCreateAction で作り、EventCreateDAO に渡して登録する
 */
public class EventCreateBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== イベント情報（t003_events） =====
    private long accountId;            // ログイン中の運営者
    private String eventName;          // ② イベント名
    private Timestamp eventDatetime;   // ③④ 開催日 + 開始時間
    private Timestamp endDatetime;     // ③④ 開催日 + 終了時間
    private String venue;              // ⑤ 会場
    private Integer capacity;          // ⑥ 定員（任意）
    private String eventDescription;   // ⑦ イベント説明（任意）

    // ===== ⑫～⑱ 参加者への質問（t004_survey_questions・最大5問） =====
    private List<Question> questions = new ArrayList<>();

    // ===== ⑩⑪ 興味タグ =====
    private List<Long> interestIds = new ArrayList<>();      // チェックした既存タグ
    private List<String> newInterests = new ArrayList<>();   // 「＋追加」で入力したタグ

    // ===== ⑧⑨ 資料（ファイル本体は Action で保存済み） =====
    private List<Material> materials = new ArrayList<>();

    /** 質問1問分 */
    public static class Question implements Serializable {
        private static final long serialVersionUID = 1L;

        public static final String TYPE_SELECT = "select";   // 選択式
        public static final String TYPE_TEXT = "text";       // 自由記述

        private String questionText;
        private String questionType = TYPE_TEXT;
        private String options;      // 選択肢「A／B／C」（選択式のときだけ）
        private boolean required;

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
    }

    /** 資料1ファイル分 */
    public static class Material implements Serializable {
        private static final long serialVersionUID = 1L;

        private String fileName;   // 元のファイル名（表示用）
        private String filePath;   // 保存先（例：/uploads/materials/xxxx.pdf）
        private String fileType;   // image/png, application/pdf など
        private long fileSize;

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    }

    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Timestamp getEventDatetime() { return eventDatetime; }
    public void setEventDatetime(Timestamp eventDatetime) { this.eventDatetime = eventDatetime; }

    public Timestamp getEndDatetime() { return endDatetime; }
    public void setEndDatetime(Timestamp endDatetime) { this.endDatetime = endDatetime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public String getEventDescription() { return eventDescription; }
    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public List<Long> getInterestIds() { return interestIds; }
    public void setInterestIds(List<Long> interestIds) { this.interestIds = interestIds; }

    public List<String> getNewInterests() { return newInterests; }
    public void setNewInterests(List<String> newInterests) { this.newInterests = newInterests; }

    public List<Material> getMaterials() { return materials; }
    public void setMaterials(List<Material> materials) { this.materials = materials; }
}