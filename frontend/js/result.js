let totalQuestions =
parseInt(localStorage.getItem("totalQuestions")) || 0;

let correct =
parseInt(localStorage.getItem("correctAnswers")) || 0;

let attempted =
parseInt(localStorage.getItem("attemptedQuestions")) || 0;

let wrong = attempted - correct;

let percentage = 0;

if(totalQuestions > 0) {

    percentage =
    Math.round((correct / totalQuestions) * 100);

}

document.getElementById("totalQuestions").innerText =
totalQuestions;

document.getElementById("attempted").innerText =
attempted;

document.getElementById("correct").innerText =
correct;

document.getElementById("wrong").innerText =
wrong;

document.getElementById("score").innerText =
percentage + "%";

document.getElementById("dashboardBtn").addEventListener("click", function(){

    window.location.href = "dashboard.html";

});

document.getElementById("answerKeyBtn").addEventListener("click", function(){

    window.location.href = "answerkey.html";

});