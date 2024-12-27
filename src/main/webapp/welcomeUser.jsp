<!DOCTYPE html>
<html>

<head>
  <link rel="stylesheet" href="static/css/styles.css">

  <title>Welcome</title>
  <style>
    /* Styling for the profile picture */
    .profile-img {
      width: 236px;          /* Set the width */
      height: 236px;         /* Set the height */
      border-radius: 50%;    /* Make the image round */
      object-fit: cover;     /* Ensure the image maintains its aspect ratio without distortion */
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

<!-- Link to view messages -->
<a href="messageboard">View Messages</a>

<!-- Logout Form -->
<!-- Logout Form using GET method -->
<form action="logout" method="get">
  <button type="submit">Logout</button>
</form>
<h2>Test Connectivity (Secure)</h2>
<form action="connectionTester" method="post">
  <label for="url">Enter a URL (whitelisted domains only):</label>
  <input type="text" id="url" name="url" placeholder="http://example.com" required>
  <button type="submit">Test</button>
</form>

</body>
</html>
