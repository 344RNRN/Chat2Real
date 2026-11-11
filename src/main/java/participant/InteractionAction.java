package participant;

import java.io.IOException;
import java.sql.SQLException;

import Bean.ChatBean;
import Bean.ChatPartnerBean;
import Bean.InteractionBean;
import DAO.ChatDAO;
import DAO.InteractionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 交流申請（チャット画面の「申請」ボタン）
 *   GET  /interaction/request?partnerId=…               … 交流申請画面
 *   GET  /interaction/request?partnerId=…&format=json   … チャット画面のお知らせ用（今の状態だけ）
 *   POST /interaction/request（partnerId・op）          … op = request / approve / decline / cancel
 *
 * お互いが「直接交流したい」になったら（承認）→ 相互承認画面 → 合流画面 → 交流結果登録
 */
@WebServlet("/interaction/request")
public class InteractionAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/participants/interaction.jsp";
    private static final String MUTUAL = "/participant/MutualConsent.action?exchangeRequestId=";

    private final transient ChatDAO chatDao = new ChatDAO();
    private final transient InteractionDAO dao = new InteractionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        boolean json = "json".equals(req.getParameter("format"));
        InteractionBean b;
        try {
            b = load(req);
        } catch (SQLException e) {
            throw new ServletException("交流申請の情報を読み込めませんでした。", e);
        }

        if (json) {
            resp.setContentType("application/json; charset=UTF-8");
            resp.setHeader("Cache-Control", "no-store");
            if (b == null) {
                resp.getWriter().write("{\"status\":null}");
                return;
            }
            resp.getWriter().write("{\"status\":" + (b.getStatus() == null ? "null" : "\"" + b.getStatus() + "\"")
                    + ",\"incoming\":" + b.isIncoming()
                    + ",\"waiting\":" + b.isWaiting()
                    + ",\"approved\":" + b.isApproved()
                    + ",\"declined\":" + b.isDeclinedForMe()
                    + ",\"exchangeRequestId\":" + b.getExchangeRequestId() + "}");
            return;
        }

        if (b != null && b.isApproved()) {
            // 相互承認が成立 → 相互承認画面へ
            resp.sendRedirect(req.getContextPath() + MUTUAL + b.getExchangeRequestId());
            return;
        }

        req.setAttribute("interaction", b);
        HttpSession s = req.getSession();
        req.setAttribute("flash", s.getAttribute("interactionFlash"));
        s.removeAttribute("interactionFlash");
        req.getRequestDispatcher(JSP).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String op = req.getParameter("op");

        try {
            InteractionBean b = load(req);
            if (b == null) {
                resp.sendRedirect(req.getContextPath() + "/participant/ParticipantList.action");
                return;
            }
            String back = req.getContextPath() + "/interaction/request?partnerId=" + b.getPartnerId();
            HttpSession s = req.getSession();

            if ("request".equals(op) && b.isCanRequest()) {
                if (dao.request(b.getChatId(), b.getMyId(), b.getPartnerId()) > 0) {
                    s.setAttribute("interactionFlash", "交流申請を送りました。");
                }
            } else if ("approve".equals(op) && b.isIncoming()) {
                if (dao.answer(b.getExchangeRequestId(), b.getMyId(), InteractionDAO.APPROVED)) {
                    resp.sendRedirect(req.getContextPath() + MUTUAL + b.getExchangeRequestId());
                    return;
                }
            } else if ("decline".equals(op) && b.isIncoming()) {
                if (dao.answer(b.getExchangeRequestId(), b.getMyId(), InteractionDAO.DECLINED)) {
                    s.setAttribute("interactionFlash", "今回はお断りしました。チャットはこのまま続けられます。");
                }
            } else if ("cancel".equals(op) && b.isWaiting()) {
                if (dao.cancel(b.getExchangeRequestId(), b.getMyId())) {
                    s.setAttribute("interactionFlash", "交流申請を取り消しました。");
                }
            }
            resp.sendRedirect(back);       // 再読み込みで二重送信しないよう redirect

        } catch (SQLException e) {
            throw new ServletException("交流申請を更新できませんでした。", e);
        }
    }

    /** 自分・相手・チャット・いちばん新しい申請。分からなければ null */
    private InteractionBean load(HttpServletRequest req) throws SQLException {
        HttpSession session = req.getSession(false);
        Object v = (session == null) ? null : session.getAttribute("eventParticipantId");
        if (!(v instanceof Number)) {
            return null;
        }
        long myId = ((Number) v).longValue();

        long partnerId;
        try {
            partnerId = Long.parseLong(req.getParameter("partnerId").trim());
        } catch (RuntimeException e) {
            return null;
        }
        if (partnerId == myId) {
            return null;
        }

        ChatPartnerBean me = chatDao.findPartner(myId);
        ChatPartnerBean partner = chatDao.findPartner(partnerId);
        if (me == null || partner == null || me.getEventId() != partner.getEventId()) {
            return null;
        }
        ChatBean chat = chatDao.findOrCreatePersonalChat(me.getEventId(), myId, partnerId);

        InteractionBean b = new InteractionBean();
        b.setChatId(chat.getChatId());
        b.setMyId(myId);
        b.setPartnerId(partnerId);
        b.setPartnerName(partner.getDisplayName());
        b.setPartnerPhoto(partner.getPhotoUrl());
        dao.loadLatest(b);
        return b;
    }
}
