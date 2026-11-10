package admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import Bean.AccountProfileBean;
import Bean.ParticipantListBean;
import DAO.AccountProfileDAO;
import DAO.ParticipantListDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import participant.AccountProfileAction;

/**
 * 参加者一覧
 *
 *   /participant/ParticipantList.action
 *     ?keyword=みお     … ニックネームで検索（本名では検索しない）
 *     &ageGroup=20代    … 年代で絞り込み
 *     &interestId=3     … 趣味で絞り込み
 *     &all=1            … 全員表示（ふだんは PAGE_SIZE 人まで）
 *
 * カードを押すと、その人のプロフィール（AccountProfile.action?id=…）を開く。
 * 「チャットする」は 1対1チャット（AccountProfileAction.PRIVATE_CHAT）を開く。
 */
@WebServlet("/participant/ParticipantList.action")
public class ParticipantListAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/participantList.jsp";
    private static final String REGISTER = "/participant/AccountRegister.action";

    /** 最初に表示する人数 */
    private static final int PAGE_SIZE = 20;

    /** 年代の選択肢（参加者アカウント登録画面と同じ） */
    private static final List<String> AGE_GROUPS =
            Arrays.asList("10代", "20代", "30代", "40代", "50代", "60代以上");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        long myId = getMyEventParticipantId(request);

        String keyword = trim(request.getParameter("keyword"));
        if (keyword.length() > 20) keyword = keyword.substring(0, 20);   // ニックネームは20文字まで
        String ageGroup = trim(request.getParameter("ageGroup"));
        if (!AGE_GROUPS.contains(ageGroup)) ageGroup = "";
        long interestId = parseLong(request.getParameter("interestId"));
        boolean showAll = "1".equals(request.getParameter("all"));

        ParticipantListDAO dao = new ParticipantListDAO();
        ParticipantListBean bean = new ParticipantListBean();

        try {
            // 自分（参加登録していなければ登録画面へ）
            AccountProfileBean me = new AccountProfileDAO().findByEventParticipantId(myId);
            if (me == null) {
                response.sendRedirect(request.getContextPath() + REGISTER);
                return;
            }

            bean.setMyEventParticipantId(me.getEventParticipantId());
            bean.setEventId(me.getEventId());
            bean.setEventName(me.getEventName());
            bean.setCanChat(me.isWantToMeet());
            bean.setMyInterests(me.getInterests());

            bean.setKeyword(keyword);
            bean.setAgeGroup(ageGroup);
            bean.setInterestId(interestId);
            bean.setAgeGroupOptions(AGE_GROUPS);
            bean.setInterestOptions(dao.findInterestOptions(me.getEventId(), me.getEventParticipantId()));
            bean.setWantCount(dao.countWant(me.getEventId(), me.getEventParticipantId()));

            dao.search(bean, keyword, ageGroup, interestId, showAll ? 0 : PAGE_SIZE);

        } catch (SQLException e) {
            throw new ServletException("参加者一覧を読み込めませんでした。", e);
        }

        request.setAttribute("list", bean);
        request.setAttribute("chatBase", request.getContextPath() + AccountProfileAction.PRIVATE_CHAT);
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    /**
     * 開発用：参加登録をしていない（セッションが空の）ときに、DB の最新の参加者で画面を確認できるようにする。
     * ※本番では false にする（false なら参加登録画面に移動する）
     */
    private static final boolean DEV_FALLBACK = true;

    /** セッションから自分の event_participant_id を取り出す（無ければ 0） */
    private static long getMyEventParticipantId(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object id = session.getAttribute("eventParticipantId");
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        if (!DEV_FALLBACK) {
            return 0L;
        }

        // 【開発用】最新のイベントの最新の参加者として表示する
        try {
            AccountProfileDAO dao = new AccountProfileDAO();
            long devId = dao.findLatestEventParticipantId();
            AccountProfileBean dev = (devId > 0) ? dao.findByEventParticipantId(devId) : null;
            if (dev == null) {
                return 0L;
            }
            session.setAttribute("eventParticipantId", dev.getEventParticipantId());
            session.setAttribute("participantId", dev.getParticipantId());
            session.setAttribute("eventId", dev.getEventId());
            session.setAttribute("participant_id", dev.getParticipantId());   // 全体チャット用
            session.setAttribute("event_id", dev.getEventId());
            System.out.println("[開発用] セッションに参加者が無いため、event_participant_id = "
                    + devId + "（" + dev.getNickname() + "）として表示します");
            return devId;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private static long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}