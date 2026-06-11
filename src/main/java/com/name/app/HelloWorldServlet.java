package com.name.app;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/hello")
public class HelloWorldServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        if (name == null || name.trim().isEmpty()) {
            name = "World";
        }

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("  <head><title>Hello Servlet</title></head>");
        out.println("  <body>");
        out.println("    <h1>Hello, " + name + "!</h1>");
        out.println("    <p>Served by: " + getClass().getSimpleName() + "</p>");
        out.println("  </body>");
        out.println("</html>");
    }
}
