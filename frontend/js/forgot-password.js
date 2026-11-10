// ============================================================================
// FORGOT PASSWORD FLOW CONTROLLER
// Handles 3-Step Password Recovery with Email OTP & Reset Token
// ============================================================================

(function () {
    let currentEmail = "";
    let activeResetToken = "";
    let countdownInterval = null;

    // DOM Elements
    const step1Form = document.getElementById("step1Form");
    const step2Form = document.getElementById("step2Form");
    const step3Form = document.getElementById("step3Form");
    const stepSuccess = document.getElementById("stepSuccess");

    const dot1 = document.getElementById("dot1");
    const dot2 = document.getElementById("dot2");
    const dot3 = document.getElementById("dot3");

    const subheadingText = document.getElementById("subheadingText");
    const displayedEmail = document.getElementById("displayedEmail");
    const notificationBox = document.getElementById("notificationBox");
    const countdownTimer = document.getElementById("countdownTimer");
    const resendOtpBtn = document.getElementById("resendOtpBtn");

    const sendOtpBtn = document.getElementById("sendOtpBtn");
    const verifyOtpBtn = document.getElementById("verifyOtpBtn");
    const resetPasswordBtn = document.getElementById("resetPasswordBtn");

    // Notification Helper
    function showNotification(message, type = "error") {
        notificationBox.className = "notification-toast " + type;
        notificationBox.innerText = message;
        notificationBox.style.display = "flex";
        notificationBox.scrollIntoView({ behavior: "smooth", block: "nearest" });
    }

    function clearNotification() {
        notificationBox.style.display = "none";
        notificationBox.innerText = "";
    }

    // Toggle Password Visibility
    function setupPasswordToggle(inputId, toggleBtnId) {
        const input = document.getElementById(inputId);
        const btn = document.getElementById(toggleBtnId);
        if (!input || !btn) return;

        btn.addEventListener("click", () => {
            if (input.type === "password") {
                input.type = "text";
                btn.innerText = "🙈";
            } else {
                input.type = "password";
                btn.innerText = "👁️";
            }
        });
    }

    setupPasswordToggle("newPassword", "toggleNewPassword");
    setupPasswordToggle("confirmNewPassword", "toggleConfirmPassword");

    // Start 15-Minute Countdown
    function startTimer(durationSeconds) {
        clearInterval(countdownInterval);
        let remaining = durationSeconds;
        countdownTimer.style.color = "#667085";

        function updateDisplay() {
            const minutes = Math.floor(remaining / 60);
            const seconds = remaining % 60;
            countdownTimer.innerText = `OTP expires in ${minutes.toString().padStart(2, "0")}:${seconds.toString().padStart(2, "0")}`;

            if (remaining <= 0) {
                clearInterval(countdownInterval);
                countdownTimer.innerText = "OTP expired. Please resend.";
                countdownTimer.style.color = "#d32f2f";
            }
            remaining--;
        }

        updateDisplay();
        countdownInterval = setInterval(updateDisplay, 1000);
    }

    // ------------------------------------------------------------------------
    // STEP 1: SUBMIT EMAIL FOR OTP
    // ------------------------------------------------------------------------
    step1Form.addEventListener("submit", async function (e) {
        e.preventDefault();
        clearNotification();

        const email = document.getElementById("resetEmail").value.trim().toLowerCase();
        const emailRegex = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

        if (!email || !emailRegex.test(email)) {
            showNotification("Please enter a valid email address.", "error");
            return;
        }

        currentEmail = email;
        sendOtpBtn.classList.add("btn-loading");
        sendOtpBtn.innerText = "Sending OTP...";

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/forgot-password`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: currentEmail })
            });

            const data = await response.json();

            if (response.ok) {
                // Transition to Step 2
                step1Form.style.display = "none";
                step2Form.style.display = "block";

                dot1.className = "step-dot completed";
                dot2.className = "step-dot active";

                subheadingText.innerText = "Enter the 6-digit code sent to your email address.";
                displayedEmail.innerText = currentEmail;

                showNotification("OTP sent successfully! Please check your inbox or spam folder.", "success");
                startTimer(900); // 15 minutes
            } else {
                showNotification(data.message || "Failed to send OTP. Please try again.", "error");
            }
        } catch (err) {
            console.error("Forgot password error:", err);
            showNotification("Network or server connection error. Please try again.", "error");
        } finally {
            sendOtpBtn.classList.remove("btn-loading");
            sendOtpBtn.innerText = "Send Verification OTP";
        }
    });

    // Resend OTP
    resendOtpBtn.addEventListener("click", async function () {
        if (!currentEmail) return;
        clearNotification();
        resendOtpBtn.disabled = true;
        resendOtpBtn.innerText = "Resending...";

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/forgot-password`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: currentEmail })
            });

            const data = await response.json();

            if (response.ok) {
                showNotification("A new OTP has been sent to your email.", "success");
                startTimer(900); // 15 minutes
            } else {
                showNotification(data.message || "Failed to resend OTP.", "error");
            }
        } catch (err) {
            showNotification("Failed to resend OTP. Please check your network.", "error");
        } finally {
            resendOtpBtn.disabled = false;
            resendOtpBtn.innerText = "Resend OTP";
        }
    });

    // ------------------------------------------------------------------------
    // STEP 2: VERIFY OTP
    // ------------------------------------------------------------------------
    step2Form.addEventListener("submit", async function (e) {
        e.preventDefault();
        clearNotification();

        const otp = document.getElementById("otpCode").value.trim();

        if (!otp || !/^[0-9]{6}$/.test(otp)) {
            showNotification("Please enter a valid 6-digit numeric OTP.", "error");
            return;
        }

        verifyOtpBtn.classList.add("btn-loading");
        verifyOtpBtn.innerText = "Verifying...";

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/verify-otp`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    email: currentEmail,
                    otp: otp
                })
            });

            const data = await response.json();

            if (response.ok && data.resetToken) {
                clearInterval(countdownInterval);
                activeResetToken = data.resetToken;

                // Transition to Step 3
                step2Form.style.display = "none";
                step3Form.style.display = "block";

                dot2.className = "step-dot completed";
                dot3.className = "step-dot active";

                subheadingText.innerText = "Create a strong new password for your account.";
                showNotification("OTP verified successfully! Please choose a new password.", "success");
            } else {
                showNotification(data.message || "Invalid or expired OTP. Please try again.", "error");
            }
        } catch (err) {
            console.error("OTP verification error:", err);
            showNotification("Network or server connection error. Please try again.", "error");
        } finally {
            verifyOtpBtn.classList.remove("btn-loading");
            verifyOtpBtn.innerText = "Verify OTP";
        }
    });

    // ------------------------------------------------------------------------
    // STEP 3: RESET PASSWORD
    // ------------------------------------------------------------------------
    step3Form.addEventListener("submit", async function (e) {
        e.preventDefault();
        clearNotification();

        const newPassword = document.getElementById("newPassword").value;
        const confirmPassword = document.getElementById("confirmNewPassword").value;

        const passwordPattern = /^(?=.*[A-Za-z])(?=.*\d).{8,64}$/;

        if (!newPassword || !passwordPattern.test(newPassword)) {
            showNotification("Password must be at least 8 characters long and contain at least one letter and one number.", "error");
            return;
        }

        if (newPassword !== confirmPassword) {
            showNotification("Passwords do not match. Please re-enter.", "error");
            return;
        }

        resetPasswordBtn.classList.add("btn-loading");
        resetPasswordBtn.innerText = "Updating Password...";

        try {
            const response = await fetch(`${API_BASE_URL}/api/auth/reset-password`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    email: currentEmail,
                    resetToken: activeResetToken,
                    newPassword: newPassword
                })
            });

            const data = await response.json();

            if (response.ok) {
                // Success Screen
                step3Form.style.display = "none";
                stepSuccess.style.display = "block";

                dot3.className = "step-dot completed";
                subheadingText.style.display = "none";
                notificationBox.style.display = "none";
            } else {
                showNotification(data.message || "Failed to reset password. Please try again.", "error");
            }
        } catch (err) {
            console.error("Reset password error:", err);
            showNotification("Network or server connection error. Please try again.", "error");
        } finally {
            resetPasswordBtn.classList.remove("btn-loading");
            resetPasswordBtn.innerText = "Update Password";
        }
    });
})();
