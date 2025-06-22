
function menuToggle() {
    const menu = document.querySelector(".menu");
    menu.classList.toggle("active");
}

window.addEventListener("click", function(e) {
    const profile = document.querySelector(".profile");
    const menu = document.querySelector(".menu");
    if (!profile.contains(e.target) && !menu.contains(e.target)) {
        menu.classList.remove("active");
    }
});

function confirmLogout(event) {
    event.preventDefault();
    if (confirm('Are you sure you want to log out?')) {
       
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/logout';
        
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        
        if (csrfToken) {
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);
        }
        
        document.body.appendChild(form);
        form.submit();
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const path = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('href') === path) {
            link.classList.add('active');
        }
    });
});


document.addEventListener("DOMContentLoaded", function() {
  
    const notifIcon = document.getElementById("notifIcon");
    const notifPopup = document.getElementById("notifPopup");
    const closeNotif = document.getElementById("closeNotif");
    const filterButtons = document.querySelectorAll(".filter-btn");
    const notifications = document.querySelectorAll(".notif-item");

    if (notifIcon && notifPopup) {
        notifIcon.addEventListener("click", () => {
            notifPopup.style.display = "block";
        });

        if (closeNotif) {
            closeNotif.addEventListener("click", () => {
                notifPopup.style.display = "none";
            });
        }

        window.addEventListener("click", (e) => {
            if (e.target === notifPopup) {
                notifPopup.style.display = "none";
            }
        });
    }

    if (filterButtons.length > 0 && notifications.length > 0) {
        filterButtons.forEach(button => {
            button.addEventListener("click", function() {
                filterButtons.forEach(btn => btn.classList.remove("active"));
                this.classList.add("active");

                let filter = this.getAttribute("data-filter");

                notifications.forEach(notif => {
                    if (filter === "all") {
                        notif.style.display = "block";
                    } else if (filter === "unread" && notif.classList.contains("unread")) {
                        notif.style.display = "block";
                    } else if (filter === "read" && notif.classList.contains("read")) {
                        notif.style.display = "block";
                    } else {
                        notif.style.display = "none";
                    }
                });
            });
        });
    }
});
