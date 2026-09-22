// ==========================================
// PROFESSIONAL NOTIFICATION
// ==========================================

function showNotification(message, type = "error") {

    // Remove previous notification if present
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

    let iconColor = "#ffffff";

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
                color:${iconColor};
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

    // Auto remove after 3.5 seconds
    setTimeout(function () {

        let currentNotification =
            document.getElementById("oqNotification");

        if (currentNotification) {
            currentNotification.remove();
        }

    }, 3500);
}


// Notification animation
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
// LOGIN
// ==========================================

document.getElementById("loginForm").addEventListener("submit", async function(event) {

    event.preventDefault();

    let email = document.getElementById("email").value.trim();
    let password = document.getElementById("password").value;

    // -------------------------
    // EMPTY FIELD VALIDATION
    // -------------------------

    if (email === "") {
        showNotification("Please enter your email address", "warning");
        return;
    }

    if (password === "") {
        showNotification("Please enter your password", "warning");
        return;
    }

    // -------------------------
    // EMAIL VALIDATION
    // -------------------------

    let emailPattern =
        /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    if (!emailPattern.test(email)) {
        showNotification("Please enter a valid email address", "warning");
        return;
    }

    // -------------------------
    // LOGIN DATA
    // -------------------------

    let loginData = {
        email: email,
        password: password
    };

    try {

        let response = await fetch(
            "https://online-quiz-backend-iy84.onrender.com/api/auth/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(loginData)
            }
        );

        // -------------------------
        // INVALID LOGIN
        // -------------------------

        if (!response.ok) {

            showNotification(
                "Invalid Email or Password",
                "error"
            );

            return;
        }

        // Read response safely
        let responseText = await response.text();

        // Empty response means login failed
        if (
            !responseText ||
            responseText.trim() === "" ||
            responseText.trim() === "null"
        ) {

            showNotification(
                "Invalid Email or Password",
                "error"
            );

            return;
        }

        let user;

        try {

            user = JSON.parse(responseText);

        } catch (error) {

            console.log(error);

            showNotification(
                "Invalid Email or Password",
                "error"
            );

            return;
        }

        // -------------------------
        // SUCCESSFUL LOGIN
        // -------------------------

        if (user && user.id) {

            localStorage.setItem("userId", user.id);
            localStorage.setItem("userName", user.name);
            localStorage.setItem("userRole", user.role);

            showNotification(
                "Login Successful",
                "success"
            );

            setTimeout(function() {

                window.location.href = "dashboard.html";

            }, 900);

        } else {

            showNotification(
                "Invalid Email or Password",
                "error"
            );
        }

    } catch (error) {

        console.log(error);

        showNotification(
            "Server Connection Error",
            "error"
        );
    }

});