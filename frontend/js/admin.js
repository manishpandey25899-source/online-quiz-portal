// ==========================================
// ADMIN ACCESS CHECK
// ==========================================

let userRole = localStorage.getItem("userRole");

if (userRole !== "admin") {

    alert("Access Denied. Admin only.");

    window.location.href = "dashboard.html";
}


// Store all loaded questions
let allQuestions = [];


// ==========================================
// LOAD ADMIN STATISTICS
// ==========================================

async function loadAdminData() {

    try {

        let userId =
            localStorage.getItem("userId");


        let response =
            await fetch(
                "http://localhost:8080/api/admin/stats?userId=" +
                userId
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load admin statistics"
            );
        }


        let data =
            await response.json();


        document.getElementById(
            "totalUsers"
        ).innerText =
            data.users;


        document.getElementById(
            "totalQuestions"
        ).innerText =
            data.questions;


        document.getElementById(
            "totalAttempts"
        ).innerText =
            data.attempts;


    } catch (error) {

        console.log(
            "Admin Stats Error:",
            error
        );

    }
}


// ==========================================
// LOAD ALL QUESTIONS
// ==========================================

async function loadQuestions() {

    try {

        let response =
            await fetch(
                "http://localhost:8080/api/questions"
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load questions"
            );
        }


        let questions =
            await response.json();


        allQuestions =
            questions;


        let container =
            document.getElementById(
                "questionList"
            );


        container.innerHTML = "";


        questions.forEach(question => {

            container.innerHTML += `

                <div style="
                    border:1px solid #ccc;
                    padding:10px;
                    margin:10px;
                    text-align:left;
                ">

                    <p>
                        <b>ID:</b>
                        ${question.id}
                    </p>

                    <p>
                        <b>Question:</b>
                        ${question.questionText}
                    </p>


                    <button
                        onclick="editQuestion(${question.id})"
                        class="login-btn"
                        style="margin-bottom:10px;">

                        Edit

                    </button>


                    <button
                        onclick="deleteQuestion(${question.id})"
                        class="logout-btn">

                        Delete

                    </button>

                </div>

            `;

        });


    } catch (error) {

        console.log(
            "Questions Load Error:",
            error
        );

    }
}


// ==========================================
// EDIT QUESTION
// ==========================================

async function editQuestion(id) {

    let question =
        allQuestions.find(
            item => item.id === id
        );


    if (!question) {

        alert(
            "Question data not found."
        );

        return;
    }


    let newQuestionText =
        prompt(
            "Enter Question:",
            question.questionText
        );


    if (newQuestionText === null) {
        return;
    }


    let newOptionA =
        prompt(
            "Enter Option A:",
            question.optionA
        );


    if (newOptionA === null) {
        return;
    }


    let newOptionB =
        prompt(
            "Enter Option B:",
            question.optionB
        );


    if (newOptionB === null) {
        return;
    }


    let newOptionC =
        prompt(
            "Enter Option C:",
            question.optionC
        );


    if (newOptionC === null) {
        return;
    }


    let newOptionD =
        prompt(
            "Enter Option D:",
            question.optionD
        );


    if (newOptionD === null) {
        return;
    }


    let newCorrectAnswer =
        prompt(
            "Enter Correct Answer:",
            question.correctAnswer
        );


    if (newCorrectAnswer === null) {
        return;
    }


    let currentCategoryId =
        question.category &&
        question.category.id
            ? question.category.id
            : "";


    let newCategoryId =
        prompt(
            "Enter Category ID (1-11):",
            currentCategoryId
        );


    if (newCategoryId === null) {
        return;
    }


    newCategoryId =
        parseInt(newCategoryId);


    if (
        Number.isNaN(newCategoryId) ||
        newCategoryId < 1 ||
        newCategoryId > 11
    ) {

        alert(
            "Please enter a valid Category ID from 1 to 11."
        );

        return;
    }


    let userId =
        localStorage.getItem(
            "userId"
        );


    let updatedQuestion = {

        questionText:
            newQuestionText,

        optionA:
            newOptionA,

        optionB:
            newOptionB,

        optionC:
            newOptionC,

        optionD:
            newOptionD,

        correctAnswer:
            newCorrectAnswer,

        category: {

            id:
                newCategoryId
        }
    };


    try {

        let response =
            await fetch(
                "http://localhost:8080/api/questions/" +
                id +
                "?userId=" +
                userId,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            updatedQuestion
                        )
                }
            );


        if (response.ok) {

            alert(
                "Question Updated Successfully"
            );


            loadQuestions();

            loadAdminData();

        } else {

            let errorMessage =
                await response.text();


            alert(
                errorMessage ||
                "Failed To Update Question"
            );

        }


    } catch (error) {

        console.log(
            "Edit Question Error:",
            error
        );


        alert(
            "Edit Question Error"
        );

    }
}


// ==========================================
// DELETE QUESTION
// ==========================================

async function deleteQuestion(id) {

    if (
        !confirm(
            "Delete Question?"
        )
    ) {

        return;
    }


    let userId =
        localStorage.getItem(
            "userId"
        );


    try {

        let response =
            await fetch(
                "http://localhost:8080/api/questions/" +
                id +
                "?userId=" +
                userId,
                {
                    method: "DELETE"
                }
            );


        if (response.ok) {

            alert(
                "Question Deleted"
            );


            loadQuestions();

            loadAdminData();

        } else {

            let errorMessage =
                await response.text();


            alert(
                errorMessage ||
                "Failed To Delete Question"
            );

        }


    } catch (error) {

        console.log(
            "Delete Question Error:",
            error
        );


        alert(
            "Delete Question Error"
        );

    }
}


// ==========================================
// LOAD ALL QUIZ ATTEMPTS
// ==========================================

async function loadAttempts() {

    try {

        let userId =
            localStorage.getItem(
                "userId"
            );


        let response =
            await fetch(
                "http://localhost:8080/api/attempts/admin?userId=" +
                userId
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load quiz attempts"
            );
        }


        let attempts =
            await response.json();


        let container =
            document.getElementById(
                "attemptList"
            );


        container.innerHTML = "";


        if (
            !Array.isArray(attempts) ||
            attempts.length === 0
        ) {

            container.innerHTML =
                "<p>No quiz attempts found.</p>";

            return;
        }


        let tableHTML = `

            <div style="
                overflow-x:auto;
                margin-top:15px;
            ">

                <table style="
                    width:100%;
                    border-collapse:collapse;
                    text-align:center;
                    min-width:850px;
                ">

                    <thead>

                        <tr style="
                            background:#4facfe;
                            color:white;
                        ">

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Attempt ID
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                User ID
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Category ID
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Score
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Total Questions
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Start Time
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                End Time
                            </th>

                        </tr>

                    </thead>

                    <tbody>
        `;


        attempts.forEach(attempt => {

            tableHTML += `

                <tr>

                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${attempt.id}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${attempt.userId}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${attempt.categoryId}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${attempt.score}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${attempt.totalQuestions}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${attempt.startTime || "-"}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${attempt.endTime || "-"}
                    </td>

                </tr>

            `;

        });


        tableHTML += `

                    </tbody>

                </table>

            </div>

        `;


        container.innerHTML =
            tableHTML;


    } catch (error) {

        console.log(
            "Attempts Load Error:",
            error
        );


        let container =
            document.getElementById(
                "attemptList"
            );


        container.innerHTML =
            "<p>Unable to load quiz attempts.</p>";

    }
}


// ==========================================
// CHANGE USER ROLE
// ==========================================

async function changeUserRole(
    targetUserId,
    currentRole,
    userName
) {

    let currentUserId =
        localStorage.getItem(
            "userId"
        );


    // Prevent changing own role
    if (
        String(targetUserId) ===
        String(currentUserId)
    ) {

        alert(
            "You cannot change your own admin role."
        );

        return;
    }


    currentRole =
        String(
            currentRole || "student"
        ).toLowerCase();


    let newRole;


    if (currentRole === "student") {

        newRole = "admin";

    } else if (currentRole === "admin") {

        newRole = "student";

    } else {

        alert(
            "Invalid user role."
        );

        return;
    }


    let confirmation =
        confirm(
            "Change " +
            userName +
            "'s role from " +
            currentRole +
            " to " +
            newRole +
            "?"
        );


    if (!confirmation) {
        return;
    }


    try {

        let response =
            await fetch(
                "http://localhost:8080/api/admin/users/" +
                targetUserId +
                "/role?userId=" +
                currentUserId +
                "&role=" +
                encodeURIComponent(newRole),
                {
                    method: "PUT"
                }
            );


        if (response.ok) {

            alert(
                "User role changed to " +
                newRole +
                " successfully."
            );


            loadUsers();

        } else {

            let errorMessage =
                await response.text();


            alert(
                errorMessage ||
                "Failed To Change User Role"
            );

        }


    } catch (error) {

        console.log(
            "Change User Role Error:",
            error
        );


        alert(
            "Change User Role Error"
        );

    }
}


// ==========================================
// LOAD ALL USERS
// ==========================================

async function loadUsers() {

    try {

        let currentUserId =
            localStorage.getItem(
                "userId"
            );


        let response =
            await fetch(
                "http://localhost:8080/api/admin/users?userId=" +
                currentUserId
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load users"
            );
        }


        let users =
            await response.json();


        let container =
            document.getElementById(
                "userList"
            );


        container.innerHTML = "";


        if (
            !Array.isArray(users) ||
            users.length === 0
        ) {

            container.innerHTML =
                "<p>No users found.</p>";

            return;
        }


        let tableHTML = `

            <div style="
                overflow-x:auto;
                margin-top:15px;
            ">

                <table style="
                    width:100%;
                    border-collapse:collapse;
                    text-align:center;
                    min-width:850px;
                ">

                    <thead>

                        <tr style="
                            background:#4facfe;
                            color:white;
                        ">

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                User ID
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Name
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Email
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Role
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Created At
                            </th>

                            <th style="
                                padding:10px;
                                border:1px solid #ddd;
                            ">
                                Action
                            </th>

                        </tr>

                    </thead>

                    <tbody>
        `;


        users.forEach(user => {

            let role =
                String(
                    user.role || "student"
                ).toLowerCase();


            let isCurrentUser =
                String(user.id) ===
                String(currentUserId);


            let actionHTML;


            if (isCurrentUser) {

                actionHTML = `

                    <button
                        disabled
                        style="
                            padding:8px 12px;
                            border:none;
                            border-radius:6px;
                            background:#ccc;
                            color:#555;
                            cursor:not-allowed;
                        ">

                        Current User

                    </button>

                `;

            } else if (role === "student") {

                actionHTML = `

                    <button
                        onclick="changeUserRole(
                            ${user.id},
                            '${role}',
                            '${String(user.name || "").replace(/'/g, "\\'")}'
                        )"
                        class="login-btn">

                        Make Admin

                    </button>

                `;

            } else {

                actionHTML = `

                    <button
                        onclick="changeUserRole(
                            ${user.id},
                            '${role}',
                            '${String(user.name || "").replace(/'/g, "\\'")}'
                        )"
                        class="logout-btn">

                        Make Student

                    </button>

                `;

            }


            tableHTML += `

                <tr>

                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${user.id}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${user.name || "-"}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${user.email || "-"}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${role}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${user.createdAt || "-"}
                    </td>


                    <td style="
                        padding:10px;
                        border:1px solid #ddd;
                    ">
                        ${actionHTML}
                    </td>

                </tr>

            `;

        });


        tableHTML += `

                    </tbody>

                </table>

            </div>

        `;


        container.innerHTML =
            tableHTML;


    } catch (error) {

        console.log(
            "Users Load Error:",
            error
        );


        let container =
            document.getElementById(
                "userList"
            );


        container.innerHTML =
            "<p>Unable to load users.</p>";

    }
}


// ==========================================
// PAGE LOAD
// ==========================================

loadAdminData();

loadQuestions();

loadAttempts();

loadUsers();


// ==========================================
// REFRESH DATA BUTTON
// ==========================================

document
    .getElementById("loadBtn")
    .addEventListener(
        "click",
        function () {

            loadAdminData();

            loadQuestions();

            loadAttempts();

            loadUsers();

        }
    );


// ==========================================
// ADD QUESTION
// ==========================================

document
    .getElementById("addQuestionBtn")
    .addEventListener(
        "click",
        async function () {

            let questionData = {

                questionText:
                    document.getElementById(
                        "questionText"
                    ).value,

                optionA:
                    document.getElementById(
                        "optionA"
                    ).value,

                optionB:
                    document.getElementById(
                        "optionB"
                    ).value,

                optionC:
                    document.getElementById(
                        "optionC"
                    ).value,

                optionD:
                    document.getElementById(
                        "optionD"
                    ).value,

                correctAnswer:
                    document.getElementById(
                        "correctAnswer"
                    ).value,

                category: {

                    id:
                        parseInt(
                            document.getElementById(
                                "categoryId"
                            ).value
                        )

                }

            };


            let userId =
                localStorage.getItem(
                    "userId"
                );


            try {

                let response =
                    await fetch(
                        "http://localhost:8080/api/questions?userId=" +
                        userId,
                        {

                            method: "POST",

                            headers: {

                                "Content-Type":
                                    "application/json"

                            },

                            body:
                                JSON.stringify(
                                    questionData
                                )

                        }
                    );


                if (response.ok) {

                    alert(
                        "Question Added"
                    );


                    document.getElementById(
                        "questionText"
                    ).value = "";


                    document.getElementById(
                        "optionA"
                    ).value = "";


                    document.getElementById(
                        "optionB"
                    ).value = "";


                    document.getElementById(
                        "optionC"
                    ).value = "";


                    document.getElementById(
                        "optionD"
                    ).value = "";


                    document.getElementById(
                        "correctAnswer"
                    ).value = "";


                    document.getElementById(
                        "categoryId"
                    ).value = "";


                    loadQuestions();

                    loadAdminData();

                } else {

                    let errorMessage =
                        await response.text();


                    alert(
                        errorMessage ||
                        "Failed To Add Question"
                    );

                }


            } catch (error) {

                console.log(
                    "Add Question Error:",
                    error
                );


                alert(
                    "Add Question Error"
                );

            }

        }
    );