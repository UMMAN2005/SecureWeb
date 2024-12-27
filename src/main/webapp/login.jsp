<!DOCTYPE html>
<html>
    <head>
        <link rel="stylesheet" href="static/css/styles.css">
        <link rel="stylesheet" href="static/css/login.css">
        <link rel="stylesheet" href="static/css/eye.css">
    <title>Login</title>
</head>
<body>
<div class="login-form">
    <h1>Login</h1>
    <form action="login" method="POST">
        <label for="username">Username:</label>
        <input type="text" id="username" name="username" required>

        <label for="password">Password:</label>

            <div class="form-group transition-container eye-close">
                <span class="transition-bg"></span>
                                <span class="icon-lock"><svg viewBox="97 6 809 988" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><metadata>http://www.onlinewebfonts.com/icon</metadata><g fill="currentColor"><path d="M321.8,455.5h356.4V321.8c0-49.2-17.4-91.2-52.2-126c-34.8-34.8-76.8-52.2-126-52.2c-49.2,0-91.2,17.4-126,52.2c-34.8,34.8-52.2,76.8-52.2,126L321.8,455.5L321.8,455.5z M900.9,522.3v400.9c0,18.6-6.5,34.3-19.5,47.3c-13,13-28.8,19.5-47.3,19.5H165.9c-18.6,0-34.3-6.5-47.3-19.5c-13-13-19.5-28.8-19.5-47.3V522.3c0-18.6,6.5-34.3,19.5-47.3c13-13,28.8-19.5,47.3-19.5h22.3V321.8c0-85.4,30.6-158.7,91.9-219.9C341.3,40.7,414.7,10,500,10c85.3,0,158.7,30.6,219.9,91.9c61.3,61.3,91.9,134.6,91.9,219.9v133.6h22.3c18.6,0,34.3,6.5,47.3,19.5C894.4,488,900.9,503.7,900.9,522.3L900.9,522.3z"/></g></svg>
                                </span>
                <input type="password" class="form-control-lg form-control-black transition-input"  id="password" name="password" required/>


                <span class="icon-eye middle-right js-transition"><svg viewBox="-20 -200 320 400" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"><style>#eye{--duration-blink: .2s;--duration-lashes: .2s;--delay-lashes: var(--duration-blink);--duration-pupil: .1s;--delay-pupil: calc(var(--duration-blink)*2/3);}#eye-bottom,#eye-top{stroke-linecap:round;}#eye-top,#eye-lashes{transition: var(--duration-blink) ease-in;}#eye-pupil {opacity: 0;transition:opacity var(--duration-pupil) var(--delay-pupil) ease;}.eye-open #eye-top,.eye-open #eye-lashes{transform: rotateX(.5turn);animation:scaleUp var(--duration-lashes) var(--delay-lashes) ease-in-out;}.eye-open #eye-pupil{opacity:1;}.eye-close #eye-lashes{animation:scaleDown var(--duration-lashes) var(--duration-blink) ease-in-out;}.eye-close #eye-pupil{opacity:0;}@keyframes scaleUp {50%{transform:rotateX(.5turn) scaleY(1.15); }to{transform:rotateX(.5turn) scaleY(1);}}@keyframes scaleDown {50%{transform:scaleY(1.15);}to{transform:scaleY(1); }}</style><g id="eye" stroke-width="30" fill="none"><g id="eye-lashes" stroke="currentColor"><line x1="140" x2="140" y1="90" y2="180" /><line x1="70"  x2="10"  y1="60" y2="140" /><line x1="210" x2="270" y1="60" y2="140" /></g><path id="eye-bottom" d="m0,0q140,190 280,0" stroke="currentColor" /><path id="eye-top"    d="m0,0q140,190 280,0" stroke="currentColor" /><circle id="eye-pupil" cx="140" cy="0" r="50" fill="currentColor" stroke="none" /></g></svg>
    </span>
            </div>


        <div class="remember-me">
            <span>Remember Me</span>
            <input type="checkbox" id="rememberMe" name="rememberMe">
            <label for="rememberMe"></label>
        </div>


        <button type="submit">Login</button>
    </form>
</div>
    <script src="static/js/login.js"></script>
</body>
</html>
