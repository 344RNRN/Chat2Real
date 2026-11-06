package admin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Bean.EventEditBean;
import DAO.EventEditDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * イベント編集（画面ID：EventEdit）
 *
 * GET  … /admin/EventEdit.action?eventId=1 で今の内容を表示
 *        （eventId が無ければセッションの eventId = ダッシュボードで表示中のイベント）
 * POST … 入力チェック → 資料を保存 → DB 更新 → ダッシュボードへ
 *
 * ログインチェックはしない
 */
@WebServlet("/admin/EventEdit.action")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,          // 1ファイル 10MB まで
        maxRequestSize = 60 * 1024 * 1024,       // 全体 60MB まで
        fileSizeThreshold = 1024 * 1024)
public class EventEditAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/eventEdit.jsp";
    private static final String DASHBOARD = "/admin/EventDashboard.action";
    private static final String UPLOAD_DIR = "/uploads/materials";
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_QUESTIONS = 5;

    // =========================================================
    // GET：今の内容を表示
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Long eventId = getEventId(request);
        if (eventId == null) {
            response.sendRedirect(request.getContextPath() + DASHBOARD);
            return;
        }

        EventEditBean bean;
        try {
            bean = new EventEditDAO().findById(eventId);
        } catch (SQLException e) {
            throw new ServletException("イベントを読み込めませんでした", e);
        }

        if (bean == null) {
            System.out.println("[EventEdit] イベントが見つかりません：event_id = " + eventId);
            response.sendRedirect(request.getContextPath() + DASHBOARD);
            return;
        }

        forwardToForm(request, response, bean, null);
    }

    // =========================================================
    // POST：保存ボタン
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        Long eventId = parseLong(request.getParameter("eventId"));
        if (eventId == null) {
            response.sendRedirect(request.getContextPath() + DASHBOARD);
            return;
        }

        EventEditDAO dao = new EventEditDAO();
        EventEditBean bean = new EventEditBean();
        bean.setEventId(eventId);

        // 画面に戻すときにも使うので、エラーがあっても先に全部読む
        readInterests(request, bean);
        readDeleteMaterials(request, bean);
        String questionError = readQuestions(request, bean);
        String error = readEventInfo(request, bean);
        if (error == null) error = questionError;

        try {
            bean.setQuestionsLocked(dao.hasAnswers(eventId));
        } catch (SQLException e) {
            throw new ServletException("イベントを読み込めませんでした", e);
        }

        if (error != null) {
            System.out.println("[EventEdit] 入力エラー：" + error);
            forwardToForm(request, response, bean, error);
            return;
        }

        // ---------- 資料を保存 ----------
        List<File> savedFiles = new ArrayList<>();
        try {
            error = saveMaterials(request, bean, savedFiles);
        } catch (IllegalStateException e) {
            error = "資料は1ファイル10MB以内にしてください";
        }
        if (error != null) {
            deleteFiles(savedFiles);
            forwardToForm(request, response, bean, error);
            return;
        }

        // ---------- DB 更新 ----------
        List<String> deletedPaths;
        try {
            deletedPaths = dao.update(bean);
        } catch (SQLException e) {
            System.out.println("[EventEdit] DB 更新に失敗：" + e.getMessage());
            e.printStackTrace();
            deleteFiles(savedFiles);
            forwardToForm(request, response, bean, "イベントの更新に失敗しました。もう一度お試しください");
            return;
        }

        // DB から消した資料のファイルも消す
        for (String path : deletedPaths) {
            String real = getServletContext().getRealPath(path);
            if (real != null) {
                deleteFiles(List.of(new File(real)));
            }
        }

        System.out.println("[EventEdit] 更新しました：event_id = " + eventId);
        request.getSession().setAttribute("eventId", eventId);
        response.sendRedirect(request.getContextPath() + DASHBOARD + "?eventId=" + eventId);
    }

    // =========================================================
    // イベント情報
    // =========================================================
    private String readEventInfo(HttpServletRequest request, EventEditBean bean) {
        String eventName = trim(request.getParameter("eventName"));
        String eventDate = trim(request.getParameter("eventDate"));
        String startTime = trim(request.getParameter("startTime"));
        String endTime = trim(request.getParameter("endTime"));
        String venue = trim(request.getParameter("venue"));
        String capacity = trim(request.getParameter("capacity"));
        String description = trim(request.getParameter("eventDescription"));

        if (eventName.isEmpty()) return "イベント名を入力してください";
        if (eventName.length() > 150) return "イベント名は150文字以内で入力してください";
        if (eventDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty())
            return "開催日と時間を入力してください";
        if (venue.isEmpty()) return "会場を入力してください";
        if (venue.length() > 255) return "会場は255文字以内で入力してください";
        if (description.length() > 1000) return "イベント説明は1000文字以内で入力してください";

        try {
            LocalDate date = LocalDate.parse(eventDate);
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            if (!end.isAfter(start)) return "終了時間は開始時間より後にしてください";

            bean.setEventDatetime(Timestamp.valueOf(LocalDateTime.of(date, start)));
            bean.setEndDatetime(Timestamp.valueOf(LocalDateTime.of(date, end)));
        } catch (DateTimeParseException e) {
            return "開催日・時間の形式が正しくありません";
        }

        if (!capacity.isEmpty()) {
            try {
                int cap = Integer.parseInt(capacity);
                if (cap < 1 || cap > 9999) return "定員は1～9999で入力してください";
                bean.setCapacity(cap);
            } catch (NumberFormatException e) {
                return "定員は数字で入力してください";
            }
        }

        bean.setEventName(eventName);
        bean.setVenue(venue);
        bean.setEventDescription(description.isEmpty() ? null : description);
        return null;
    }

    // =========================================================
    // 参加者への質問（q1_text, q1_type, q1_options, q1_required …）
    // 先に全部読んでから（画面に戻すため）チェックする
    // =========================================================
    private String readQuestions(HttpServletRequest request, EventEditBean bean) {
        int count;
        try {
            count = Integer.parseInt(trim(request.getParameter("questionCount")));
        } catch (NumberFormatException e) {
            count = 0;
        }
        count = Math.min(count, MAX_QUESTIONS);

        for (int n = 1; n <= count; n++) {
            EventEditBean.Question q = new EventEditBean.Question();
            q.setQuestionText(trim(request.getParameter("q" + n + "_text")));
            q.setQuestionType(EventEditBean.Question.TYPE_SELECT.equals(trim(request.getParameter("q" + n + "_type")))
                    ? EventEditBean.Question.TYPE_SELECT : EventEditBean.Question.TYPE_TEXT);
            q.setOptions(trim(request.getParameter("q" + n + "_options")));
            q.setRequired("true".equals(request.getParameter("q" + n + "_required")));
            bean.getQuestions().add(q);
        }

        int n = 1;
        for (EventEditBean.Question q : bean.getQuestions()) {
            if (q.getQuestionText().isEmpty()) return "質問" + n + "の質問文を入力してください";
            if (q.getQuestionText().length() > 500) return "質問" + n + "は500文字以内で入力してください";
            if (q.isSelect()) {
                if (q.getOptions().isEmpty()) return "質問" + n + "の選択肢を入力してください";
                if (q.getOptions().length() > 500) return "質問" + n + "の選択肢は500文字以内で入力してください";
            }
            n++;
        }
        return null;
    }

    // =========================================================
    // 興味タグ・削除する資料
    // =========================================================
    private void readInterests(HttpServletRequest request, EventEditBean bean) {
        String[] ids = request.getParameterValues("interestIds");
        if (ids != null) {
            for (String id : ids) {
                Long v = parseLong(id);
                if (v != null && !bean.getInterestIds().contains(v)) {
                    bean.getInterestIds().add(v);
                }
            }
        }

        String[] names = request.getParameterValues("newInterests");
        if (names != null) {
            for (String name : names) {
                String t = trim(name);
                if (!t.isEmpty() && t.length() <= 50 && !bean.getNewInterests().contains(t)) {
                    bean.getNewInterests().add(t);
                }
            }
        }
    }

    private void readDeleteMaterials(HttpServletRequest request, EventEditBean bean) {
        String[] ids = request.getParameterValues("deleteMaterialIds");
        if (ids != null) {
            for (String id : ids) {
                Long v = parseLong(id);
                if (v != null) {
                    bean.getDeleteMaterialIds().add(v);
                }
            }
        }
    }

    // =========================================================
    // 新しい資料の保存（画像・PDF、1ファイル10MB以内）
    // =========================================================
    private String saveMaterials(HttpServletRequest request, EventEditBean bean, List<File> savedFiles)
            throws IOException, ServletException {

        File dir = new File(getServletContext().getRealPath(UPLOAD_DIR));
        if (!dir.exists() && !dir.mkdirs()) {
            return "資料の保存先を作成できませんでした";
        }

        for (Part part : request.getParts()) {
            if (!"materials".equals(part.getName()) || part.getSize() == 0) {
                continue;
            }

            String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            String type = part.getContentType() == null ? "" : part.getContentType();

            boolean okType = type.startsWith("image/") || "application/pdf".equals(type);
            if (!okType) return "資料は画像またはPDFを選択してください：" + originalName;
            if (part.getSize() > MAX_FILE_SIZE) return "10MBを超えるファイルは追加できません：" + originalName;

            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) ext = originalName.substring(dot).toLowerCase();
            String savedName = UUID.randomUUID().toString() + ext;

            File file = new File(dir, savedName);
            part.write(file.getAbsolutePath());
            savedFiles.add(file);

            EventEditBean.Material m = new EventEditBean.Material();
            m.setFileName(originalName);
            m.setFilePath(UPLOAD_DIR + "/" + savedName);
            m.setFileType(type);
            m.setFileSize(part.getSize());
            bean.getNewMaterials().add(m);
        }
        return null;
    }

    // =========================================================
    // 共通
    // =========================================================

    /** 画面に出す（タグ一覧・登録済み資料を読み直す） */
    private void forwardToForm(HttpServletRequest request, HttpServletResponse response,
                               EventEditBean bean, String error)
            throws ServletException, IOException {

        EventEditDAO dao = new EventEditDAO();
        try {
            request.setAttribute("interestList", dao.findActiveInterests());
            bean.setMaterials(dao.findMaterials(bean.getEventId()));
            // 質問が変更できないときは、表示用に DB の質問を読み直す（フォームからは送られてこないため）
            if (bean.isQuestionsLocked()) {
                bean.setQuestions(dao.findQuestions(bean.getEventId()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("interestList", new ArrayList<>());
            if (error == null) error = "興味タグ・資料を読み込めませんでした";
        }

        request.setAttribute("event", bean);
        if (error != null) {
            request.setAttribute("errorMessage", error);
        }
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /** ?eventId= → 無ければセッションの eventId */
    private Long getEventId(HttpServletRequest request) {
        Long id = parseLong(request.getParameter("eventId"));
        if (id != null) return id;

        Object s = request.getSession().getAttribute("eventId");
        return (s instanceof Long) ? (Long) s : null;
    }

    private void deleteFiles(List<File> files) {
        for (File f : files) {
            if (f.exists() && !f.delete()) {
                System.err.println("ファイルを削除できませんでした：" + f.getAbsolutePath());
            }
        }
    }

    private static Long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}