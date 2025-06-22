function fetchUsers() {
    fetch('http://localhost:8080/api/users/json')  
        .then(response => response.json())
        .then(data => {
            const tableBody = document.querySelector('.table-body');  
            tableBody.innerHTML = ''; 

            const sortedUsers = data.sort((a, b) => {
                if (a.status === 'INACTIVE' && b.status !== 'INACTIVE') return 1;
                if (a.status !== 'INACTIVE' && b.status === 'INACTIVE') return -1;
                return 0;
            });

            // Update the total users count
            const totalUsersElement = document.querySelector('.tracking-subtitle');
            if (totalUsersElement) {
                totalUsersElement.textContent = totalUsersElement.textContent.replace(/\d+/, sortedUsers.length);
            }

            sortedUsers.forEach(user => {
                const row = document.createElement('div');
                row.classList.add('table-row');
                
                if (user.status === 'INACTIVE') {
                    row.classList.add('inactive-user');
                }

                row.innerHTML = `
                    <span class="select-col"><input type="checkbox" class="row-checkbox"></span>
                    <span class="id-col">${user.matricule}</span>
                    <span class="last-name-col">${user.lastName}</span>
                    <span class="first-name-col">${user.firstName}</span>
                   <span class="role-col">${user.role?.roleName ?? ''}</span>
                   <span class="actions-col">${getActionButtons(user.matricule, user.status)}</span>
                 `;

                tableBody.appendChild(row);
            });
        })
        .catch(error => console.error('Error fetching contracts:', error));
}

function getActionButtons(matricule, status) {
    let showEdit = true;
    let showView = true;

    return `
        <div class="contract-actions">
            ${showView ? `<button class="action-btn view-btn" data-matricule="${matricule}" title="View"><i class="fas fa-plus"></i></button>` : ''}
            ${showEdit ? `<button class="action-btn edit-btn" data-matricule="${matricule}" title="Edit"><i class="fas fa-pen"></i></button>` : ''}
            ${
                status === 'ACTIVE'
                    ? `<button class="action-btn delete-btn" title="Deactivate"><i class="fas fa-user-slash"></i></button>`
                    : `<button class="action-btn reactivate-btn" title="Reactivate"><i class="fas fa-user-check"></i></button>`
            }
        </div>
    `;
}


document.addEventListener('DOMContentLoaded', () => {
    fetchUsers();
    
    const path = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('href') === path) {
            link.classList.add('active');
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const selectAll = document.getElementById('everything-checkbox');

    if (selectAll) {
        selectAll.addEventListener('change', function () {
            const allCheckboxes = document.querySelectorAll('.row-checkbox');
            allCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAll.checked;
            });
        });
    }
});

document.getElementById('search').addEventListener('input', function () {
    const searchValue = this.value.toLowerCase();
    document.querySelectorAll('.table-body .table-row').forEach(row => {
        const id = row.querySelector('.id-col')?.innerText.toLowerCase() || '';
        const firstName = row.querySelector('.first-name-col')?.innerText.toLowerCase() || '';
        const lastName = row.querySelector('.last-name-col')?.innerText.toLowerCase() || '';
        if (id.includes(searchValue) || firstName.includes(searchValue) || lastName.includes(searchValue)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
});

document.addEventListener('click', function(event) {
    if (event.target.closest('.view-btn')) {
        const matricule = event.target.closest('.view-btn').getAttribute('data-matricule');
        if (matricule) {
            window.location.href = `/api/users/${matricule}`;
        }
    }
});

document.addEventListener('click', function(event) {
    const editBtn = event.target.closest('.edit-btn');
    if (editBtn) {
        const matricule = editBtn.getAttribute('data-matricule');
        console.log('Edit button clicked for matricule:', matricule); 
        if (matricule) {
    
            window.location.href = `/api/users/${matricule}?edit=true`;
        }
    }
});


document.addEventListener('click', function(event) {
    if (event.target.closest('.delete-btn')) {
        const row = event.target.closest('.table-row');
        const matricule = row.querySelector('.id-col').textContent;
        const firstName = row.querySelector('.first-name-col').textContent;
        const lastName = row.querySelector('.last-name-col').textContent;
        const role = row.querySelector('.role-col').textContent;

        document.getElementById('deactivateMatricule').textContent = matricule;
        document.getElementById('deactivateName').textContent = `${firstName} ${lastName}`;
        document.getElementById('deactivateRole').textContent = role;

        const form = document.getElementById('deactivateForm');
        form.action = `/api/users/${matricule}/deactivate`;

        const modal = document.getElementById('deactivateModal');
        modal.style.display = 'block';
    }
});

document.querySelector('#deactivateModal .close').addEventListener('click', () => {
    document.getElementById('deactivateModal').style.display = 'none';
});

document.getElementById('cancelDeactivate').addEventListener('click', () => {
    document.getElementById('deactivateModal').style.display = 'none';
});

window.addEventListener('click', (event) => {
    const modal = document.getElementById('deactivateModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
});
document.addEventListener('click', function(event) {
    const reactivateBtn = event.target.closest('.reactivate-btn');
    if (reactivateBtn) {
        const row = reactivateBtn.closest('.table-row');
        const matricule = row.querySelector('.id-col').textContent;
        const firstName = row.querySelector('.first-name-col').textContent;
        const lastName = row.querySelector('.last-name-col').textContent;
        const role = row.querySelector('.role-col').textContent;

        document.getElementById('reactivateMatricule').textContent = matricule;
        document.getElementById('reactivateName').textContent = `${firstName} ${lastName}`;
        document.getElementById('reactivateRole').textContent = role;

        const form = document.getElementById('reactivateForm');
        form.action = `/api/users/${matricule}/reactivate`;

        const modal = document.getElementById('reactivateModal');
        modal.style.display = 'block';
    }
});
document.querySelector('#reactivateModal .close').addEventListener('click', () => {
    document.getElementById('reactivateModal').style.display = 'none';
});

document.getElementById('cancelReactivate').addEventListener('click', () => {
    document.getElementById('reactivateModal').style.display = 'none';
});

window.addEventListener('click', (event) => {
    const modal = document.getElementById('reactivateModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
});

function showToast(message, type) {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = document.createElement('i');
    icon.className = `toast-icon fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}`;
    
    const content = document.createElement('div');
    content.className = 'toast-content';
    content.textContent = message;
    
    toast.appendChild(icon);
    toast.appendChild(content);
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.classList.add('hide');
        setTimeout(() => {
            container.removeChild(toast);
        }, 300);
    }, 3000);
}

document.getElementById('deactivateForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const form = this;
  
    const redirectInput = document.createElement('input');
    redirectInput.type = 'hidden';
    redirectInput.name = 'redirect';
    redirectInput.value = '/userManager';
    form.appendChild(redirectInput);

    form.submit();
});


document.getElementById('reactivateForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const form = this;
  
    const redirectInput = document.createElement('input');
    redirectInput.type = 'hidden';
    redirectInput.name = 'redirect';
    redirectInput.value = '/userManager';
    form.appendChild(redirectInput);
    form.submit();
});


function showMessage(message, type) {
    let messageDiv = document.getElementById('messageDiv');
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.id = 'messageDiv';
        document.querySelector('.tracking-container').appendChild(messageDiv);
    }

    messageDiv.textContent = message;
    messageDiv.className = `message ${type}`;
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 3000);
}

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
document.querySelector('.contract-action').addEventListener('change', function(e) {
    const selectedRole = e.target.value;
    const tableRows = document.querySelectorAll('.table-row');
    
    tableRows.forEach(row => {
        const roleCell = row.querySelector('.role-col');
        const currentRole = roleCell.textContent.trim();
        
        if (!selectedRole || selectedRole === 'all roles') {
            row.style.display = '';
            return;
        }
        
        if (currentRole === selectedRole) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
});

document.querySelector('.refresh-btn').addEventListener('click', function() {
    document.querySelector('.contract-action').selectedIndex = 0;
    
    const tableRows = document.querySelectorAll('.table-row');
    tableRows.forEach(row => {
        row.style.display = '';
    });

    // Refresh the users list to update the count
    fetchUsers();
});
