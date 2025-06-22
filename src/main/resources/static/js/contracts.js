let contractsData = [];

function updateContractCount(count) {
    const subtitle = document.querySelector('.tracking-subtitle');
    if (subtitle) {
        subtitle.textContent = `Total ${count} contracts`;
    }
}

function fetchContracts(filters = {}) {
    Promise.all([
        fetch('http://localhost:8080/contracts').then(res => res.json()),
        fetch('http://localhost:8080/api/comments').then(res => res.json())
    ])
    .then(([contracts, comments]) => {
        
        contractsData = contracts;
        
        const tableBody = document.querySelector('.table-body');
        tableBody.innerHTML = '';

        const commentsByContractId = {};
        comments.forEach(comment => {
            const id = comment.contractId;
            if (!commentsByContractId[id]) {
                commentsByContractId[id] = [];
            }
            commentsByContractId[id].push(new Date(comment.createdAt));
        });

        contracts.forEach(contract => {
            const commentDates = commentsByContractId[contract.contractId] || [];
            contract.hasComment = commentDates.length > 0;
            contract.commentDate = commentDates.length > 0
                ? new Date(Math.max(...commentDates.map(date => date.getTime())))
                : null;
        });

        let filteredContracts = contracts.filter(contract =>
            contract.status !== 'signed' &&
            contract.status !== 'approved' &&
            contract.status !== 'expired'
        );

        filteredContracts = filteredContracts.filter(contract => {
            const {
                signed,
                creationDate,
                signatureDate,
                cancellationDate,
                endDate,
                type,
                status,
                approvalStage,
                contractorName
            } = filters;

            const matchHasComment = !signed || (signed === 'true' && contract.hasComment) || (signed === 'false' && !contract.hasComment);
            const matchCreation = !creationDate || new Date(contract.createdAt).toDateString() === new Date(creationDate).toDateString();
            const matchCommentDate = !signatureDate || (contract.commentDate && contract.commentDate.toDateString() === new Date(signatureDate).toDateString());
            const matchCanceledAt = !cancellationDate || (contract.cancelledAt && new Date(contract.cancelledAt).toDateString() === new Date(cancellationDate).toDateString());
            const matchEndDate = !endDate || (contract.endDate && new Date(contract.endDate).toDateString() === new Date(endDate).toDateString());
            const matchType = !type || contract.type === type;
            const matchStatus = !status || contract.status === status;
            const matchApproval = !approvalStage || contract.approvalStage === approvalStage;
            const matchContractor = !contractorName || contract.contractorName?.toLowerCase().includes(contractorName.toLowerCase());

            return matchHasComment && matchCreation && matchCommentDate && matchCanceledAt && matchEndDate &&
                matchType && matchStatus && matchApproval && matchContractor;
        });

        // Update the contract count with the filtered contracts
        updateContractCount(filteredContracts.length);

        filteredContracts.sort((a, b) => {
           
            if (a.hasComment && !b.hasComment) return -1;
            if (!a.hasComment && b.hasComment) return 1;
            
            if (a.status === 'draft' && b.status !== 'draft') return -1;
            if (a.status !== 'draft' && b.status === 'draft') return 1;
            
            return new Date(b.createdAt) - new Date(a.createdAt);
        });

        filteredContracts.forEach(contract => {
            const formattedDate = new Date(contract.createdAt).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
            });

            const actionButtons = getContractActionButtons(contract.contractId, contract.status, contract.hasComment);

            const row = document.createElement('div');
            row.classList.add('table-row');
            if (contract.hasComment) {
                row.classList.add('has-comment');
            }
            row.innerHTML = `
                <span class="select-col"><input type="checkbox" class="row-checkbox"></span>
                <span class="date-col">${formattedDate}</span>
                <span class="id-col">
                    ${contract.contractId}
                    ${contract.hasComment ? '<i class="fas fa-comment-dots comment-indicator" title="Has comments"></i>' : ''}
                </span>
                <span class="title-col">${contract.contractTitle}</span>
                <span class="status-col">${contract.status}</span>
                <span class="actions-col">${actionButtons}</span>
            `;
            tableBody.appendChild(row);
        });
    })
    .catch(error => console.error('Error fetching contracts:', error));
}

function getContractActionButtons(contractId, status, hasComment) {
    let showView = true; 
    let showShare = true;
    let showEdit = false;
    let showDelete = false;
    let showComment = hasComment;

    const statusLower = (status || '').toLowerCase();
    
    if (statusLower === 'draft') {
        showEdit = true;
        showDelete = true;
    }
    if (statusLower === 'pending with comment') {
        showEdit = true;
    }

    return `
        <div class="contract-actions">
            ${showView ? `<button class="action-btn view-btn" data-id="${contractId}" title="View"><i class="fas fa-plus"></i></button>` : ''}
            ${showShare ? `<button class="action-btn share-btn" data-id="${contractId}" title="Share"><i class="fas fa-share-alt"></i></button>` : ''}
            ${showEdit ? `<button class="action-btn edit-btn" data-id="${contractId}" title="Edit"><i class="fas fa-pen"></i></button>` : ''}
            ${showDelete ? `<button class="action-btn delete-btn" data-id="${contractId}" title="Delete"><i class="fas fa-trash-alt"></i></button>` : ''}
            ${showComment ? `<button class="action-btn comment-btn" data-id="${contractId}" title="View Comments"><i class="fas fa-comment"></i></button>` : ''}
        </div>
    `;
}

document.addEventListener('DOMContentLoaded', fetchContracts);

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
    let visibleCount = 0;
    document.querySelectorAll('.table-body .table-row').forEach(row => {
        const id = row.querySelector('.id-col')?.innerText.toLowerCase() || '';
        const name = row.querySelector('.title-col')?.innerText.toLowerCase() || '';
        if (id.includes(searchValue) || name.includes(searchValue)) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });
    updateContractCount(visibleCount);
});
const openBtn = document.getElementById('openFilterModal');
const closeBtn = document.getElementById('closeFilterModal');
const modal = document.getElementById('filterModal');

openBtn?.addEventListener('click', () => {
    modal.style.display = 'block';
});

closeBtn?.addEventListener('click', () => {
    modal.style.display = 'none';
});

window.addEventListener('click', (e) => {
    if (e.target === modal) {
        modal.style.display = 'none';
    }
});

document.querySelectorAll('input[type="date"]').forEach(dateInput => {
    const updatePlaceholder = () => {
        if (dateInput.value) {
            dateInput.classList.add('has-value');
        } else {
            dateInput.classList.remove('has-value');
        }
    };
    updatePlaceholder();
    dateInput.addEventListener('change', updatePlaceholder);
});

document.getElementById("filtersForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const filters = {
        signed: document.getElementById("filterSigned").value,
        creationDate: document.getElementById("filterCreationDate").value,
        signatureDate: document.getElementById("filterSignatureDate").value,
        cancellationDate: document.getElementById("filterCancellationDate").value,
        endDate: document.getElementById("filterEndDate").value,
        type: document.getElementById("filterType").value,
        status: document.getElementById("filterStatus").value,
        approvalStage: document.getElementById("filterApproval").value,
        contractorName: document.getElementById("filterContractor").value.trim()
    };

    fetchContracts(filters);
    modal.style.display = 'none';
});

document.querySelector('.reset-btn').addEventListener('click', () => {
    fetchContracts(); 
});
document.addEventListener('DOMContentLoaded', () => {
    fetchContracts(); 
});

document.querySelector('.refresh-btn')?.addEventListener('click', (e) => {
    e.preventDefault();
    location.reload();
});

  const path = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === path) {
      link.classList.add('active');
    }
  });
document.addEventListener("click", function (event) {
    if (event.target.closest(".view-btn")) {
        const contractId = event.target.closest(".view-btn").getAttribute("data-id");
        if (contractId) {
            window.location.href = `/contracts/${contractId}/details`;
        }
    }
    if (event.target.closest(".edit-btn")) {
        const contractId = event.target.closest(".edit-btn").getAttribute("data-id");
        if (contractId) {
            window.location.href = `/contracts/${contractId}/details?mode=edit`;
        }
    }
    if (event.target.closest(".comment-btn")) {
        const contractId = event.target.closest(".comment-btn").getAttribute("data-id");
        if (contractId) {
            showCommentsModal(contractId);
        }
    }
});

function showCommentsModal(contractId) {
   
    let modal = document.getElementById('commentsModal');
    if (!modal) {
        modal = document.createElement('div');
        modal.id = 'commentsModal';
        modal.className = 'modal';
        modal.innerHTML = `
            <div class="modal-content">
                <div class="modal-header">
                    <h2>Contract Comments</h2>
                    <span class="close">&times;</span>
                </div>
                <div class="modal-body">
                    <div class="comments-list"></div>
                </div>
            </div>
        `;
        document.body.appendChild(modal);

        const closeBtn = modal.querySelector('.close');
        closeBtn.onclick = function() {
            modal.style.display = "none";
        }

        window.onclick = function(event) {
            if (event.target == modal) {
                modal.style.display = "none";
            }
        }
    }

    fetch(`http://localhost:8080/api/comments`)
        .then(response => response.json())
        .then(comments => {
            const contractComments = comments.filter(comment => comment.contractId === parseInt(contractId));
            
            const commentsList = modal.querySelector('.comments-list');
            if (contractComments.length === 0) {
                commentsList.innerHTML = '<p class="no-comments">No comments yet.</p>';
            } else {
                commentsList.innerHTML = contractComments.map(comment => `
                    <div class="comment-item">
                        <div class="comment-header">
                            <span class="comment-author">${comment.authorMatricule || 'Unknown User'}</span>
                            <span class="comment-date">${new Date(comment.createdAt).toLocaleString()}</span>
                        </div>
                        <div class="comment-content">${comment.content}</div>
                    </div>
                `).join('');
            }
            modal.style.display = "block";
        })
        .catch(error => {
            console.error('Error fetching comments:', error);
            modal.querySelector('.comments-list').innerHTML = '<p class="error">Error loading comments.</p>';
        });
}

document.querySelector('.contract-request').addEventListener('change', function(e) {
    const selectedStatus = e.target.value.toLowerCase();
    let visibleCount = 0;
    const tableRows = document.querySelectorAll('.table-row');
    
    tableRows.forEach(row => {
        const statusCell = row.querySelector('.status-col');
        const currentStatus = statusCell.textContent.toLowerCase();
        
        if (selectedStatus === 'all status' || selectedStatus === currentStatus) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });
    updateContractCount(visibleCount);
});


document.querySelector('.contract-action').addEventListener('change', function(e) {
    const selectedType = e.target.value.toLowerCase();
    let visibleCount = 0;
    const tableRows = document.querySelectorAll('.table-row');
    
    tableRows.forEach(row => {
        const contractId = row.querySelector('.id-col').textContent.trim();
        const contract = contractsData.find(c => c.contractId.toString() === contractId);
        
    
        if (!selectedType || selectedType === 'all types') {
            row.style.display = '';
            visibleCount++;
            return;
        }
        
        if (!contract) {
            row.style.display = 'none';
            return;
        }
        
        const contractType = contract.type?.toLowerCase();
        
        if (contractType === selectedType) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });
    updateContractCount(visibleCount);
});

document.querySelector('.refresh-btn').addEventListener('click', function() {
  
    document.querySelector('.contract-request').value = 'All Status';
    document.querySelector('.contract-action').selectedIndex = 0;

    const tableRows = document.querySelectorAll('.table-row');
    tableRows.forEach(row => {
        row.style.display = '';
    });
    updateContractCount(tableRows.length);
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
