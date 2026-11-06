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
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;

import Bean.AccountBean;
import Bean.AdminLogin;
import Bean.EventCreateBean;
import DAO.EventCreateDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

/**
 * イベント作成（画面ID：EventCreate）
 *
 * GET  … イベント作成画面を表示（基本フロー1）
 * POST … 入力チェック → 資料を保存 → DB 登録 → イベント管理画面へ（基本フロー4・5）
 *
 * ※Tomcat 9 以前（javax）の場合は import の jakarta を javax に変える
 */
@WebServlet("/admin/EventCreate.action")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,          // 1ファイル 10MB まで
        maxRequestSize = 60 * 1024 * 1024,       // 全体 60MB まで
        fileSizeThreshold = 1024 * 1024)
public class EventCreateAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/eventCreate.jsp";
    private static final String NO_ACCOUNT =
            "運営者アカウントがありません。先に「運営者アカウント作成」でアカウントを作ってください";
    private static final String UPLOAD_DIR = "/uploads/materials";
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final int MAX_QUESTIONS = 5;

    // =========================================================
    // GET：画面表示
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ログインチェックなし（アカウントが無いときだけ画面上にメッセージを出す）
        AdminLogin admin = getLoginAdmin(request);
        forwardToForm(request, response, admin == null ? NO_ACCOUNT : null);
    }

    // =========================================================
    // POST：作成ボタン
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // ログインチェックなし。ただしイベントには account_id が必要なので、アカウントが無いときは登録できない
        AdminLogin admin = getLoginAdmin(request);
        if (admin == null) {
            System.out.println("[EventCreate] 運営者アカウントが無いので登録できません");
            forwardToForm(request, response, NO_ACCOUNT);
            return;
        }

        // ---------- 1. 入力値を Bean に詰めながらチェック（代替フロー4a-1） ----------
        EventCreateBean bean = new EventCreateBean();
        bean.setAccountId(admin.getAccountId());

        String error = readEventInfo(request, bean);
        if (error == null) error = readQuestions(request, bean);
        if (error == null) readInterests(request, bean);

        if (error != null) {
            System.out.println("[EventCreate] 入力エラー：" + error);
            forwardToForm(request, response, error);
            return;
        }

        // ---------- 2. 資料を保存（代替フロー3a-1） ----------
        List<File> savedFiles = new ArrayList<>();
        try {
            error = saveMaterials(request, bean, savedFiles);
        } catch (IllegalStateException e) {
            // @MultipartConfig のサイズ上限を超えたとき
            error = "資料は1ファイル10MB以内にしてください";
        }
        if (error != null) {
            deleteFiles(savedFiles);
            forwardToForm(request, response, error);
            return;
        }

        // ---------- 3. DB 登録（基本フロー5） ----------
        long eventId;
        try {
            eventId = new EventCreateDAO().create(bean);
        } catch (SQLException e) {
            System.out.println("[EventCreate] DB 登録に失敗：" + e.getMessage());
            e.printStackTrace();
            deleteFiles(savedFiles);   // DB に入らなかったファイルは消す
            forwardToForm(request, response, "イベントの登録に失敗しました。もう一度お試しください");
            return;
        }

        System.out.println("[EventCreate] 登録しました：event_id = " + eventId);

        // ---------- 4. イベント管理画面へ ----------
        // 作成したイベントをセッションに入れておく（ダッシュボード・QRコード画面で使う）
        request.getSession().setAttribute("eventId", eventId);
        response.sendRedirect(request.getContextPath() + "/admin/EventDashboard.action");
    }

    // =========================================================
    // ②～⑦ イベント情報
    // =========================================================
    private String readEventInfo(HttpServletRequest request, EventCreateBean bean) {
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

        // 開催日 + 時間 → Timestamp
        try {
            LocalDate date = LocalDate.parse(eventDate);          // 2026-10-18
            LocalTime start = LocalTime.parse(startTime);         // 19:00
            LocalTime end = LocalTime.parse(endTime);             // 21:00

            if (!end.isAfter(start)) return "終了時間は開始時間より後にしてください";

            bean.setEventDatetime(Timestamp.valueOf(LocalDateTime.of(date, start)));
            bean.setEndDatetime(Timestamp.valueOf(LocalDateTime.of(date, end)));
        } catch (DateTimeParseException e) {
            return "開催日・時間の形式が正しくありません";
        }

        // 定員（任意）
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
    // ⑫～⑱ 参加者への質問（q1_text, q1_type, q1_options, q1_required …）
    // =========================================================
    private String readQuestions(HttpServletRequest request, EventCreateBean bean) {
        int count;
        try {
            count = Integer.parseInt(trim(request.getParameter("questionCount")));
        } catch (NumberFormatException e) {
            count = 0;
        }
        if (count > MAX_QUESTIONS) return "質問は最大" + MAX_QUESTIONS + "問までです";

        for (int n = 1; n <= count; n++) {
            String text = trim(request.getParameter("q" + n + "_text"));
            String type = trim(request.getParameter("q" + n + "_type"));
            String options = trim(request.getParameter("q" + n + "_options"));
            boolean required = "true".equals(request.getParameter("q" + n + "_required"));

            if (text.isEmpty()) return "質問" + n + "の質問文を入力してください";
            if (text.length() > 500) return "質問" + n + "は500文字以内で入力してください";

            EventCreateBean.Question q = new EventCreateBean.Question();
            q.setQuestionText(text);
            q.setRequired(required);

            if (EventCreateBean.Question.TYPE_SELECT.equals(type)) {
                if (options.isEmpty()) return "質問" + n + "の選択肢を入力してください";
                if (options.length() > 500) return "質問" + n + "の選択肢は500文字以内で入力してください";
                q.setQuestionType(EventCreateBean.Question.TYPE_SELECT);
                q.setOptions(options);
            } else {
                q.setQuestionType(EventCreateBean.Question.TYPE_TEXT);
            }
            bean.getQuestions().add(q);
        }
        return null;
    }

    // =========================================================
    // ⑩⑪ 興味タグ
    // =========================================================
    private void readInterests(HttpServletRequest request, EventCreateBean bean) {
        String[] ids = request.getParameterValues("interestIds");
        if (ids != null) {
            for (String id : ids) {
                try {
                    bean.getInterestIds().add(Long.parseLong(id));
                } catch (NumberFormatException ignore) {
                    // 不正な値は無視
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

    // =========================================================
    // ⑧⑨ 資料の保存（画像・PDF、1ファイル10MB以内）
    // =========================================================
    private String saveMaterials(HttpServletRequest request, EventCreateBean bean, List<File> savedFiles)
            throws IOException, ServletException {

        String dirPath = getServletContext().getRealPath(UPLOAD_DIR);
        File dir = new File(dirPath);
        if (!dir.exists() && !dir.mkdirs()) {
            return "資料の保存先を作成できませんでした";
        }

        for (Part part : request.getParts()) {
            if (!"materials".equals(part.getName()) || part.getSize() == 0) {
                continue;   // ファイル未選択
            }

            String originalName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            String type = part.getContentType() == null ? "" : part.getContentType();

            boolean okType = type.startsWith("image/") || "application/pdf".equals(type);
            if (!okType) return "資料は画像またはPDFを選択してください：" + originalName;
            if (part.getSize() > MAX_FILE_SIZE) return "10MBを超えるファイルは追加できません：" + originalName;

            // 同じ名前で上書きしないように UUID を付ける
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0) ext = originalName.substring(dot).toLowerCase();
            String savedName = UUID.randomUUID().toString() + ext;

            File file = new File(dir, savedName);
            part.write(file.getAbsolutePath());
            savedFiles.add(file);

            EventCreateBean.Material m = new EventCreateBean.Material();
            m.setFileName(originalName);
            m.setFilePath(UPLOAD_DIR + "/" + savedName);
            m.setFileType(type);
            m.setFileSize(part.getSize());
            bean.getMaterials().add(m);
        }
        return null;
    }

    // =========================================================
    // 共通
    // =========================================================

    /** 画面に戻す（タグ一覧を読み直してエラーメッセージを付ける） */
    private void forwardToForm(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {

        AdminLogin admin = getLoginAdmin(request);
        if (admin != null) {
            // eventCreate.jsp は session の accountName を表示する
            request.getSession().setAttribute("accountName", admin.getAccountName());
        }

        try {
            request.setAttribute("interestList", new EventCreateDAO().findActiveInterests());
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("interestList", new ArrayList<>());
            if (error == null) error = "興味タグを読み込めませんでした";
        }

        if (error != null) {
            request.setAttribute("errorMessage", error);
        }
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /**
     * 運営者を取り出す（ログインチェックはしない）
     *   1. セッションに AdminLogin / AccountBean があればそれ
     *   2. 無ければ t001_account の最初のアカウント
     *   3. アカウントが1件も無ければ null
     */
    private AdminLogin getLoginAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                Object value = session.getAttribute(names.nextElement());
                if (value instanceof AdminLogin) {
                    return (AdminLogin) value;
                }
                if (value instanceof AccountBean) {
                    AccountBean a = (AccountBean) value;
                    return new AdminLogin(a.getAccountId(), a.getLoginId(), a.getAccountName());
                }
            }
        }

        try {
            return new EventCreateDAO().findFirstAccount();
        } catch (SQLException e) {
            System.out.println("[EventCreate] アカウントを読み込めませんでした：" + e.getMessage());
            return null;
        }
    }

    private void deleteFiles(List<File> files) {
        for (File f : files) {
            if (f.exists() && !f.delete()) {
                System.err.println("ファイルを削除できませんでした：" + f.getAbsolutePath());
            }
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}