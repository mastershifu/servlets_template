<%@ page contentType="text/html;charset=UTF-8" %>
<%
    if (session.getAttribute("username") != null) {
        response.sendRedirect(request.getContextPath() + "/home");
    } else {
        response.sendRedirect(request.getContextPath() + "/login");
    }
%>
