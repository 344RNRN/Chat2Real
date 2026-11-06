package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import Bean.EventEditBean;
import Bean.InterestBean;

/**
 * イベント編集画面（EventEdit）の DAO
 *
 * 使うテーブル：
 *   t003_events / t004_survey_questions / t004_2_survey_answers
 *   t008_interests / t014_event_materials / t015_event_interests
 */
public class EventEditDAO {

    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    // =========================================================
    // 画面表示：今の内容を読み込む（イベントが無ければ null）
    // =========================================================
    public EventEditBean findById(long eventId) throws SQLException {
        String sql = "SELECT event_id, event_name, event_datetime, end_datetime, venue, capacity, "
                   + "event_description, event_status FROM public.t003_events WHERE event_id = ?";

        try (Connection con = getConnection()) {
            EventEditBean bean;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setLong(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    bean = new EventEditBean();
                    bean.setEventId(rs.getLong("event_id"));
                    bean.setEventName(rs.getString("event_name"));
                    bean.setEventDatetime(rs.getTimestamp("event_datetime"));
                    bean.setEndDatetime(rs.getTimestamp("end_datetime"));
                    bean.setVenue(rs.getString("venue"));
                    int cap = rs.getInt("capacity");
                    bean.setCapacity(rs.wasNull() ? null : cap);
                    bean.setEventDescription(rs.getString("event_description"));
                    bean.setEventStatus(rs.getString("event_status"));
                }
            }

            loadQuestions(con, bean);
            bean.setQuestionsLocked(hasAnswers(con, eventId));
            bean.setInterestIds(findInterestIds(con, eventId));
            bean.setMaterials(findMaterials(con, eventId));
            return bean;
        }
    }

    /** 有効な興味タグ一覧 */
    public List<InterestBean> findActiveInterests() throws SQLException {
        String sql = "SELECT interest_id, interest_name, is_active "
                   + "FROM public.t008_interests WHERE is_active = true ORDER BY interest_id";

        List<InterestBean> list = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                InterestBean b = new InterestBean();
                b.setInterestId(rs.getLong("interest_id"));
                b.setInterestName(rs.getString("interest_name"));
                b.setActive(rs.getBoolean("is_active"));
                list.add(b);
            }
        }
        return list;
    }

    /** 運営者の質問（select / text）だけを表示順で */
    public List<EventEditBean.Question> findQuestions(long eventId) throws SQLException {
        try (Connection con = getConnection()) {
            EventEditBean tmp = new EventEditBean();
            tmp.setEventId(eventId);
            loadQuestions(con, tmp);
            return tmp.getQuestions();
        }
    }

    /** 登録済み資料（削除チェック・表示用） */
    public List<EventEditBean.Material> findMaterials(long eventId) throws SQLException {
        try (Connection con = getConnection()) {
            return findMaterials(con, eventId);
        }
    }

    /** アンケート回答がすでにあるか（あれば質問は変更できない） */
    public boolean hasAnswers(long eventId) throws SQLException {
        try (Connection con = getConnection()) {
            return hasAnswers(con, eventId);
        }
    }

    // =========================================================
    // 更新：イベント・タグ・質問・資料をまとめて更新
    // どれか1つでも失敗したら全部取り消す
    // 戻り値：削除した資料のファイルパス（DB 更新が成功したあと Action でファイルを消す）
    // =========================================================
    public List<String> update(EventEditBean bean) throws SQLException {
        long eventId = bean.getEventId();
        List<String> deletedPaths = new ArrayList<>();

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. イベント情報
                updateEvent(con, bean);

                // 2. 興味タグ（全部消して入れ直す）
                execute(con, "DELETE FROM public.t015_event_interests WHERE event_id = ?", eventId);
                for (Long id : bean.getInterestIds()) {
                    insertEventInterest(con, eventId, id);
                }
                for (String name : bean.getNewInterests()) {
                    insertEventInterest(con, eventId, findOrCreateInterest(con, name));
                }

                // 3. 質問（回答がまだ無いときだけ。運営者の質問を消して入れ直す）
                if (!hasAnswers(con, eventId)) {
                    execute(con, "DELETE FROM public.t004_survey_questions "
                               + "WHERE event_id = ? AND question_type IN ('select', 'text')", eventId);
                    int order = 1;
                    for (EventEditBean.Question q : bean.getQuestions()) {
                        insertQuestion(con, eventId, q.getQuestionText(), q.getQuestionType(),
                                q.isRequired(), order++, q.isSelect() ? q.getOptions() : null);
                    }
                }

                // 4. 満足度・不満点の固定質問が無いイベントには足しておく
                ensureFixedQuestions(con, eventId);

                // 5. 資料：削除にチェックしたもの
                for (Long materialId : bean.getDeleteMaterialIds()) {
                    String path = deleteMaterial(con, eventId, materialId);
                    if (path != null) {
                        deletedPaths.add(path);
                    }
                }

                // 6. 資料：新しく追加したもの
                for (EventEditBean.Material m : bean.getNewMaterials()) {
                    insertMaterial(con, eventId, m);
                }

                con.commit();
                return deletedPaths;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- 以下、部品 ----------

    private void updateEvent(Connection con, EventEditBean b) throws SQLException {
        String sql = "UPDATE public.t003_events SET event_name = ?, event_datetime = ?, end_datetime = ?, "
                   + "venue = ?, capacity = ?, event_description = ? WHERE event_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, b.getEventName());
            ps.setTimestamp(2, b.getEventDatetime());
            ps.setTimestamp(3, b.getEndDatetime());
            ps.setString(4, b.getVenue());
            if (b.getCapacity() != null) {
                ps.setInt(5, b.getCapacity());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.setString(6, b.getEventDescription());
            ps.setLong(7, b.getEventId());

            if (ps.executeUpdate() == 0) {
                throw new SQLException("イベントが見つかりません：event_id = " + b.getEventId());
            }
        }
    }

    private void loadQuestions(Connection con, EventEditBean bean) throws SQLException {
        String sql = "SELECT question_text, question_type, is_required, options "
                   + "FROM public.t004_survey_questions "
                   + "WHERE event_id = ? AND question_type IN ('select', 'text') "
                   + "ORDER BY display_order, question_id";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, bean.getEventId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventEditBean.Question q = new EventEditBean.Question();
                    q.setQuestionText(rs.getString("question_text"));
                    q.setQuestionType(rs.getString("question_type"));
                    q.setRequired(rs.getBoolean("is_required"));
                    q.setOptions(rs.getString("options"));
                    bean.getQuestions().add(q);
                }
            }
        }
    }

    private boolean hasAnswers(Connection con, long eventId) throws SQLException {
        String sql = "SELECT 1 FROM public.t004_2_survey_answers a "
                   + "JOIN public.t004_survey_questions q ON q.question_id = a.question_id "
                   + "WHERE q.event_id = ? LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<Long> findInterestIds(Connection con, long eventId) throws SQLException {
        String sql = "SELECT interest_id FROM public.t015_event_interests WHERE event_id = ?";

        List<Long> ids = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("interest_id"));
                }
            }
        }
        return ids;
    }

    private List<EventEditBean.Material> findMaterials(Connection con, long eventId) throws SQLException {
        String sql = "SELECT material_id, file_name, file_path, file_type, file_size "
                   + "FROM public.t014_event_materials WHERE event_id = ? ORDER BY material_id";

        List<EventEditBean.Material> list = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventEditBean.Material m = new EventEditBean.Material();
                    m.setMaterialId(rs.getLong("material_id"));
                    m.setFileName(rs.getString("file_name"));
                    m.setFilePath(rs.getString("file_path"));
                    m.setFileType(rs.getString("file_type"));
                    m.setFileSize(rs.getLong("file_size"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    private void insertQuestion(Connection con, long eventId, String text, String type,
                                boolean required, int order, String options) throws SQLException {
        String sql = "INSERT INTO public.t004_survey_questions "
                   + "(event_id, question_text, question_type, is_required, display_order, options) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setString(2, text);
            ps.setString(3, type);
            ps.setBoolean(4, required);
            ps.setInt(5, order);
            ps.setString(6, options);
            ps.executeUpdate();
        }
    }

    /** 満足度（rating）・不満点（complaint）が無ければ追加する */
    private void ensureFixedQuestions(Connection con, long eventId) throws SQLException {
        if (!existsQuestionType(con, eventId, "complaint")) {
            insertQuestion(con, eventId, "不満点がございましたらお書きください", "complaint", false, 98, null);
        }
        if (!existsQuestionType(con, eventId, "rating")) {
            insertQuestion(con, eventId, "今日の交流のしやすさは？（満足度アンケート）", "rating", true, 99, null);
        }
    }

    private boolean existsQuestionType(Connection con, long eventId, String type) throws SQLException {
        String sql = "SELECT 1 FROM public.t004_survey_questions WHERE event_id = ? AND question_type = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private long findOrCreateInterest(Connection con, String name) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT interest_id FROM public.t008_interests WHERE interest_name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("interest_id");
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO public.t008_interests (interest_name) VALUES (?) RETURNING interest_id")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("interest_id");
            }
        }
    }

    private void insertEventInterest(Connection con, long eventId, long interestId) throws SQLException {
        String sql = "INSERT INTO public.t015_event_interests (event_id, interest_id) VALUES (?, ?) "
                   + "ON CONFLICT (event_id, interest_id) DO NOTHING";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setLong(2, interestId);
            ps.executeUpdate();
        }
    }

    /** 資料を1件削除してファイルパスを返す（そのイベントの資料でなければ何もしないで null） */
    private String deleteMaterial(Connection con, long eventId, long materialId) throws SQLException {
        String sql = "DELETE FROM public.t014_event_materials WHERE material_id = ? AND event_id = ? "
                   + "RETURNING file_path";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, materialId);
            ps.setLong(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("file_path") : null;
            }
        }
    }

    private void insertMaterial(Connection con, long eventId, EventEditBean.Material m) throws SQLException {
        String sql = "INSERT INTO public.t014_event_materials "
                   + "(event_id, file_name, file_path, file_type, file_size) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setString(2, m.getFileName());
            ps.setString(3, m.getFilePath());
            ps.setString(4, m.getFileType());
            ps.setLong(5, m.getFileSize());
            ps.executeUpdate();
        }
    }

    private void execute(Connection con, String sql, long eventId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        }
    }
}