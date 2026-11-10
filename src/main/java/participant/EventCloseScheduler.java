package participant;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import DAO.EventCloseDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Tomcat が起動したら、1分ごとにイベントの終了チェックをする
 *   ・終了日時を過ぎたイベントを「終了」にする
 *   ・終了10分前 と 終了 のお知らせを参加者に配信する
 */
@WebListener
public class EventCloseScheduler implements ServletContextListener {

    private ScheduledExecutorService timer;

    @Override
    public void contextInitialized(ServletContextEvent e) {
        timer = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "event-close-check");
            t.setDaemon(true);
            return t;
        });
        timer.scheduleAtFixedRate(() -> {
            try {
                int sent = new EventCloseDAO().runChecks();
                if (sent > 0) {
                    System.out.println("[イベント終了チェック] お知らせを " + sent + " 件配信しました");
                }
            } catch (Exception ex) {
                // 失敗しても次の1分でまた試す（止めない）
                System.out.println("[イベント終了チェック] 失敗：" + ex.getMessage());
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
        if (timer != null) {
            timer.shutdownNow();
        }
    }
}
