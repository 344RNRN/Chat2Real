package participant;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import Bean.AccountRegisterBean;
import Bean.InterestBean;
import DAO.AccountRegisterDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

/**
 * 参加者アカウント登録
 *
 * GET  … /participant/AccountRegister.action?eventId=1
 *        （QRコードの URL。eventId が無ければいちばん最近のイベント）
 * POST … 入力チェック → 写真を保存 → 参加者・プロフィール・興味を登録
 *        → セッションに参加者を入れてトップ画面へ
 */
@WebServlet("/participant/AccountRegister.action")
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,           // 写真は 5MB まで
        maxRequestSize = 10 * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024)
public class AccountRegisterAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/accountRegister.jsp";
    private static final String acc_complete = "/participants/accountComplete.jsp";
    private static final String UPLOAD_DIR = "/uploads/profiles";
    private static final long MAX_PHOTO_SIZE = 5L * 1024 * 1024;

    /** 年代の選択肢（JSP の <option> と同じ） */
    public static final List<String> AGE_GROUPS =
            Arrays.asList("10代", "20代", "30代", "40代", "50代", "60代以上");

    // =========================================================
    // GET：イベントを決めて画面表示
    // =========================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AccountRegisterBean event = loadEvent(request);
        if (event == null) {
            request.setAttribute("errorMessage", "参加できるイベントがありません");
        }
        forwardToForm(request, response, event, null);
    }

    // =========================================================
    // POST：登録
    // =========================================================
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        AccountRegisterBean bean = loadEvent(request);
        if (bean == null) {
            request.setAttribute("errorMessage", "参加できるイベントがありません");
            forwardToForm(request, response, null, null);
            return;
        }

        // ---------- 1. 入力チェック ----------
        String error = readForm(request, bean);
        if (error != null) {
            forwardToForm(request, response, bean, error);
            return;
        }
        
        // ---------- 1-2. 同じニックネームはこのイベントで1人だけ ----------
        try {
            if (new AccountRegisterDAO().existsNickname(bean.getEventId(), bean.getNickname())) {
                forwardToForm(request, response, bean,
                        "「" + bean.getNickname() + "」は、このイベントですでに使われています。別のニックネームにしてください");
                return;
            }
        } catch (SQLException e) {
            throw new ServletException("ニックネームを確認できませんでした", e);
        }

        // ---------- 2. プロフィール写真（任意） ----------
        File savedPhoto = null;
        try {
            Part photo = request.getPart("photo");
            if (photo != null && photo.getSize() > 0) {
                String type = photo.getContentType() == null ? "" : photo.getContentType();
                if (!"image/jpeg".equals(type) && !"image/png".equals(type)) {
                    forwardToForm(request, response, bean, "プロフィール写真は JPEG か PNG を選んでください");
                    return;
                }
                if (photo.getSize() > MAX_PHOTO_SIZE) {
                    forwardToForm(request, response, bean, "プロフィール写真は5MB以内にしてください");
                    return;
                }

                File dir = new File(getServletContext().getRealPath(UPLOAD_DIR));
                if (!dir.exists() && !dir.mkdirs()) {
                    forwardToForm(request, response, bean, "写真の保存先を作成できませんでした");
                    return;
                }
                String savedName = UUID.randomUUID() + ("image/png".equals(type) ? ".png" : ".jpg");
                savedPhoto = new File(dir, savedName);
                photo.write(savedPhoto.getAbsolutePath());
                bean.setProfileImageUrl(UPLOAD_DIR + "/" + savedName);
            }
        } catch (IllegalStateException e) {
            forwardToForm(request, response, bean, "プロフィール写真は5MB以内にしてください");
            return;
        }

        // ---------- 3. 登録 ----------
        try {
            new AccountRegisterDAO().register(bean);
        } catch (SQLException e) {
            System.out.println("[AccountRegister] 登録に失敗：" + e.getMessage());
            e.printStackTrace();
            if (savedPhoto != null && savedPhoto.exists()) {
                savedPhoto.delete();   // DB に入らなかった写真は消す
            }
            forwardToForm(request, response, bean, "登録に失敗しました。もう一度お試しください");
            return;
        }

        System.out.println("[AccountRegister] 登録しました：event_participant_id = " + bean.getEventParticipantId());

        // ---------- 4. 参加者としてセッションに入れてトップ画面へ ----------
        HttpSession session = request.getSession();
        request.changeSessionId();   // セッション固定攻撃の対策
        session.setAttribute("eventParticipantId", bean.getEventParticipantId());   // 終了レポート画面などで使う
        session.setAttribute("participantId", bean.getParticipantId());
        session.setAttribute("eventId", bean.getEventId());

        response.sendRedirect(request.getContextPath() +acc_complete);
    }

    // =========================================================
    // 入力の読み取りとチェック（エラーがなければ null）
    // =========================================================
    private String readForm(HttpServletRequest request, AccountRegisterBean bean) {
        String nickname = trim(request.getParameter("nickname"));
        String realName = trim(request.getParameter("realName"));
        String ageGroup = trim(request.getParameter("ageGroup"));
        String comment = trim(request.getParameter("comment"));
        String other = trim(request.getParameter("otherInterest"));

        bean.setNickname(nickname);
        bean.setRealName(realName);
        bean.setAgeGroup(ageGroup);
        bean.setSelfIntroduction(comment.isEmpty() ? null : comment);

        String[] ids = request.getParameterValues("interestIds");
        if (ids != null) {
            for (String id : ids) {
                try {
                    long v = Long.parseLong(id);
                    if (!bean.getInterestIds().contains(v)) bean.getInterestIds().add(v);
                } catch (NumberFormatException ignore) {
                    // 不正な値は無視
                }
            }
        }

        // 「その他」は 、 , ， で区切って複数書ける
        if (!other.isEmpty()) {
            for (String s : other.split("[、,，]")) {
                String t = s.trim();
                if (!t.isEmpty() && !bean.getOtherInterests().contains(t)) {
                    if (t.length() > 50) return "その他の興味は1つ50文字以内で入力してください";
                    bean.getOtherInterests().add(t);
                }
            }
        }
        if (other.length() > 100) return "その他の興味は100文字以内で入力してください";

        if (nickname.isEmpty()) return "ニックネームを入力してください";
        if (nickname.length() > 20) return "ニックネームは20文字以内で入力してください";
        if (realName.isEmpty()) return "本名を入力してください";
        if (realName.length() > 30) return "本名は30文字以内で入力してください";
        if (!AGE_GROUPS.contains(ageGroup)) return "年代を選んでください";
        if (bean.getInterestIds().isEmpty() && bean.getOtherInterests().isEmpty())
            return "興味・特徴を1つ以上選ぶか、その他に入力してください";
        if (comment.length() > 50) return "一言・参加目的は50文字以内で入力してください";

        return null;
    }

    // =========================================================
    // 共通
    // =========================================================

    /** ?eventId=… → 無ければいちばん最近のイベント */
    private AccountRegisterBean loadEvent(HttpServletRequest request) throws ServletException {
        AccountRegisterDAO dao = new AccountRegisterDAO();
        try {
            String param = trim(request.getParameter("eventId"));
            if (!param.isEmpty()) {
                try {
                    AccountRegisterBean b = dao.findEvent(Long.parseLong(param));
                    if (b != null) return b;
                } catch (NumberFormatException ignore) {
                    // 数字でなければ最新のイベントにする
                }
            }
            return dao.findLatestEvent();
        } catch (SQLException e) {
            throw new ServletException("イベントを読み込めませんでした", e);
        }
    }

    private void forwardToForm(HttpServletRequest request, HttpServletResponse response,
                               AccountRegisterBean bean, String error)
            throws ServletException, IOException {

        List<InterestBean> interests = new ArrayList<>();
        if (bean != null) {
            try {
                interests = new AccountRegisterDAO().findInterests(bean.getEventId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        request.setAttribute("register", bean);
        request.setAttribute("interestList", interests);
        request.setAttribute("ageGroups", AGE_GROUPS);
        if (error != null) {
            request.setAttribute("errorMessage", error);
        }
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}