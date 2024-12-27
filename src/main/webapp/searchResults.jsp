<%@ page %>
<%
  String username = (String) session.getAttribute("user");
  if (username == null) {
    response.sendRedirect("login.jsp");
  }
%>
<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="static/css/styles.css">

  <title>Search Results</title>
</head>
<body>
<h2>Search Results</h2>
<%-- Dynamically populated results --%>
<div>
  <%
    // Placeholder for messages fetched from the database
  %>
</div>
<a href="messagePanel.jsp">Back to Message Board</a>
</body>
</html>
