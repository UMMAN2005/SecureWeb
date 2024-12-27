<%@ page import="java.sql.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
    <link rel="stylesheet" href="static/css/styles.css">
    <link rel="stylesheet" href="static/css/messages.css">
    <title>Message Board</title>
</head>
<body>

<h1>Message Board!</h1>

<%-- Search form to filter messages by keyword --%>
<form method="get" action="messageboard">
    <label for="keyword">Search:</label>
    <input type="text" id="keyword" name="keyword" value="<%= request.getParameter("keyword") %>">
    <button type="submit">Search</button>
</form>

<h2>Messages</h2>

<%-- Displaying messages if they exist --%>
<%
    String keyword = request.getParameter("keyword");
    String sql = "SELECT username, message, date_posted FROM messages";
    if (keyword != null && !keyword.trim().isEmpty()) {
        sql += " WHERE message LIKE ?";
    }

    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/secureweb", "root", "root");
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            stmt.setString(1, "%" + keyword + "%");
        }
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String dbUsername = rs.getString("username");
            String message = rs.getString("message");
            String timestamp = rs.getString("date_posted");
%>
<ul>
    <li><strong><%= dbUsername %></strong>: <%= message %> <em>(<%= timestamp %>)</em></li>
</ul>
<%
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
%>

<%-- Display message form only if user is logged in --%>
<%
    String username = (String) session.getAttribute("username");
    if (username != null) {
%>
<h3>Post a Message</h3>
<form method="post" action="messageboard">
    <textarea name="message" rows="4" cols="50" required></textarea><br>
    <button type="submit">Post</button>
</form>
<%
    }
%>

<%-- Display logout form only if the user is logged in --%>
<%
    if (username != null) {
%>
<p>Welcome, <%= username %> |
<form action="logout" method="get" style="display:inline;">
    <button type="submit">Logout</button>
</form>
<%
    }
%>

</body>
</html>
