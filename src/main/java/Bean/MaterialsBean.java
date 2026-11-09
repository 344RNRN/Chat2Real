package Bean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * イベント資料画面（12_イベント資料）に表示するデータ
 *   お知らせ：t013_notifications
 *   資料　　：t014_event_materials
 */
public class MaterialsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private long eventId;
    private String eventName;
    private String exchangeStatus;   // 交流したい／今は交流したくない

    private List<Notice> notices = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();

    /** お知らせ1件分（t013_notifications） */
    public static class Notice implements Serializable {
        private static final long serialVersionUID = 1L;

        private String title;
        private String text;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    /** 資料1ファイル分（t014_event_materials） */
    public static class Material implements Serializable {
        private static final long serialVersionUID = 1L;

        private String fileName;
        private String filePath;    // 例：/uploads/materials/xxxx.pdf
        private String fileType;    // 例：application/pdf、image/png
        private Timestamp createdAt;

        /** 種別アイコンの文字（画像／PDF／資料） */
        public String getKindLabel() {
            if (fileType != null && fileType.startsWith("image/")) return "画像";
            if ("application/pdf".equals(fileType)) return "PDF";
            if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) return "PDF";
            return "資料";
        }

        /** 例：14:02 */
        public String getTimeText() {
            return createdAt == null ? "" : new SimpleDateFormat("HH:mm").format(createdAt);
        }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }

        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }

        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    }

    public long getEventId() { return eventId; }
    public void setEventId(long eventId) { this.eventId = eventId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getExchangeStatus() { return exchangeStatus; }
    public void setExchangeStatus(String exchangeStatus) { this.exchangeStatus = exchangeStatus; }

    public List<Notice> getNotices() { return notices; }
    public void setNotices(List<Notice> notices) { this.notices = notices; }

    public List<Material> getMaterials() { return materials; }
    public void setMaterials(List<Material> materials) { this.materials = materials; }
}
