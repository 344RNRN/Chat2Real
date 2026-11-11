package admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import Bean.NotificationBean;
import DAO.MeetupZoneDAO;
import DAO.NotificationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * 会場マップ・合流エリアの設定（運営者）
 *   POST /admin/MeetupZone.action … お知らせ配信画面の「会場マップ・合流エリア」から
 *     eventId / zones（1行に1つ）/ venueMap（写真）/ removeMap / notify
 *   保存後はお知らせ配信画面に戻る。notify にチェックがあれば参加者にもお知らせする。
 */
@WebServlet("/admin/MeetupZone.action")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 12 * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024)
public class MeetupZoneAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String UPLOAD_DIR = "/uploads/venue";
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(request.getContextPath() + "/NoticePostAction");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        NotificationDAO noticeDao = new NotificationDAO();
        File saved = null;

        try {
            long eventId = NoticePostAction.resolveEventId(request, noticeDao);
            Object[] event = (eventId > 0) ? noticeDao.findEvent(eventId) : null;
            if (event == null) {
                response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
                return;
            }
            String back = request.getContextPath() + "/NoticePostAction?eventId=" + eventId + "#venue";

            List<String> zones = MeetupZoneDAO.parse(request.getParameter("zones"));
            boolean removeMap = request.getParameter("removeMap") != null;
            boolean notify = request.getParameter("notify") != null;

            // 会場マップの写真（画像だけ・10MB まで）
            String mapPath = null;
            String mapName = null;
            String mapType = null;
            long mapSize = 0;
            Part part = getPartSafely(request, "venueMap");
            if (part != null && part.getSize() > 0) {
                mapType = (part.getContentType() == null) ? "" : part.getContentType();
                if (!mapType.startsWith("image/")) {
                    response.sendRedirect(back.replace("#venue", "&venueError=type#venue"));
                    return;
                }
                if (part.getSize() > MAX_FILE_SIZE) {
                    response.sendRedirect(back.replace("#venue", "&venueError=size#venue"));
                    return;
                }
                mapName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
                String ext = "";
                int dot = mapName.lastIndexOf('.');
                if (dot >= 0) ext = mapName.substring(dot).toLowerCase();

                File dir = new File(getServletContext().getRealPath(UPLOAD_DIR));
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new ServletException("会場マップの保存先を作成できませんでした。");
                }
                saved = new File(dir, UUID.randomUUID().toString() + ext);
                part.write(saved.getAbsolutePath());
                mapPath = UPLOAD_DIR + "/" + saved.getName();
                mapSize = part.getSize();
                removeMap = false;
            }

            if (!new MeetupZoneDAO().save(eventId, zones, mapPath, removeMap)) {
                throw new ServletException("会場マップ・合流エリアを保存できませんでした。");
            }
            saved = null;   // 保存できたので写真は残す

            // 参加者にお知らせ（新しい会場マップがあれば添付）
            if (notify) {
                List<String> shown = zones.isEmpty() ? MeetupZoneDAO.DEFAULT_ZONES : zones;
                NotificationBean n = new NotificationBean();
                n.setEventId(eventId);
                n.setAccountId((Long) event[1]);
                n.setTitle("会場マップ・合流エリアのご案内");
                n.setNotificationText("合流するときは、次のエリアから選んで合図を送ってください。\n"
                        + "・" + String.join("\n・", shown));
                if (mapPath != null) {
                    n.setAttachmentName(mapName);
                    n.setAttachmentPath(mapPath);
                    n.setAttachmentType(mapType);
                    n.setAttachmentSize(mapSize);
                }
                noticeDao.insertNotification(n);
            }

            response.sendRedirect(back.replace("#venue", "&venueSaved=1#venue"));

        } catch (SQLException e) {
            throw new ServletException("会場マップ・合流エリアの保存に失敗しました。", e);
        } finally {
            if (saved != null) saved.delete();
        }
    }

    private Part getPartSafely(HttpServletRequest request, String name) throws IOException, ServletException {
        try {
            return request.getPart(name);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
