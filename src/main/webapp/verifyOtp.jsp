<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Verify OTP</title>
    <link rel="stylesheet" href="static/css/styles.css">
</head>
<body>
<h2>Enter OTP</h2>
<form action="verifyOtp" method="POST">
    <label for="otp">OTP:</label>
    <input type="text" id="otp" name="otp" required>
    <button type="submit">Verify</button>
</form>
<form action="resendOtp" method="POST">
    <button type="submit">Resend OTP</button>
</form>
</body>
</html>
