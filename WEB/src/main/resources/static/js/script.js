// Function to toggle between login and register forms
function toggleForm() {
    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");

    if (loginForm.style.display === "none") {
        loginForm.style.display = "block";
        registerForm.style.display = "none";
    } else {
        loginForm.style.display = "none";
        registerForm.style.display = "block";
    }
}

// JavaScript for register form validation
document.getElementById("registerFormElement").addEventListener("submit", function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const terms = document.getElementById("terms").checked;

    if (!terms) {
        alert("You must agree to the terms & conditions.");
        return;
    }

    if (password !== confirmPassword) {
        alert("Passwords do not match.");
        return;
    }

    alert("Registration successful!");
    toggleForm(); // After successful registration, show the login form
});

function toggleMenu() {
    document.getElementById("sidebar").style.left = "0";
}

function closeMenu() {
    document.getElementById("sidebar").style.left = "-250px";
}


