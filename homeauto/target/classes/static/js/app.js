/* =====================================================
   HOMEAUTO JAVASCRIPT
   ===================================================== */

/* =====================================================
   VOICE ASSISTANT ENGINE (Web Speech API)
   ===================================================== */

function speakVoice(text) {
    if (!('speechSynthesis' in window)) {
        console.warn("Speech Synthesis support nahi hai");
        return;
    }

    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 1.0;
    utterance.pitch = 1.0;

    // Available English voice dhund kar assign karein
    const voices = window.speechSynthesis.getVoices();
    const enVoice = voices.find(v => v.lang.startsWith('en'));
    if (enVoice) {
        utterance.voice = enVoice;
    }

    window.speechSynthesis.speak(utterance);
}

// Next page load par bolne ke liye save karna
function queueVoiceAndSubmit(form, message) {
    sessionStorage.setItem("pendingVoiceMessage", message);
    form.submit();
}

// Page load hote hi pending voice check aur play karna
document.addEventListener("DOMContentLoaded", function () {
    const pendingMsg = sessionStorage.getItem("pendingVoiceMessage");
    if (pendingMsg) {
        sessionStorage.removeItem("pendingVoiceMessage");
        // Chota delay taaki browser voice engine ready ho jaye
        setTimeout(() => {
            speakVoice(pendingMsg);
        }, 300);
    }
});


/* =====================================================
   MOBILE MENU
   ===================================================== */

const menuBtn =
    document.getElementById("menuBtn");

const navLinks =
    document.querySelector(".nav-links");

if (menuBtn && navLinks) {

    menuBtn.addEventListener(
        "click",
        function () {

            navLinks.classList.toggle("show");

            const icon =
                menuBtn.querySelector("i");

            if (navLinks.classList.contains("show")) {

                icon.classList.remove("fa-bars");
                icon.classList.add("fa-xmark");

            } else {

                icon.classList.remove("fa-xmark");
                icon.classList.add("fa-bars");
            }
        }
    );
}


/* =====================================================
   PASSWORD SHOW / HIDE
   ===================================================== */

function togglePassword(
    inputId,
    button
) {

    const input =
        document.getElementById(inputId);

    if (!input) {
        return;
    }

    const icon =
        button.querySelector("i");

    if (input.type === "password") {

        input.type = "text";

        icon.classList.remove(
            "fa-eye"
        );

        icon.classList.add(
            "fa-eye-slash"
        );

    } else {

        input.type = "password";

        icon.classList.remove(
            "fa-eye-slash"
        );

        icon.classList.add(
            "fa-eye"
        );
    }
}


/* =====================================================
   PASSWORD STRENGTH
   ===================================================== */

const passwordInput =
    document.getElementById(
        "registerPassword"
    );

const strengthBar =
    document.querySelector(
        ".strength-bar span"
    );

const strengthText =
    document.getElementById(
        "strengthText"
    );

if (
    passwordInput &&
    strengthBar &&
    strengthText
) {

    passwordInput.addEventListener(
        "input",
        function () {

            const password =
                passwordInput.value;

            let score = 0;

            if (password.length >= 6) {
                score++;
            }

            if (password.length >= 10) {
                score++;
            }

            if (/[A-Z]/.test(password)) {
                score++;
            }

            if (/[0-9]/.test(password)) {
                score++;
            }

            if (/[^A-Za-z0-9]/.test(password)) {
                score++;
            }

            const width =
                Math.min(score * 20, 100);

            strengthBar.style.width =
                width + "%";


            if (password.length === 0) {

                strengthText.textContent =
                    "Password strength";

            } else if (score <= 2) {

                strengthText.textContent =
                    "Weak password";

            } else if (score <= 3) {

                strengthText.textContent =
                    "Medium password";

            } else {

                strengthText.textContent =
                    "Strong password";
            }
        }
    );
}


/* =====================================================
   BUTTON LOADING
   ===================================================== */

document
    .querySelectorAll("form")
    .forEach(function (form) {

        form.addEventListener(
            "submit",
            function () {

                const button =
                    form.querySelector(
                        "button[type='submit']"
                    );

                if (!button) {
                    return;
                }

                if (button.dataset.loading === "true") {
                    return;
                }

                button.dataset.loading = "true";

                button.disabled = true;

                const original =
                    button.innerHTML;

                button.innerHTML =
                    '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';

                setTimeout(
                    function () {

                        button.disabled =
                            false;

                        button.innerHTML =
                            original;

                        button.dataset.loading =
                            "false";

                    },
                    8000
                );
            }
        );
    });


/* =====================================================
   SCROLL REVEAL
   ===================================================== */

const cards =
    document.querySelectorAll(
        ".feature-card, .dashboard-card, .about-content"
    );

if ("IntersectionObserver" in window) {

    const observer =
        new IntersectionObserver(
            function (entries) {

                entries.forEach(
                    function (entry) {

                        if (
                            entry.isIntersecting
                        ) {

                            entry.target.style.opacity =
                                "1";

                            entry.target.style.transform =
                                "translateY(0)";
                        }
                    }
                );

            },
            {
                threshold: 0.15
            }
        );

    cards.forEach(
        function (card) {

            card.style.opacity = "0";

            card.style.transform =
                "translateY(25px)";

            card.style.transition =
                "opacity 0.7s ease, transform 0.7s ease";

            observer.observe(card);
        }
    );
}