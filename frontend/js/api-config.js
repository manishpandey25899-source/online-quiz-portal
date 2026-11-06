// ============================================================================
// AI QUIZ PORTAL - GLOBAL API CONFIGURATION
// Automatically selects local backend for development and Render for production
// ============================================================================

const API_BASE_URL = (
    window.location.hostname === "localhost" ||
    window.location.hostname === "127.0.0.1"
)
    ? "http://localhost:8080"
    : "https://online-quiz-backend-iy84.onrender.com";

// Helper function to get authorization headers
function getAuthHeaders(extraHeaders = {}) {
    const token = localStorage.getItem("authToken");
    const headers = {
        "Content-Type": "application/json",
        ...extraHeaders
    };
    if (token) {
        headers["Authorization"] = "Bearer " + token;
    }
    return headers;
}
