window.onload = function () {
 
    const canvas = document.getElementById('myChart');
    if (!canvas) {
        console.error('Canvas element not found');
        return;
    }
    const ctx = canvas.getContext('2d');

    const approvedMonthlyElement = document.getElementById('approvedMonthly');
    const declinedMonthlyElement = document.getElementById('declinedMonthly');
    
    const approvedMonthly = approvedMonthlyElement ? JSON.parse(approvedMonthlyElement.value || '[]') : [];
    const declinedMonthly = declinedMonthlyElement ? JSON.parse(declinedMonthlyElement.value || '[]') : [];

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
            datasets: [
                {
                    label: 'Approved Contracts',
                    data: approvedMonthly,
                    backgroundColor: 'rgba(50, 44, 117, 0.6)', 
                    borderColor: 'rgba(50, 44, 117, 1)',
                    borderWidth: 1
                },
                {
                    label: 'Declined Contracts',
                    data: declinedMonthly,
                    backgroundColor: 'rgba(255, 99, 132, 0.6)',
                    borderColor: 'rgba(255, 99, 132, 1)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: 'Monthly Contracts Overview',
                    font: {
                        size: 16
                    }
                },
                legend: {
                    position: 'top'
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        stepSize: 1,
                        precision: 0
                    },
                    title: {
                        display: true,
                        text: 'Number of Contracts'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Month'
                    }
                }
            }
        }
    });

    animateCounter('contractCount', 10); 
};


function animateCounter(id, value) {
    let count = 0;
    let element = document.getElementById(id);
    if (!element) return; 

    let interval = setInterval(() => {
        if (count >= value) {
            clearInterval(interval);
            return;
        }
        count++;
        element.innerText = count;
    }, 50);
}

// Notification popup handling
const notifIcon = document.getElementById("notifIcon");
const notifPopup = document.getElementById("notifPopup");
const closeNotif = document.getElementById("closeNotif");

if (notifIcon && notifPopup && closeNotif) {
    notifIcon.addEventListener("click", () => {
        notifPopup.style.display = "block";
    });

    closeNotif.addEventListener("click", () => {
        notifPopup.style.display = "none";
    });

    window.addEventListener("click", (e) => {
        if (e.target === notifPopup) {
            notifPopup.style.display = "none";
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    const filterButtons = document.querySelectorAll(".filter-btn");
    const notifications = document.querySelectorAll(".notif-item");

    filterButtons.forEach(button => {
        button.addEventListener("click", function () {
            // Remove active class from all buttons
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
});

  const path = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === path) {
      link.classList.add('active');
    }
  });
  document.addEventListener("DOMContentLoaded", () => {
    const percent = parseFloat(document.getElementById("signedPercent").value);
    const circle = document.getElementById("circleProgress");
    const label = document.getElementById("percentLabel");

    const radius = circle.r.baseVal.value;
    const circumference = 2 * Math.PI * radius;

    circle.style.strokeDasharray = `${circumference} ${circumference}`;
    circle.style.strokeDashoffset = circumference;

    function setProgress(percent) {
        const offset = circumference - (percent / 100) * circumference;
        circle.style.strokeDashoffset = offset;
        label.innerText = `${Math.round(percent)}%`;
    }

    setProgress(percent);
});

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
