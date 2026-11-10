package admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import Bean.NotificationBean;
import DAO.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

/**
 * お知らせ配信（運営者）
 *   GET  /NoticePostAction?eventId=… … 配信画面（ダッシュボードの「お知らせ配信」から開く）
 *   POST /NoticePostAction            … 配信（添付ファイルも保存）→ 画面に戻る
 * 配信したお知らせは、参加者のイベントルーム（バナー）と資料・全体チャット画面に出る。
 * 添付ファイルはお知らせの中の「開く」ボタンから見られる。
 */
@WebServlet("/NoticePostAction")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,          // 1ファイル 10MB まで（イベント作成と同じ）
        maxRequestSize = 12 * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024)
public class NoticePostAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String UPLOAD_DIR = "/uploads/materials";   // イベント作成の資料と同じ場所
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        NotificationDAO dao = new NotificationDAO();

        try {
            long eventId = resolveEventId(request, dao);
            Object[] event = (eventId > 0) ? dao.findEvent(eventId) : null;
            if (event == null) {
                response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
                return;
            }

            List<NotificationBean> notificationList = dao.findByEventId(eventId);

            request.setAttribute("notificationList", notificationList);
            request.setAttribute("eventId", eventId);
            request.setAttribute("eventName", event[0]);

        } catch (SQLException e) {
            throw new ServletException("お知らせ一覧の取得に失敗しました。", e);
        }

        request.getRequestDispatcher("/admin/noticePost.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        NotificationDAO dao = new NotificationDAO();
        File saved = null;

        try {
            long eventId = resolveEventId(request, dao);
            String back = request.getContextPath() + "/NoticePostAction?eventId=" + eventId;

            Object[] event = (eventId > 0) ? dao.findEvent(eventId) : null;
            if (event == null) {
                response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
                return;
            }

            String title = request.getParameter("title");
            String message = request.getParameter("message");

            // 入力チェック① 空欄
            if (title == null || title.trim().isEmpty()
                    || message == null || message.trim().isEmpty()) {
                response.sendRedirect(back + "&error=empty");
                return;
            }
            title = title.trim();
            message = message.trim();

            // 入力チェック② タイトルの文字数
            if (title.length() > 100) {
                response.sendRedirect(back + "&error=length");
                return;
            }

            NotificationBean notification = new NotificationBean();
            notification.setEventId(eventId);
            notification.setAccountId((Long) event[1]);    // イベントを作った運営者
            notification.setTitle(title);
            notification.setNotificationText(message);

            // 入力チェック③ 添付ファイル（画像・PDF、10MB まで）
            Part part = getPartSafely(request, "attachment");
            if (part != null && part.getSize() > 0) {
                String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                String type = (part.getContentType() == null) ? "" : part.getContentType();

                if (!(type.startsWith("image/") || "application/pdf".equals(type))) {
                    response.sendRedirect(back + "&error=type");
                    return;
                }
                if (part.getSize() > MAX_FILE_SIZE) {
                    response.sendRedirect(back + "&error=size");
                    return;
                }

                File dir = new File(getServletContext().getRealPath(UPLOAD_DIR));
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new ServletException("添付ファイルの保存先を作成できませんでした。");
                }

                // 同じ名前で上書きしないように UUID を付ける
                String ext = "";
                int dot = originalName.lastIndexOf('.');
                if (dot >= 0) {
                    ext = originalName.substring(dot).toLowerCase();
                }
                String savedName = UUID.randomUUID().toString() + ext;
                saved = new File(dir, savedName);
                part.write(saved.getAbsolutePath());

                notification.setAttachmentName(originalName);
                notification.setAttachmentPath(UPLOAD_DIR + "/" + savedName);
                notification.setAttachmentType(type);
                notification.setAttachmentSize(part.getSize());
            }

            if (!dao.insertNotification(notification)) {
                throw new ServletException("お知らせを登録できませんでした。");
            }
            saved = null;                                   // 登録できたのでファイルは残す
            response.sendRedirect(back + "&success=1");

        } catch (SQLException e) {
            throw new ServletException("お知らせの登録に失敗しました。", e);
        } finally {
            if (saved != null) {
                saved.delete();                             // DB 登録に失敗したら保存したファイルも消す
            }
        }
    }

    /** ファイルが大きすぎるときは例外になるので null にする（→ size エラー） */
    private Part getPartSafely(HttpServletRequest request, String name) throws IOException, ServletException {
        try {
            return request.getPart(name);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * 対象のイベント
     *   1. フォーム・URL の eventId
     *   2. セッションの eventId（ダッシュボードで選んだイベント）
     *   3. どちらも無ければ いちばん新しいイベント
     */
    static long resolveEventId(HttpServletRequest request, NotificationDAO dao) throws SQLException {
        long eventId = 0L;
        String p = request.getParameter("eventId");
        if (p != null) {
            try {
                eventId = Long.parseLong(p.trim());
            } catch (NumberFormatException e) {
                eventId = 0L;
            }
        }

        HttpSession session = request.getSession();
        if (eventId <= 0 && session.getAttribute("eventId") instanceof Number) {
            eventId = ((Number) session.getAttribute("eventId")).longValue();
        }
        if (eventId <= 0) {
            eventId = dao.findLatestEventId();
        }
        if (eventId > 0) {
            session.setAttribute("eventId", eventId);
        }
        return eventId;
    }
}
