
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PostgreSQLConnectionTest {

    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/chat2real";
        String user = "postgres";
        String password = "pass";

        String sql = "SELECT current_database()";

        try (
            Connection conn =
                DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)
        ) {
            if (rs.next()) {
                System.out.println(
                    "接続成功！データベース名：" + rs.getString(1)
                );
            }

        } catch (Exception e) {
            System.out.println("接続に失敗しました。");
            e.printStackTrace();
        }
    }
}
