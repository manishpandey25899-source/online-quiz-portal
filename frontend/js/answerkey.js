let questions =
JSON.parse(localStorage.getItem("quizQuestions")) || [];

let userAnswers =
JSON.parse(localStorage.getItem("userAnswers")) || [];

let container =
document.getElementById("answerKeyContainer");

for (let i = 0; i < questions.length; i++) {

    let userAnswer = userAnswers[i] || "Not Attempted";

    let userAnswerText = userAnswer;

    if (userAnswer === "A")
        userAnswerText = questions[i].optionA;

    else if (userAnswer === "B")
        userAnswerText = questions[i].optionB;

    else if (userAnswer === "C")
        userAnswerText = questions[i].optionC;

    else if (userAnswer === "D")
        userAnswerText = questions[i].optionD;

    let isCorrect =
        userAnswerText === questions[i].correctAnswer;

    container.innerHTML += `
        <div style="
            border:1px solid #ccc;
            padding:10px;
            margin-bottom:10px;
            text-align:left;
        ">

            <h3>Question ${i + 1}</h3>

            <p><b>${questions[i].questionText}</b></p>

            <p>
                Your Answer:
                ${userAnswerText}
            </p>

            <p>
                Correct Answer:
                ${questions[i].correctAnswer}
            </p>

            <p>
                ${isCorrect ? "✅ Correct" : "❌ Wrong"}
            </p>

        </div>
    `;
}

document.getElementById("backBtn")
.addEventListener("click", function () {

    window.location.href = "result.html";

});