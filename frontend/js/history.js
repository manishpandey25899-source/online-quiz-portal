let userId = localStorage.getItem("userId");

fetch("http://localhost:8080/api/attempts/user/" + userId)

.then(response => response.json())

.then(data => {

    let table =
        document.getElementById("historyTable");

    data.forEach(attempt => {

        table.innerHTML += `

        <tr>

            <td>${attempt.id}</td>

            <td>${attempt.score}</td>

            <td>${attempt.totalQuestions}</td>

            <td>${attempt.startTime}</td>

        </tr>

        `;

    });

})

.catch(error => {

    console.log(error);

    alert("History Load Error");

});

document.getElementById("backBtn")
.addEventListener("click", function(){

    window.location.href = "dashboard.html";

});