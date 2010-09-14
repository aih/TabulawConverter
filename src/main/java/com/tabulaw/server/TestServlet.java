package com.tabulaw.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			java.io.IOException {

		response.getWriter().println("<html><body> hello world 67!!!89</body></html>");
	}
}