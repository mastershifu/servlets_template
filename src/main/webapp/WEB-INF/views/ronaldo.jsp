<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Ronaldo</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; }
        .topbar { background: #2f6feb; color: #fff; padding: 14px 24px; display: flex; justify-content: space-between; align-items: center; }
        .topbar a { color: #fff; text-decoration: none; font-size: 14px; margin-left: 16px; }
        .content { padding: 30px; }
    </style>
</head>
<body>
    <div class="topbar">
        <span>Welcome, <%= session.getAttribute("username") %></span>
        <span>
            <a href="${pageContext.request.contextPath}/home">Home</a>
            <a href="${pageContext.request.contextPath}/logout">Log out</a>
        </span>
    </div>
    <div class="content">
        <h1>Cristiano Ronaldo</h1>
        <p>Cristiano Ronaldo is a Portuguese professional footballer widely regarded as one of the greatest players of all time.</p>
        <p><a href="${pageContext.request.contextPath}/home">&larr; Back to menu</a></p>
    </div>
</body>
</html>
