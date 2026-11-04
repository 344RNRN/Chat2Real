
package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Bean.ProfileBean;

public class ProfileDAO {

    private static final String DB_URL =
            "jdbc:postgresql://localhost:5432/chat2real";

    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "pass";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }

        return DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASSWORD);
    }

    // 参加者IDからプロフィールを取得
    public ProfileBean findByParticipantId(long participantId)
            throws SQLException {

        String sql =
                "SELECT profile_id, participant_id, nickname, " +
                "real_name, age_group, gender, self_introduction, " +
                "profile_image_url " +
                "FROM t007_profiles WHERE participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, participantId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProfileBean profile = new ProfileBean();

                    profile.setProfileId(rs.getLong("profile_id"));
                    profile.setParticipantId(
                            rs.getLong("participant_id"));
                    profile.setNickname(rs.getString("nickname"));
                    profile.setRealName(rs.getString("real_name"));
                    profile.setAgeGroup(rs.getString("age_group"));
                    profile.setGender(rs.getString("gender"));
                    profile.setSelfIntroduction(
                            rs.getString("self_introduction"));
                    profile.setProfileImageUrl(
                            rs.getString("profile_image_url"));

                    return profile;
                }
            }
        }

        return null;
    }

    // プロフィールを新規登録
    public long insertProfile(ProfileBean profile)
            throws SQLException {

        String sql =
                "INSERT INTO t007_profiles " +
                "(participant_id, nickname, real_name, age_group, " +
                "gender, self_introduction, profile_image_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, profile.getParticipantId());
            ps.setString(2, profile.getNickname());
            ps.setString(3, profile.getRealName());
            ps.setString(4, profile.getAgeGroup());
            ps.setString(5, profile.getGender());
            ps.setString(6, profile.getSelfIntroduction());
            ps.setString(7, profile.getProfileImageUrl());

            int result = ps.executeUpdate();

            if (result == 0) {
                return 0;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }

        return 0;
    }

    // プロフィールを更新
    public boolean updateProfile(ProfileBean profile)
            throws SQLException {

        String sql =
                "UPDATE t007_profiles SET " +
                "nickname = ?, real_name = ?, age_group = ?, " +
                "gender = ?, self_introduction = ?, " +
                "profile_image_url = ? " +
                "WHERE participant_id = ?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, profile.getNickname());
            ps.setString(2, profile.getRealName());
            ps.setString(3, profile.getAgeGroup());
            ps.setString(4, profile.getGender());
            ps.setString(5, profile.getSelfIntroduction());
            ps.setString(6, profile.getProfileImageUrl());
            ps.setLong(7, profile.getParticipantId());

            return ps.executeUpdate() > 0;
        }
    }
}
