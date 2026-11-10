package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import Bean.AdminLogin;
import Bean.EventCreateBean;
import Bean.InterestBean;

/**
 * イベント作成画面（EventCreate）の DAO
 *
 * 使うテーブル：
 *   t003_events / t004_survey_questions / t008_interests
 *   t014_event_materials（新規） / t015_event_interests（新規）
 * ※ end_datetime・capacity・options の追加カラムと新規テーブルは
 *   add_event_create.sql を先に実行しておくこと
 */
public class EventCreateDAO {

    /** 満足度・不満点（終了レポート画面で使う固定質問） */
    public static final String RATING_TEXT = "今日の交流のしやすさは？（満足度アンケート）";
    public static final String COMPLAINT_TEXT = "不満点がございましたらお書きください";

    /** 接続先・パスワードは DBManager にまとめてある */
    private Connection getConnection() throws SQLException {
        return DBManager.getConnection();
    }

    // =========================================================
    // ログインしていないとき用：いちばん最初の運営者アカウント（無ければ null）
    // =========================================================
    public AdminLogin findFirstAccount() throws SQLException {
        String sql = "SELECT account_id, login_id, account_name FROM public.t001_account "
                   + "ORDER BY account_id LIMIT 1";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new AdminLogin(rs.getLong("account_id"),
                                  rs.getString("login_id"),
                                  rs.getString("account_name"));
        }
    }

    // =========================================================
    // 画面表示用：有効な興味タグ一覧（⑩）
    // =========================================================
    public List<InterestBean> findActiveInterests() throws SQLException {
        String sql = "SELECT interest_id, interest_name, is_active "
                   + "FROM public.t008_interests WHERE is_active = true ORDER BY interest_id";

        List<InterestBean> list = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InterestBean bean = new InterestBean();
                bean.setInterestId(rs.getLong("interest_id"));
                bean.setInterestName(rs.getString("interest_name"));
                bean.setActive(rs.getBoolean("is_active"));
                list.add(bean);
            }
        }
        return list;
    }

    // =========================================================
    // 基本フロー5：イベント・質問・タグ・資料をまとめて登録
    // どれか1つでも失敗したら全部取り消す（ロールバック）
    // =========================================================
    public long create(EventCreateBean bean) throws SQLException {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                long eventId = insertEvent(con, bean);

                // 運営者の質問（表示順 1～5）
                int order = 1;
                for (EventCreateBean.Question q : bean.getQuestions()) {
                    insertQuestion(con, eventId, q.getQuestionText(), q.getQuestionType(),
                            q.isRequired(), order++, q.getOptions());
                }

                // 満足度・不満点の固定質問（運営者の質問の後ろ）
                insertQuestion(con, eventId, COMPLAINT_TEXT, "complaint", false, 98, null);
                insertQuestion(con, eventId, RATING_TEXT, "rating", true, 99, null);

                // 興味タグ
                for (Long interestId : bean.getInterestIds()) {
                    insertEventInterest(con, eventId, interestId);
                }
                for (String name : bean.getNewInterests()) {
                    insertEventInterest(con, eventId, findOrCreateInterest(con, name));
                }

                // 資料
                for (EventCreateBean.Material m : bean.getMaterials()) {
                    insertMaterial(con, eventId, m);
                }

                con.commit();
                return eventId;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------- 以下、create() の中で使う部品 ----------

    private long insertEvent(Connection con, EventCreateBean b) throws SQLException {
        String sql = "INSERT INTO public.t003_events "
                   + "(account_id, event_name, event_datetime, end_datetime, venue, capacity, event_description) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING event_id";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, b.getAccountId());
            ps.setString(2, b.getEventName());
            ps.setTimestamp(3, b.getEventDatetime());
            ps.setTimestamp(4, b.getEndDatetime());
            ps.setString(5, b.getVenue());
            if (b.getCapacity() != null) {
                ps.setInt(6, b.getCapacity());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, b.getEventDescription());
           

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("event_id");
            }
        }
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

    /** タグ名で探し、無ければ登録して ID を返す（interest_name は UNIQUE） */
    private long findOrCreateInterest(Connection con, String name) throws SQLException {
        String select = "SELECT interest_id FROM public.t008_interests WHERE interest_name = ?";
        try (PreparedStatement ps = con.prepareStatement(select)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("interest_id");
                }
            }
        }

        String insert = "INSERT INTO public.t008_interests (interest_name) VALUES (?) RETURNING interest_id";
        try (PreparedStatement ps = con.prepareStatement(insert)) {
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

    private void insertMaterial(Connection con, long eventId, EventCreateBean.Material m) throws SQLException {
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
}