package admin;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import Bean.QRCodeBean;
import DAO.QRCodeDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * イベント参加用 QRコード
 *
 *   /admin/QRCode.action?eventId=1            … そのイベントの QRコード
 *   /admin/QRCode.action?eventId=1&host=…     … QRコードに入れる IP アドレスを選び直す
 *
 * QRコードの中身は、参加登録画面の URL：
 *   http://（PCのIPアドレス）:8080/（プロジェクト名）/participant/AccountRegister.action?eventId=1
 *
 * PC で localhost として開いていても、スマホは localhost を開けないので、
 * PC の Wi-Fi などの IP アドレスに置きかえる。
 */
@WebServlet("/admin/QRCode.action")
public class QRCodeAction extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP = "/admin/qrCode.jsp";
    private static final String DASHBOARD = "/admin/EventDashboard.action";
    private static final String REGISTER_PATH = "/participant/AccountRegister.action?eventId=";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ---------- イベント（?eventId= → ダッシュボードで表示中 → 最新） ----------
        QRCodeDAO dao = new QRCodeDAO();
        QRCodeBean bean;
        try {
            long eventId = parseLong(request.getParameter("eventId"));
            if (eventId == 0) {
                Object s = request.getSession().getAttribute("eventId");
                if (s instanceof Number) eventId = ((Number) s).longValue();
            }
            bean = (eventId > 0) ? dao.findEvent(eventId) : null;
            if (bean == null) {
                bean = dao.findLatestEvent();
            }
        } catch (SQLException e) {
            throw new ServletException("イベントを読み込めませんでした", e);
        }

        if (bean == null) {   // イベントが1件も無い
            response.sendRedirect(request.getContextPath() + DASHBOARD);
            return;
        }

        // ---------- QRコードに入れるホスト（IP アドレス） ----------
        String requestHost = request.getServerName();
        List<String[]> options = findLanAddresses();

        // PC 以外（スマホ・ほかの PC・本番サーバー）から開いているときは、そのアドレスで開けるので候補に入れる
        if (!isLocal(requestHost) && !contains(options, requestHost)) {
            options.add(0, new String[] { requestHost, requestHost + "（今開いているアドレス）" });
        }

        String host = request.getParameter("host");
        if (host == null || !contains(options, host)) {          // 候補にないものは使わない
            host = isLocal(requestHost)
                    ? (options.isEmpty() ? requestHost : options.get(0)[0])
                    : requestHost;
        }

        bean.setHostOptions(options);
        bean.setSelectedHost(host);
        bean.setLocalhostOnly(isLocal(host));
        bean.setRegisterUrl(buildUrl(request, host) + REGISTER_PATH + bean.getEventId());

        request.getSession().setAttribute("eventId", bean.getEventId());
        request.setAttribute("qr", bean);
        request.getRequestDispatcher(JSP).forward(request, response);
    }

    // =========================================================
    // URL・IP アドレス
    // =========================================================

    /** http://ホスト:ポート/プロジェクト名 */
    private static String buildUrl(HttpServletRequest request, String host) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port) + request.getContextPath();
    }

    /**
     * この PC の IPv4 アドレス（スマホから開けそうなもの）。
     * Wi-Fi を先頭にし、VirtualBox・VMware・WSL などの仮想ネットワークは後ろにする。
     * {IPアドレス, 表示名}
     */
    private static List<String[]> findLanAddresses() {
        List<String[]> wifi = new ArrayList<>();
        List<String[]> wired = new ArrayList<>();
        List<String[]> virtual = new ArrayList<>();

        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }
                String name = (ni.getDisplayName() == null ? ni.getName() : ni.getDisplayName());
                String lower = name.toLowerCase(Locale.ROOT);

                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (!(addr instanceof Inet4Address) || addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
                        continue;
                    }
                    String[] option = { addr.getHostAddress(), addr.getHostAddress() + "（" + name + "）" };

                    if (lower.contains("virtual") || lower.contains("vmware") || lower.contains("vethernet")
                            || lower.contains("hyper-v") || lower.contains("wsl") || lower.contains("docker")
                            || ni.isVirtual()) {
                        virtual.add(option);
                    } else if (lower.contains("wi-fi") || lower.contains("wifi") || lower.contains("wireless")
                            || lower.contains("wlan") || lower.contains("無線")) {
                        wifi.add(option);
                    } else {
                        wired.add(option);
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        List<String[]> all = new ArrayList<>(wifi);
        all.addAll(wired);
        all.addAll(virtual);
        return all;
    }

    private static boolean isLocal(String host) {
        return host == null || "localhost".equalsIgnoreCase(host)
                || host.startsWith("127.") || "0:0:0:0:0:0:0:1".equals(host) || "::1".equals(host);
    }

    private static boolean contains(List<String[]> options, String host) {
        for (String[] o : options) {
            if (o[0].equals(host)) return true;
        }
        return false;
    }

    private static long parseLong(String s) {
        if (s == null || s.trim().isEmpty()) return 0L;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
