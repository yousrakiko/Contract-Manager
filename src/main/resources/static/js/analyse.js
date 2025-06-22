fetch("/analytics/financial-summary")
  .then(res => res.json())
  .then(data => {
  
    const list = document.getElementById("topGuaranteesList");
    list.innerHTML = "";

    data.topGuarantees.forEach(item => {
      const percent = (item.rate * 100).toFixed(2);
      const li = document.createElement("li");
      li.innerHTML = `
        <strong>${item.title}</strong><br>
        <small>${item.contractor}</small>
        <span class="rate">${percent}%</span>
      `;
      list.appendChild(li);
    });
  })
  .catch(err => {
    console.error("Error fetching top contracts:", err);
    document.getElementById("topGuaranteesList").innerHTML = "<li>Error loading data</li>";
  });

 fetch('/api/analytics/awaiting-modification')
  .then(response => response.json())
  .then(count => {
    animateCount("awaiting-modifications-count", count, 500, translations.awaitingModification);
  })
  .catch(err => console.error("Error loading awaiting modifications count:", err));

fetch('/api/analytics/waiting-review')
  .then(response => response.json())
  .then(count => {
    animateCount("awaiting-review-count", count, 500, translations.awaitingReview);
  })
  .catch(err => console.error("Error loading awaiting review count:", err));

fetch('/api/analytics/recent-signed')
  .then(response => response.json())
  .then(count => {
    animateCount("recentSignedContracts", count, 500, translations.signedRecently);
  })
  .catch(err => console.error("Error loading signed contracts count:", err));

function animateCount(elementId, endValue, duration = 500, suffix = "") {
  const el = document.getElementById(elementId);
  let start = 15;

  if (endValue > 15) {
    start = endValue + 5;
  }

  const difference = start - endValue;
  const steps = duration / 16;
  const decrement = difference / steps;

  const counter = setInterval(() => {
    start -= decrement;
    if (start <= endValue) {
      el.innerText = `${endValue} ${suffix}`;
      clearInterval(counter);
    } else {
      el.innerText = `${Math.floor(start)} ${suffix}`;
    }
  }, 16);
}


  fetch("/analytics/expiring-contracts")
  .then(res => res.json())
  .then(data => {
    const list = document.getElementById("expiringContractsList");
    list.innerHTML = ""; 
    data.sort((a, b) => a.daysLeft - b.daysLeft);
    data.forEach(item => {
      const li = document.createElement("li");

      const colorClass = item.daysLeft < 20 ? "expiring-red" : "expiring-green";

      li.innerHTML = `
        <strong>${item.title}</strong><br>
        <small>${item.contractor}</small>
        <span class="${colorClass}">${item.daysLeft} ${translations.daysLeft}</span>
      `;
      list.appendChild(li);
    });
  })
  .catch(err => {
    console.error("Error fetching expiring contracts:", err);
    document.getElementById("expiringContractsList").innerHTML = `<li>${translations.errorLoadingData}</li>`;
  });

fetch("/analytics/contract-type-distribution")
  .then(res => res.json())
  .then(data => {
    const total = Object.values(data).reduce((sum, count) => sum + count, 0);

    const serviceCount = data.service || 0;
    const supplyCount = data.supply || 0;
    const servicePercent = (serviceCount / total) * 100;

   
    const serviceCircle = document.getElementById("serviceProgress");
    const radius = serviceCircle.r.baseVal.value;
    const circumference = 2 * Math.PI * radius;

    serviceCircle.style.strokeDasharray = `${circumference} ${circumference}`;
    const offset = ((100 - servicePercent) / 100) * circumference;
    serviceCircle.style.strokeDashoffset = offset;

   
    document.querySelector(".type-label.service").textContent = `Service (${serviceCount})`;
    document.querySelector(".type-label.supply").textContent = `Supply (${supplyCount})`;
  })
  .catch(err => {
    console.error("Error fetching contract type distribution:", err);
  });




fetch("/analytics/status-trend")
  .then(res => res.json())
  .then(data => {
    const monthLabels = [
      "Jan", "Feb", "Mar", "Apr", "May", "Jun",
      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    ];

    
    const signedCounts = Array(12).fill(0);
    const rejectedCounts = Array(12).fill(0);
    const pendingCounts = Array(12).fill(0);

    for (let status in data) {
      for (let month in data[status]) {
        const index = parseInt(month) - 1;
        const count = data[status][month];

        if (status === "signed") signedCounts[index] = count;
        else if (status === "rejected") rejectedCounts[index] = count;
        else if (status === "pending") pendingCounts[index] = count;
      }
    }

    const ctx = document.getElementById("monthlytrends").getContext("2d");
    new Chart(ctx, {
      type: "line",
      data: {
        labels: monthLabels,
        datasets: [
          {
            label: "Signed",
            data: signedCounts,
            borderColor: "rgb(50, 44, 117)",
            backgroundColor: "rgba(50, 44, 117, 0.2)",
            tension: 0.3
          },
          {
            label: "Rejected",
            data: rejectedCounts,
            borderColor: "rgba(0, 123, 255, 1)",
            backgroundColor: "rgba(0, 123, 255, 0.1)",
            tension: 0.3
          },
          {
            label: "Pending",
            data: pendingCounts,
            borderColor: "rgb(140, 217, 249)",
            backgroundColor: "rgba(140, 217, 249, 0.2)",
            tension: 0.3
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top'
          },
          tooltip: {
            callbacks: {
              label: function(context) {
                return context.dataset.label + ': ' + Math.round(context.raw);
              }
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              stepSize: 1,
              callback: function(value) {
                return Math.round(value);
              }
            }
          }
        }
      }
    });
  })
  .catch(err => {
    console.error("Failed to load monthly trends data:", err);
  });


  const ctx = document.getElementById('guaranteeVsBudgetChart').getContext('2d');
  let budgetVsGuaranteeChart;

const currentMonth = new Date().getMonth() + 1;
const monthNames = {
  "1": "January", "2": "February", "3": "March",
  "4": "April", "5": "May", "6": "June",
  "7": "July", "8": "August", "9": "September",
  "10": "October", "11": "November", "12": "December"
};

const monthOptions = document.querySelector('.month-options');
if (monthOptions) {
  monthOptions.innerHTML = '';
  
  for (let i = 1; i <= currentMonth; i++) {
    const label = document.createElement('label');
    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.value = i;
    
    if (i === currentMonth) {
      checkbox.checked = true;
    }
    
    label.appendChild(checkbox);
    label.appendChild(document.createTextNode(monthNames[i]));
    monthOptions.appendChild(label);
  }
}

const dropdownToggle = document.getElementById('monthFilterButton');
const dropdownMenu = document.querySelector('.month-options');

if (dropdownToggle) {
  dropdownToggle.addEventListener('click', (e) => {
    e.stopPropagation();
    dropdownMenu.classList.toggle('show');
  });
}
document.addEventListener('click', (e) => {
  if (!dropdownMenu.contains(e.target) && !dropdownToggle.contains(e.target)) {
    dropdownMenu.classList.remove('show');
  }
});



const fetchAndRenderChart = () => {
  const selectedMonths = Array.from(document.querySelectorAll('.month-options input:checked'))
    .map(input => parseInt(input.value));
  
  document.getElementById('selectedMonthsLabel').textContent = 
    translations.filteredBy + ': ' + selectedMonths.map(m => monthNames[m]).join(', ');
  
  if (selectedMonths.length === 0) {
    alert(translations.pleaseSelectMonth);
    return;
  }

  const queryParams = selectedMonths.map(m => `months=${m}`).join('&');

  fetch(`/analytics/guarantee-vs-budget?${queryParams}`)
    .then(res => res.json())
    .then(data => {
     
      document.getElementById('selectedMonthsLabel').textContent = 
        translations.filteredBy + ': ' + selectedMonths.map(m => monthNames[m]).join(', ');

      const labels = data.labels;                
      const budgets = data.budgetData;           
      const guarantees = data.guaranteeData;    

      if (budgetVsGuaranteeChart) budgetVsGuaranteeChart.destroy();

      budgetVsGuaranteeChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels,
          datasets: [
            {
              label: 'Budget',
              data: budgets,
              backgroundColor: 'rgb(50, 44, 117)', 
              borderRadius: 6,
            },
            {
              label: 'Guarantee',
              data: guarantees,
              backgroundColor: 'rgb(140, 217, 249)', 
              borderRadius: 6,
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'top' },
            tooltip: { mode: 'index', intersect: false },
          },
          scales: {
          x: { stacked: false, ticks: { autoSkip: false } },
            y: { beginAtZero: true }
          }
        }
      });

      monthOptions.classList.remove('show');
    })
    .catch(err => {
      console.error('Error loading chart data:', err);
      alert(translations.errorLoadingChart);
    });
};

fetchAndRenderChart();

document.getElementById('applyMonthFilter').addEventListener('click', () => {
  monthOptions.classList.remove('show');  
  fetchAndRenderChart();
});

  const path = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === path) {
      link.classList.add('active');
    }
  });
function loadPerformanceSnapshot() {
    fetch('/api/analytics/performance-snapshot')
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch performance snapshot");
            }
            return response.json();
        })
        .then(data => {
            document.getElementById("activeContracts").innerText = `${data.activeContracts}${translations.newActiveContracts}`;
            document.getElementById("signedContracts").innerText = `${data.signedContracts}${translations.newSignedContracts}`;
            document.getElementById("createdContracts").innerText = `${data.createdContracts}${translations.newCreatedContracts}`;
        })
        .catch(error => {
            console.error("Error loading performance snapshot:", error);
        });
}

function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}

document.addEventListener("DOMContentLoaded", function () {
    loadPerformanceSnapshot();
});

fetch('/api/analytics/signature-delay')
  .then(response => response.json())
  .then(data => {
    const ctx = document.getElementById('signatureDelayChart').getContext('2d');

    const monthsOnly = data.map(item => item.month.split(' ')[0]); 
    const delays = data.map(item => item.avg_delay);

    new Chart(ctx, {
      type: 'line',
      data: {
        labels: monthsOnly,
        datasets: [{
          label: translations.avgSignatureDelay,
          data: delays,
          borderColor: 'rgb(50, 44, 117)',
          backgroundColor: 'rgba(50, 44, 117, 0.2)',
          fill: true,
          tension: 0.3
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false, 
        plugins: {
          legend: {
            position: 'top'
          },
          title: {
            display: true,
            text: translations.averageContractSignatureDelay,
            position: 'bottom', 
            font: {
              size: 14,
              weight: 'bold'
            },
            padding: {
              top: 20
            }
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            suggestedMax: Math.max(...delays) + 10, 
            title: {
              display: true,
              text: translations.days
            }
          },
          x: {
            title: {
              display: true,
              text: translations.month
            }
          }
        }
      }
    });
  })
  .catch(err => {
    console.error('Error loading signature delay data:', err);
  });
document.addEventListener("DOMContentLoaded", function () {
  fetch('/api/analytics/latest-actions')
    .then(res => res.json())
    .then(actions => {
      const container = document.querySelector(".card .table-subheader-container").nextElementSibling;
      container.innerHTML = '';
      actions.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));

      actions.forEach(action => {
        const row = document.createElement('div');
        row.className = 'table-row';

        const label = `${action.module}-${action.referenceId}`;

        const date = new Date(action.timestamp);
        const formattedDate = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;

        row.innerHTML = `
          <span class="id-col"><a href="/${action.module}s/details/${action.referenceId}">${label}</a></span>
          <span class="status-col">${action.summary}</span>
          <span class="title-col">${action.userMatricule}</span>
          <span class="date-col">${formattedDate}</span>
        `;

        container.appendChild(row);
      });
    })
    .catch(error => {
      console.error("Error fetching latest actions:", error);
    });
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




