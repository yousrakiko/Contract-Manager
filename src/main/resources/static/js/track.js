
let contractsData = [];

function updateContractCount() {
    const visibleRows = document.querySelectorAll('.table-row:not([style*="display: none"])').length;
    const subtitle = document.querySelector('.tracking-subtitle');
    if (subtitle) {
        subtitle.textContent = `Total ${visibleRows} contracts`;
    }
}

function fetchTrackContracts(filters = {}){
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

        let filteredContracts = contracts.filter(contract =>
                contract.status !== 'draft' &&
                contract.status !== 'canceled' &&
                contract.status !== 'expired'
            );

        filteredContracts.forEach(contract => {
            const commentDates = commentsByContractId[contract.contractId] || [];
            contract.hasComment = commentDates.length > 0;
        });

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

            const matchSigned = !signed || (signed === 'true' && contract.isSigned) || (signed === 'false' && !contract.isSigned);
            const matchCreation = !creationDate || new Date(contract.createdAt).toDateString() === new Date(creationDate).toDateString();
            const matchSignedAt = !signatureDate || (contract.signedAt && new Date(contract.signedAt).toDateString() === new Date(signatureDate).toDateString());
            const matchCanceledAt = !cancellationDate || (contract.cancelledAt && new Date(contract.cancelledAt).toDateString() === new Date(cancellationDate).toDateString());
            const matchEndDate = !endDate || (contract.endDate && new Date(contract.endDate).toDateString() === new Date(endDate).toDateString());
            const matchType = !type || contract.type?.toLowerCase() === type.toLowerCase();
            const matchStatus = !status || contract.status === status;
            const matchApproval = !approvalStage || contract.approvalStage === approvalStage;
            const matchContractor = !contractorName || contract.contractorName?.toLowerCase().includes(contractorName.toLowerCase());

            return matchSigned && matchCreation && matchSignedAt && matchCanceledAt && matchEndDate &&
                   matchType && matchStatus && matchApproval && matchContractor;
        });

        filteredContracts.sort((a, b) => {
            if (a.status === 'pending' && b.status !== 'pending') return -1;
            if (b.status === 'pending' && a.status !== 'pending') return 1;
            return new Date(b.createdAt) - new Date(a.createdAt);
        });

        filteredContracts.forEach(contract => {
            const formattedDate = new Date(contract.createdAt).toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
            });

            const row = document.createElement('div');
            row.classList.add('table-row');
            if (contract.hasComment) {
                row.classList.add('has-comment');
            }

            const actionButtons = getTrackActionButtons(contract.contractId, contract.approvalStage, contract.status, false);
            row.innerHTML = `
                <span class="select-col"><input type="checkbox" class="row-checkbox"></span>
                <span class="date-col">${formattedDate}</span>
            <span class="id-col">
                ${contract.contractId}
                ${contract.hasComment && (contract.status.toLowerCase() === 'pending' || contract.status.toLowerCase() === 'pending with comment') ? '<i class="fas fa-comment-dots comment-indicator" title="Has comments"></i>' : ''}
            </span>
                <span class="title-col">${contract.contractTitle}</span>
                <span class="isSigned-col">${contract.isSigned ? 'Yes' : 'No'}</span>
                <span class="status-col">${contract.status}</span>
                <span class="actions-col">${actionButtons}</span>
            `;

            tableBody.appendChild(row);
        });

        updateContractCount();
    })
    .catch(error => console.error('Error fetching contracts:', error));
}

function getTrackActionButtons(contractId, stage, status, isTrackView, mode = '') {
    let showView = true;
    let showShare = true;
    let showSign = false;
    let showComment = false;

    const statusLower = (status || '').toLowerCase();
    
    if (statusLower === 'pending' || statusLower === 'modified' || statusLower === 'pending with comment') {
        showSign = true;
    }

    if (statusLower === 'pending' || statusLower === 'pending with comment') {
        showComment = true;
    }

    if (mode === 'sign') {
        showSign = true;
    }
    if (mode === 'comment') {
        showComment = true;
    }

    return `
        <div class="contract-actions">
            ${showView ? `<button class="action-btn view-btn" data-id="${contractId}" title="View"><i class="fas fa-plus"></i></button>` : ''}
            ${showShare ? `<button class="action-btn share-btn" data-id="${contractId}" title="Share"><i class="fas fa-share-alt"></i></button>` : ''}
            ${showSign ? `<button class="action-btn sign-btn" data-id="${contractId}" title="Sign"><i class="fas fa-pen-nib"></i></button>` : ''}
            ${showComment ? `<button class="action-btn comment-btn" data-id="${contractId}" title="Comment"><i class="fas fa-comment"></i></button>` : ''}
        </div>
    `;
}

document.addEventListener('click', function (e) {
    if (e.target.closest('.sign-btn')) {
        const contractId = e.target.closest('.sign-btn').dataset.id;
        window.location.href = `/contracts/${contractId}/sign`;
    }

    if (e.target.closest('.comment-btn')) {
        const contractId = e.target.closest('.comment-btn').dataset.id;
        window.location.href = `/contracts/${contractId}/comment`;
    }
});

document.addEventListener('DOMContentLoaded', fetchTrackContracts);

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
        const name = row.querySelector('.title-col')?.innerText.toLowerCase() || '';
        if (id.includes(searchValue) || name.includes(searchValue)) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
});

const openBtn = document.getElementById('openFilterModal');
const closeBtn = document.getElementById('closeFilterModal');
const modal = document.getElementById('filterModal');

openBtn.addEventListener('click', () => {
  modal.style.display = 'block';
});

closeBtn.addEventListener('click', () => {
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


document.getElementById("applyFiltersBtn").addEventListener("click", () => {
  const signedValue = document.getElementById("filterSigned").value;

  const rows = document.querySelectorAll('.table-body .table-row');
  rows.forEach(row => {
    const signedText = row.querySelector('.isSigned-col')?.innerText.trim().toLowerCase();
    const matchSigned = !signedValue || 
        (signedValue === 'true' && signedText === 'yes') ||
        (signedValue === 'false' && signedText === 'no');

    row.style.display = matchSigned ? '' : 'none';
  });

  modal.style.display = 'none';
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

    fetchTrackContracts(filters); 
    modal.style.display = 'none';
});
document.querySelector('.reset-btn').addEventListener('click', () => {
    fetchTrackContracts(); 
});
document.addEventListener('DOMContentLoaded', () => {
    fetchTrackContracts(); 
});
document.querySelector('.refresh-btn').addEventListener('click', (e) => {
  e.preventDefault(); 
  location.reload(); 
});

  const path = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === path) {
      link.classList.add('active');
    }
  });

document.querySelector('.contract-request').addEventListener('change', function(e) {
    const selectedStatus = e.target.value.toLowerCase();
    const tableRows = document.querySelectorAll('.table-row');
    
    tableRows.forEach(row => {
        const statusCell = row.querySelector('.status-col');
        const currentStatus = statusCell.textContent.toLowerCase();
        
        if (selectedStatus === 'all status' || selectedStatus === currentStatus) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
    updateContractCount();
});

document.querySelector('.contract-action').addEventListener('change', function(e) {
    const selectedOption = e.target.value.toLowerCase();
    const tableRows = document.querySelectorAll('.table-row');
    
    tableRows.forEach(row => {
        const statusCell = row.querySelector('.status-col');
        const currentStatus = statusCell.textContent.toLowerCase();
        const hasComment = row.classList.contains('has-comment');
        
        if (!selectedOption || selectedOption === 'all') {
            row.style.display = '';
            return;
        }
        
        switch(selectedOption) {
            case 'pending_with_comment':
                row.style.display = (currentStatus === 'pending' && hasComment) ? '' : 'none';
                break;
            case 'pending_without_comment':
                row.style.display = (currentStatus === 'pending' && !hasComment) ? '' : 'none';
                break;
            case 'modified':
                row.style.display = (currentStatus === 'modified') ? '' : 'none';
                break;
            default:
                row.style.display = '';
        }
    });
    updateContractCount();
});

document.querySelector('.refresh-btn').addEventListener('click', function() {

    document.querySelector('.contract-request').value = 'All Status';
    document.querySelector('.contract-action').selectedIndex = 0;
    
    const tableRows = document.querySelectorAll('.table-row');
    tableRows.forEach(row => {
        row.style.display = '';
    });
    updateContractCount();
});

document.addEventListener("click", function (event) {
    if (event.target.closest(".view-btn")) {
        const contractId = event.target.closest(".view-btn").getAttribute("data-id");
        if (contractId) {
            window.location.href = `/contracts/${contractId}/details`;
        }
    }
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