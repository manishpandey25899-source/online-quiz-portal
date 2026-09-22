// ==========================================
// PROFESSIONAL NOTIFICATION
// ==========================================

function showNotification(message, type = "error") {

    let oldNotification = document.getElementById("oqNotification");

    if (oldNotification) {
        oldNotification.remove();
    }

    let notification = document.createElement("div");
    notification.id = "oqNotification";

    let isSuccess = type === "success";
    let isWarning = type === "warning";

    let background = isSuccess
        ? "#eefaf4"
        : isWarning
        ? "#fff8e8"
        : "#fff1f1";

    let border = isSuccess
        ? "#cdeedc"
        : isWarning
        ? "#f5dfaa"
        : "#f3cccc";

    let iconBackground = isSuccess
        ? "#2f9e68"
        : isWarning
        ? "#e5a51d"
        : "#e05252";

    let icon = isSuccess
        ? "✓"
        : isWarning
        ? "!"
        : "×";

    notification.innerHTML = `
        <div style="
            position:fixed;
            top:28px;
            left:50%;
            transform:translateX(-50%);
            width:min(420px, calc(100% - 30px));
            background:${background};
            border:1px solid ${border};
            border-radius:16px;
            padding:14px 16px;
            display:flex;
            align-items:center;
            gap:12px;
            box-shadow:0 15px 35px rgba(30,50,80,0.14);
            z-index:99999;
            font-family:Arial, Helvetica, sans-serif;
            animation:oqNotificationIn 0.3s ease;
        ">

            <div style="
                width:38px;
                height:38px;
                min-width:38px;
                border-radius:50%;
                background:${iconBackground};
                color:#ffffff;
                display:flex;
                align-items:center;
                justify-content:center;
                font-size:22px;
                font-weight:800;
            ">
                ${icon}
            </div>

            <div style="
                flex:1;
                color:#24324a;
                font-size:14px;
                font-weight:700;
                line-height:1.45;
            ">
                ${message}
            </div>

            <button
                onclick="document.getElementById('oqNotification').remove()"
                style="
                    border:none;
                    background:transparent;
                    color:#98a2b3;
                    font-size:18px;
                    cursor:pointer;
                    padding:3px 5px;
                    line-height:1;
                ">
                ×
            </button>

        </div>
    `;

    document.body.appendChild(notification);

    setTimeout(function () {

        let currentNotification =
            document.getElementById("oqNotification");

        if (currentNotification) {
            currentNotification.remove();
        }

    }, 3500);
}


// ==========================================
// NOTIFICATION ANIMATION
// ==========================================

let notificationStyle = document.createElement("style");

notificationStyle.innerHTML = `
    @keyframes oqNotificationIn {
        from {
            opacity:0;
            transform:translate(-50%, -15px);
        }

        to {
            opacity:1;
            transform:translate(-50%, 0);
        }
    }
`;

document.head.appendChild(notificationStyle);


// ==========================================
// REGISTER
// ==========================================

document.getElementById("registerForm").addEventListener("submit", async function (event) {

    event.preventDefault();

    let fullName = document.getElementById("fullName").value.trim();
    let email = document.getElementById("email").value.trim();
    let password = document.getElementById("password").value;
    let confirmPassword = document.getElementById("confirmPassword").value;


    // ==========================================
    // NAME VALIDATION
    // ==========================================

    if (fullName === "") {

        showNotification(
            "Please enter your full name",
            "warning"
        );

        return;
    }

    let namePattern = /^[A-Za-z][A-Za-z ]{1,49}$/;

    if (!namePattern.test(fullName)) {

        showNotification(
            "Please enter a valid name using letters and spaces only",
            "warning"
        );

        return;
    }


    // ==========================================
    // EMAIL VALIDATION
    // ==========================================

    if (email === "") {

        showNotification(
            "Please enter your email address",
            "warning"
        );

        return;
    }

    let emailPattern =
        /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    if (!emailPattern.test(email)) {

        showNotification(
            "Please enter a valid email address, for example: name@gmail.com",
            "warning"
        );

        return;
    }


    // ==========================================
    // PASSWORD VALIDATION
    // ==========================================

    if (password === "") {

        showNotification(
            "Please enter a password",
            "warning"
        );

        return;
    }

    if (password.length < 8) {

        showNotification(
            "Password must be at least 8 characters long",
            "warning"
        );

        return;
    }

    let passwordPattern =
        /^(?=.*[A-Za-z])(?=.*\d).{8,64}$/;

    if (!passwordPattern.test(password)) {

        showNotification(
            "Password must contain at least one letter and one number",
            "warning"
        );

        return;
    }


    // ==========================================
    // CONFIRM PASSWORD
    // ==========================================

    if (confirmPassword === "") {

        showNotification(
            "Please confirm your password",
            "warning"
        );

        return;
    }

    if (password !== confirmPassword) {

        showNotification(
            "Passwords do not match",
            "error"
        );

        return;
    }


    // ==========================================
    // USER DATA
    // ==========================================

    let userData = {
        name: fullName,
        email: email,
        password: password
    };


    // ==========================================
    // SERVER REQUEST
    // ==========================================

    try {

        let response = await fetch(
            "https://online-quiz-backend-iy84.onrender.com/api/auth/register",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(userData)
            }
        );


        // ======================================
        // SUCCESS
        // ======================================

        if (response.ok) {

            showNotification(
                "Registration Successful",
                "success"
            );

            setTimeout(function () {

                window.location.href = "login.html";

            }, 900);

            return;
        }


        // ======================================
        // SERVER ERROR MESSAGE
        // ======================================

        let message = await response.text();

        showNotification(
            message || "Registration Failed",
            "error"
        );

    } catch (error) {

        console.log(error);

        showNotification(
            "Server Connection Error",
            "error"
        );
    }

});