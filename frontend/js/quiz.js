let questions = [];
let currentQuestion = 0;
let userAnswers = [];

let quizSubmitted = false;
let timer = null;


// ==========================================
// QUIZ SESSION
// ==========================================

let quizSessionId =
    localStorage.getItem("quizSessionId");


if (!quizSessionId) {

    alert(
        "Invalid quiz session. Please start the quiz again."
    );

    window.location.href =
        "dashboard.html";
}


// ==========================================
// QUIZ START TIME
// ==========================================

let quizStartTime =
    localStorage.getItem("quizStartTime");


if (!quizStartTime) {

    quizStartTime =
        new Date().toISOString();

    localStorage.setItem(
        "quizStartTime",
        quizStartTime
    );
}


// ==========================================
// RESTORE ANSWERS
// ==========================================

let savedAnswers =
    localStorage.getItem("quizLiveAnswers");


if (savedAnswers) {

    try {

        userAnswers =
            JSON.parse(savedAnswers);

    } catch (error) {

        userAnswers = [];

    }
}


// ==========================================
// LOAD QUESTIONS
// ==========================================

function loadQuestions() {

    let storedQuestions =
        localStorage.getItem("aiQuestions");


    if (!storedQuestions) {

        alert(
            "No Questions Found."
        );

        window.location.href =
            "dashboard.html";

        return false;
    }


    try {

        questions =
            JSON.parse(storedQuestions);


        if (
            !Array.isArray(questions) ||
            questions.length === 0
        ) {

            alert(
                "No Questions Found."
            );

            window.location.href =
                "dashboard.html";

            return false;
        }


        console.log(
            "Questions Loaded:",
            questions
        );


        return true;

    } catch (error) {

        console.log(
            "Question Parse Error:",
            error
        );

        alert(
            "Questions Parse Error."
        );

        window.location.href =
            "dashboard.html";

        return false;
    }
}


// ==========================================
// LOAD CURRENT QUESTION
// ==========================================

function loadQuestion() {

    if (questions.length === 0) {
        return;
    }


    document.getElementById(
        "questionNumber"
    ).innerText =
        `Question ${currentQuestion + 1} of ${questions.length}`;


    document.getElementById(
        "questionText"
    ).innerText =
        questions[currentQuestion].questionText;


    document.getElementById(
        "optionA"
    ).innerText =
        questions[currentQuestion].optionA;


    document.getElementById(
        "optionB"
    ).innerText =
        questions[currentQuestion].optionB;


    document.getElementById(
        "optionC"
    ).innerText =
        questions[currentQuestion].optionC;


    document.getElementById(
        "optionD"
    ).innerText =
        questions[currentQuestion].optionD;


    // Clear radio buttons
    document
        .querySelectorAll(
            'input[name="answer"]'
        )
        .forEach(
            radio => {
                radio.checked = false;
            }
        );


    // Restore previous answer
    if (
        userAnswers[currentQuestion]
    ) {

        let previousAnswer =
            document.querySelector(
                `input[value="${userAnswers[currentQuestion]}"]`
            );


        if (previousAnswer) {

            previousAnswer.checked =
                true;

        }
    }


    // Progress
    let progress =
        (
            (currentQuestion + 1) /
            questions.length
        ) * 100;


    document.getElementById(
        "progressBar"
    ).style.width =
        progress + "%";
}


// ==========================================
// SAVE CURRENT ANSWER
// ==========================================

function saveCurrentAnswer() {

    let selected =
        document.querySelector(
            'input[name="answer"]:checked'
        );


    if (selected) {

        userAnswers[currentQuestion] =
            selected.value;

    }


    localStorage.setItem(
        "quizLiveAnswers",
        JSON.stringify(userAnswers)
    );
}


// ==========================================
// CALCULATE SCORE
// ==========================================

function calculateScore() {

    let score = 0;


    for (
        let i = 0;
        i < questions.length;
        i++
    ) {

        let selected =
            userAnswers[i];


        if (!selected) {
            continue;
        }


        let selectedText = "";


        if (selected === "A") {

            selectedText =
                questions[i].optionA;

        }
        else if (selected === "B") {

            selectedText =
                questions[i].optionB;

        }
        else if (selected === "C") {

            selectedText =
                questions[i].optionC;

        }
        else if (selected === "D") {

            selectedText =
                questions[i].optionD;
        }


        console.log(
            "Question",
            i + 1,
            "| Selected:",
            selected,
            "| Selected Text:",
            selectedText,
            "| Correct:",
            questions[i].correctAnswer
        );


        if (
            selectedText
                .trim()
                .toLowerCase() ===
            questions[i]
                .correctAnswer
                .trim()
                .toLowerCase()
        ) {

            score++;

        }
    }


    return score;
}


// ==========================================
// SAVE RESULT LOCALLY
// ==========================================

function saveResultLocally(
    score
) {

    localStorage.setItem(
        "totalQuestions",
        questions.length
    );


    localStorage.setItem(
        "correctAnswers",
        score
    );


    localStorage.setItem(
        "attemptedQuestions",
        userAnswers.filter(
            answer => answer
        ).length
    );


    localStorage.setItem(
        "userAnswers",
        JSON.stringify(userAnswers)
    );


    localStorage.setItem(
        "quizQuestions",
        JSON.stringify(questions)
    );
}


// ==========================================
// CREATE ATTEMPT DATA
// ==========================================

function createAttemptData(
    score
) {

    return {

        userId:
            parseInt(
                localStorage.getItem(
                    "userId"
                )
            ),

        categoryId:
            parseInt(
                localStorage.getItem(
                    "categoryId"
                )
            ),

        score:
            score,

        totalQuestions:
            questions.length,

        startTime:
            quizStartTime,

        endTime:
            new Date().toISOString()
    };
}


// ==========================================
// NORMAL DATABASE SAVE
// ==========================================

async function saveAttempt(
    attemptData
) {

    try {

        let response =
            await fetch(
                "https://online-quiz-backend-iy84.onrender.com/api/attempts/save",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            attemptData
                        )
                }
            );


        if (response.ok) {

            console.log(
                "Attempt saved successfully."
            );


            localStorage.setItem(
                "pendingAttemptStatus",
                "sent"
            );


            localStorage.removeItem(
                "pendingAttemptData"
            );


            return true;
        }


        console.log(
            "Attempt save failed."
        );

        return false;

    } catch (error) {

        console.log(
            "Database save error:",
            error
        );

        return false;
    }
}


// ==========================================
// SUBMIT QUIZ
// ==========================================

async function submitQuiz(
    reason
) {

    if (quizSubmitted) {
        return;
    }


    quizSubmitted = true;


    // Stop timer
    if (timer) {

        clearInterval(timer);

        timer = null;
    }


    // Save current answer
    saveCurrentAnswer();


    // Calculate score
    let score =
        calculateScore();


    // Save result for Result page
    saveResultLocally(score);


    // Create database attempt
    let attemptData =
        createAttemptData(score);


    console.log(
        "Quiz Submitted Because:",
        reason
    );


    // Save pending copy first
    localStorage.setItem(
        "pendingAttemptData",
        JSON.stringify(
            attemptData
        )
    );


    localStorage.setItem(
        "pendingAttemptStatus",
        "pending"
    );


    // Try database save
    if (navigator.onLine) {

        await saveAttempt(
            attemptData
        );

    }


    // No longer a refresh-pending quiz
    localStorage.removeItem(
        "pendingRefreshSessionId"
    );


    // Remove live quiz state
    localStorage.removeItem(
        "quizLiveAnswers"
    );

    localStorage.removeItem(
        "quizStartTime"
    );


    // Open result
    window.location.href =
        "result.html";
}


// ==========================================
// REFRESH / PAGE CLOSE AUTO SUBMIT
// ==========================================

let unloadHandled = false;


function handlePageExit() {

    if (
        quizSubmitted ||
        unloadHandled
    ) {

        return;
    }


    unloadHandled = true;


    // Save current answer
    saveCurrentAnswer();


    // Calculate score
    let score =
        calculateScore();


    // Save all result information
    saveResultLocally(score);


    // Create attempt
    let attemptData =
        createAttemptData(score);


    // Keep attempt for possible retry
    localStorage.setItem(
        "pendingAttemptData",
        JSON.stringify(
            attemptData
        )
    );


    localStorage.setItem(
        "pendingAttemptStatus",
        "pending"
    );


    // Mark this exact quiz session
    // as refresh/page-exit submitted
    localStorage.setItem(
        "pendingRefreshSessionId",
        quizSessionId
    );


    // Try Beacon for reliable page-exit save
    if (
        navigator.onLine &&
        navigator.sendBeacon
    ) {

        try {

            let blob =
                new Blob(
                    [
                        JSON.stringify(
                            attemptData
                        )
                    ],
                    {
                        type:
                            "application/json"
                    }
                );


            let beaconSent =
                navigator.sendBeacon(
                    "https://online-quiz-backend-iy84.onrender.com/api/attempts/save",
                    blob
                );


            if (beaconSent) {

                localStorage.setItem(
                    "pendingAttemptStatus",
                    "sent"
                );

            }

        } catch (error) {

            console.log(
                "Beacon Error:",
                error
            );

        }

    }
}


// Page refresh / close
window.addEventListener(
    "pagehide",
    handlePageExit
);


window.addEventListener(
    "beforeunload",
    handlePageExit
);


// ==========================================
// TAB CHANGE
// ==========================================

document.addEventListener(
    "visibilitychange",
    function () {

        if (
            document.hidden &&
            !quizSubmitted
        ) {

            submitQuiz(
                "tab-change"
            );

        }
    }
);


// ==========================================
// INTERNET DISCONNECT
// ==========================================

window.addEventListener(
    "offline",
    function () {

        if (!quizSubmitted) {

            submitQuiz(
                "internet-disconnected"
            );

        }
    }
);


// ==========================================
// NEXT BUTTON
// ==========================================

document
    .getElementById("nextBtn")
    .addEventListener(
        "click",
        function () {

            saveCurrentAnswer();


            if (
                currentQuestion <
                questions.length - 1
            ) {

                currentQuestion++;

                loadQuestion();
            }
        }
    );


// ==========================================
// PREVIOUS BUTTON
// ==========================================

document
    .getElementById("prevBtn")
    .addEventListener(
        "click",
        function () {

            saveCurrentAnswer();


            if (
                currentQuestion > 0
            ) {

                currentQuestion--;

                loadQuestion();
            }
        }
    );


// ==========================================
// SUBMIT BUTTON
// ==========================================

document
    .getElementById("submitBtn")
    .addEventListener(
        "click",
        function () {

            submitQuiz(
                "manual-submit"
            );

        }
    );


// ==========================================
// TIMER
// ==========================================

function startTimer() {

    let timeLeft =
        15 * 60;


    timer =
        setInterval(
            function () {

                let minutes =
                    Math.floor(
                        timeLeft / 60
                    );


                let seconds =
                    timeLeft % 60;


                document.getElementById(
                    "timer"
                ).innerText =
                    `${minutes}:${seconds < 10 ? "0" : ""}${seconds}`;


                timeLeft--;


                if (
                    timeLeft < 0
                ) {

                    clearInterval(timer);

                    timer = null;


                    submitQuiz(
                        "time-up"
                    );
                }

            },
            1000
        );
}


// ==========================================
// INITIALIZE QUIZ
// ==========================================

function initializeQuiz() {

    let loaded =
        loadQuestions();


    if (!loaded) {
        return;
    }


    // ======================================
    // CHECK REFRESH SUBMISSION
    // ======================================

    let pendingSession =
        localStorage.getItem(
            "pendingRefreshSessionId"
        );


    let navigationEntry =
        performance.getEntriesByType(
            "navigation"
        )[0];


    let navigationType =
        navigationEntry
            ? navigationEntry.type
            : "navigate";


    if (
        pendingSession ===
        quizSessionId &&
        navigationType ===
        "reload"
    ) {

        console.log(
            "Refresh detected for same quiz session."
        );


        // Restore saved answers
        let saved =
            localStorage.getItem(
                "quizLiveAnswers"
            );


        if (saved) {

            try {

                userAnswers =
                    JSON.parse(saved);

            } catch (error) {

                userAnswers = [];

            }
        }


        // Restore score/result
        let savedScore =
            parseInt(
                localStorage.getItem(
                    "correctAnswers"
                )
            );


        if (
            !Number.isNaN(
                savedScore
            )
        ) {

            saveResultLocally(
                savedScore
            );

        }


        // If Beacon could not save,
        // retry database save now
        let pendingData =
            localStorage.getItem(
                "pendingAttemptData"
            );


        let pendingStatus =
            localStorage.getItem(
                "pendingAttemptStatus"
            );


        if (
            pendingData &&
            pendingStatus !== "sent" &&
            navigator.onLine
        ) {

            try {

                let attemptData =
                    JSON.parse(
                        pendingData
                    );


                saveAttempt(
                    attemptData
                );

            } catch (error) {

                console.log(
                    "Refresh retry error:",
                    error
                );
            }
        }


        // Clear only the refresh marker
        localStorage.removeItem(
            "pendingRefreshSessionId"
        );


        // Open result
        window.location.href =
            "result.html";


        return;
    }


    // ======================================
    // NORMAL NEW QUIZ
    // ======================================

    loadQuestion();

    startTimer();
}


// Start quiz
initializeQuiz();