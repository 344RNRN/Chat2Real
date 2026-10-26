package chat2real;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple test servlet. Open http://localhost:8080/Chat2Real/hello
 * (add ?name=Taro to greet someone by name).
 */
@WebServlet("/hello")
public class HelloServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		String name = request.getParameter("name");
		if (name == null || name.isBlank()) {
			name = "Guest";
		}
		String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"ja\">");
		out.println("<head><meta charset=\"UTF-8\"><title>Chat2Real - Hello</title></head>");
		out.println("<body>");
		out.println("<h1>Hello, " + escapeHtml(name) + "!</h1>");
		out.println("<p>Server time: " + now + "</p>");
		out.println("<p><a href=\"" + request.getContextPath() + "/\">Back to top</a></p>");
		out.println("</body>");
		out.println("</html>");
	}

	// Prevents HTML injection through the name parameter
	private static String escapeHtml(String s) {
		return s.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
