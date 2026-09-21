// ==========================================
// LOGIN CHECK
// ==========================================

if (!localStorage.getItem("userId")) {
    window.location.href = "login.html";
}


// ==========================================
// SYNC PENDING ATTEMPT
// ==========================================

async function syncPendingAttempt() {

    let pendingAttempt =
        localStorage.getItem("pendingAttemptData");

    let pendingStatus =
        localStorage.getItem("pendingAttemptStatus");

    if (
        !pendingAttempt ||
        pendingStatus === "sent" ||
        !navigator.onLine
    ) {
        return;
    }

    try {

        let attemptData =
            JSON.parse(pendingAttempt);

        let response =
            await fetch(
                "http://localhost:8080/api/attempts/save",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(attemptData)
                }
            );

        if (response.ok) {

            localStorage.setItem(
                "pendingAttemptStatus",
                "sent"
            );

            localStorage.removeItem(
                "pendingAttemptData"
            );

            console.log(
                "Pending attempt synced successfully."
            );
        }

    } catch (error) {

        console.log(
            "Pending attempt sync failed:",
            error
        );

    }
}


// Sync when dashboard opens
syncPendingAttempt();


// Sync when internet comes back
window.addEventListener(
    "online",
    function () {
        syncPendingAttempt();
    }
);


// ==========================================
// START QUIZ
// ==========================================

document
    .getElementById("startQuizBtn")
    .addEventListener(
        "click",
        async function () {

            let category =
                document.getElementById("category").value;


            if (category === "") {

                alert(
                    "Please select a category"
                );

                return;
            }


            let startButton =
                document.getElementById(
                    "startQuizBtn"
                );


            try {

                startButton.innerText =
                    "Loading Quiz...";


                let response =
                    await fetch(
                        "http://localhost:8080/api/questions/category/" +
                        category
                    );


                if (!response.ok) {

                    throw new Error(
                        "Question API Error"
                    );

                }


                let data =
                    await response.json();


                if (
                    !Array.isArray(data) ||
                    data.length === 0
                ) {

                    alert(
                        "No questions found for this category."
                    );

                    startButton.innerText =
                        "Start Quiz";

                    return;
                }


                console.log(
                    "Quiz Questions:",
                    data
                );


                // ==================================
                // CREATE NEW QUIZ SESSION
                // ==================================

                let quizSessionId =
                    Date.now() +
                    "-" +
                    Math.random()
                        .toString(36)
                        .substring(2);


                localStorage.setItem(
                    "quizSessionId",
                    quizSessionId
                );


                // ==================================
                // SAVE QUIZ DATA
                // ==================================

                localStorage.setItem(
                    "categoryId",
                    category
                );


                localStorage.setItem(
                    "aiQuestions",
                    JSON.stringify(data)
                );


                // ==================================
                // RESET OLD QUIZ STATE
                // ==================================

                localStorage.removeItem(
                    "pendingRefreshSessionId"
                );

                localStorage.removeItem(
                    "pendingAttemptData"
                );

                localStorage.removeItem(
                    "pendingAttemptStatus"
                );

                localStorage.removeItem(
                    "quizLiveAnswers"
                );

                localStorage.removeItem(
                    "userAnswers"
                );

                localStorage.removeItem(
                    "quizQuestions"
                );

                localStorage.removeItem(
                    "totalQuestions"
                );

                localStorage.removeItem(
                    "attemptedQuestions"
                );

                localStorage.removeItem(
                    "correctAnswers"
                );


                // ==================================
                // NEW QUIZ START TIME
                // ==================================

                localStorage.setItem(
                    "quizStartTime",
                    new Date().toISOString()
                );


                // ==================================
                // OPEN QUIZ
                // ==================================

                window.location.href =
                    "quiz.html";

            } catch (error) {

                console.log(
                    "Quiz Load Error:",
                    error
                );

                alert(
                    "Quiz Load Error"
                );

                startButton.innerText =
                    "Start Quiz";
            }

        }
    );


// ==========================================
// LOGOUT
// ==========================================

document
    .getElementById("logoutBtn")
    .addEventListener(
        "click",
        function () {

            localStorage.clear();

            window.location.href =
                "login.html";

        }
    );