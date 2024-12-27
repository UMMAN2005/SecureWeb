<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="static/css/styles.css">

    <title>Welcome - Admin</title>
    <style>
        /* Styling for the profile picture */
        .profile-img {
            width: 236px;          /* Set the width */
            height: 236px;         /* Set the height */
            border-radius: 50%;    /* Make the image round */
            object-fit: cover;     /* Ensure the image maintains its aspect ratio without distortion */
        }

        /* Styling for the grid of all user profile pictures */
        .profile-gallery {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
            gap: 15px;
            margin-top: 20px;
        }

        .profile-item {
            text-align: center;
        }

        .profile-item img {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            object-fit: cover;
        }

        .button {
            display: inline-block;
            padding: 10px 20px;
            background-color: #4CAF50; /* Green button color */
            color: white;
            text-align: center;
            text-decoration: none; /* Remove underline */
            border-radius: 5px;
            border: none;
            cursor: pointer;
        }

        /* Styling for delete button */
        .delete-btn {
            background-color: red;
            color: white;
            border: none;
            padding: 5px 10px;
            cursor: pointer;
            border-radius: 5px;
        }

        .delete-btn:hover {
            background-color: darkred;
        }
    </style>
</head>
<body>
<h1>Welcome, <%= request.getAttribute("username") != null ? request.getAttribute("username") : "Unknown User" %>!</h1>

<!-- Profile Picture with applied CSS class for styling -->
<img src="<%= request.getAttribute("profilePicture") != null ? request.getAttribute("profilePicture") : "profile_pics/default.jpg" %>" alt="Profile Picture" class="profile-img"><br>

<!-- Form for uploading a new profile picture -->
<form action="profileAdd" method="post" enctype="multipart/form-data">
    <input type="file" name="profile_picture" accept="image/*" required><br><br>
    <button type="submit">Upload</button>
</form>

<a href="messagePanel.jsp" class="button">View Messages</a>


<!-- Logout Form -->
<form action="logout" method="get">
    <button class="delete-btn" type="submit">Logout</button>
</form>

<h2>Test Connectivity (Secure)</h2>
<form action="connectionTester" method="post">
    <label for="url">Enter a URL (whitelisted domains only):</label>
    <input type="text" id="url" name="url" placeholder="http://example.com" required>
    <button type="submit">Test</button>
</form>

<h2>All User Profile Pictures</h2>
<div class="profile-gallery">
    <%-- Assuming "userProfiles" is a list of objects containing "username" and "profilePicture" URLs --%>
    <%
        List<Map<String, String>> userProfiles = (List<Map<String, String>>) request.getAttribute("userProfiles");
        if (userProfiles != null) {
            for (Map<String, String> userProfile : userProfiles) {
    %>
    <div class="profile-item">
        <img src="<%= userProfile.get("profilePicture") != null ? userProfile.get("profilePicture") : "profile_pics/default.jpg" %>" alt="<%= userProfile.get("username") %>'s Profile Picture">
        <p><%= userProfile.get("username") %></p>
        <form action="profileRemoveServlet" method="post" onsubmit="return confirm('Are you sure you want to delete this profile picture?');">
            <input type="hidden" name="username" value="<%= userProfile.get("username") %>">
            <button type="submit" class="delete-btn">Delete Profile Picture</button>
        </form>
    </div>
    <%
        }
    } else {
    %>
    <p>No user profiles available.</p>
    <% } %>
</div>

</body>
</html>
