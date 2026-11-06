package Bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * イベント編集画面（EventEdit）の内容
 *   GET  … EventEditDAO が今の内容を詰めて eventEdit.jsp に渡す
 *   POST … EventEditAction が入力内容を詰めて EventEditDAO で更新する
 */
public class EventEditBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===== イベント情報（t003_events） =====
    private long eventId;
    private String eventName;
    private Timestamp eventDatetime;
    private Timestamp endDatetime;
    private String venue;
    private Integer capacity;
    private String eventDescription;
    private String eventStatus;

    // ===== 参加者への質問（select / text だけ。満足度・不満点は触らない） =====
    private List<Question> questions = new ArrayList<>();
    /** すでにアンケート回答があるときは質問を変更できない */
    private boolean questionsLocked;

    // ===== 興味タグ =====
    private List<Long> interestIds = new ArrayList<>();      // チェックされているタグ
    private List<String> newInterests = new ArrayList<>();   // 「＋追加」で入力したタグ

    // ===== 資料 =====
    private List<Material> materials = new ArrayList<>();          // 今登録されている資料（表示用）
    private List<Long> deleteMaterialIds = new ArrayList<>();      // 削除にチェックした資料
    private List<Material> newMaterials = new ArrayList<>();       // 新しく追加した資料

    /** 質問1問分 */
    public static class Question implements Serializable {
        private static final long serialVersionUID = 1L;

        public static final String TYPE_SELECT = "select";
        public static final String TYPE_TEXT = "text";

        private String questionText;
        private String questionType = TYPE_TEXT;
        private String options;
        private boolean required;

        public boolean isSelect() { return TYPE_SELECT.equals(questionType); }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }

        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
    }

    /** 資料1ファイル分（t014_event_materials） */
    public static class Material implements Serializable {
        private static final long serialVersionUID = 1L;

        private long materialId;
        private String fileName;
        private String filePath;
        private String fileType;
        private long fileSize;

        public long getMaterialId() { return materialId; }
        public void setMaterialId(long materialId) { this.materialId = materialId; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    }

    // ===== 画面の初期値用（<input type="date"> / <input type="time"> の形） =====
    public String getDateValue() {
        return eventDatetime == null ? ""
                : eventDatetime.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getStartTimeValue() {
        return eventDatetime == null ? ""
                : eventDatetime.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getEndTimeValue() {
        return endDatetime == null ? ""
                : endDatetime.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

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

    public String getEventStatus() { return eventStatus; }
    public void setEventStatus(String eventStatus) { this.eventStatus = eventStatus; }

    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }

    public boolean isQuestionsLocked() { return questionsLocked; }
    public void setQuestionsLocked(boolean questionsLocked) { this.questionsLocked = questionsLocked; }

    public List<Long> getInterestIds() { return interestIds; }
    public void setInterestIds(List<Long> interestIds) { this.interestIds = interestIds; }

    public List<String> getNewInterests() { return newInterests; }
    public void setNewInterests(List<String> newInterests) { this.newInterests = newInterests; }

    public List<Material> getMaterials() { return materials; }
    public void setMaterials(List<Material> materials) { this.materials = materials; }

    public List<Long> getDeleteMaterialIds() { return deleteMaterialIds; }
    public void setDeleteMaterialIds(List<Long> deleteMaterialIds) { this.deleteMaterialIds = deleteMaterialIds; }

    public List<Material> getNewMaterials() { return newMaterials; }
    public void setNewMaterials(List<Material> newMaterials) { this.newMaterials = newMaterials; }
}