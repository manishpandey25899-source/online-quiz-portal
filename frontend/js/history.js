let userId = localStorage.getItem("userId");
let authToken = localStorage.getItem("authToken");

if (!userId || !authToken) {
    alert("Please log in to view your quiz history.");
    window.location.href = "login.html";
} else {
    let baseUrl = typeof API_BASE_URL !== "undefined" ? API_BASE_URL : "https://online-quiz-backend-iy84.onrender.com";

    fetch(baseUrl + "/api/attempts/user/" + userId, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
            "Authorization": "Bearer " + authToken
        }
    })
    .then(response => {
        if (response.status === 401 || response.status === 403) {
            alert("Session expired or access denied. Please log in again.");
            window.location.href = "login.html";
            return [];
        }
        return response.json();
    })
    .then(data => {
        let table = document.getElementById("historyTable");
        if (Array.isArray(data)) {
            data.forEach(attempt => {
                table.innerHTML += `
                <tr>
                    <td>${attempt.id}</td>
                    <td>${attempt.score}</td>
                    <td>${attempt.totalQuestions}</td>
                    <td>${attempt.startTime ? attempt.startTime.replace("T", " ").substring(0, 19) : "-"}</td>
                </tr>
                `;
            });
        }
    })
    .catch(error => {
        console.error("History Load Error:", error);
        alert("Failed to load quiz history.");
    });
}

document.getElementById("backBtn").addEventListener("click", function(){
    window.location.href = "dashboard.html";
});
