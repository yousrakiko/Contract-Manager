let stompClient = null;

// Initialize WebSocket connection when page loads
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM Content Loaded - Initializing WebSocket...');
    connectWebSocket();
    loadExistingNotifications();
    updateNotificationBadge();
});

function connectWebSocket() {
    console.log('Attempting to connect to WebSocket...');
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // Enable debug logging
    stompClient.debug = function(str) {
        console.log('STOMP: ' + str);
    };

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket: ' + frame);

        // Get user ID from hidden input
        const userIdElement = document.getElementById("user-matricule");
        if (!userIdElement) {
            console.error('User matricule element not found!');
            return;
        }
        
        const userId = userIdElement.value;
        console.log('Subscribing to notifications for user: ' + userId);

        // Subscribe to user-specific notifications
        stompClient.subscribe('/user/' + userId + '/queue/notifications', function (notification) {
            console.log('Received WebSocket notification:', notification.body);
            try {
                const data = JSON.parse(notification.body);
                console.log('Parsed notification data:', data);
                addNotificationToList(
                    data.title,
                    data.message,
                    data.link,
                    data.type,
                    data.createdAt,
                    data.id
                );
                updateNotificationBadge();
                showNotificationToast(data.title, data.message);
            } catch (error) {
                console.error('Error parsing notification:', error);
            }
        });
    }, function(error) {
        console.error('WebSocket connection failed:', error);
        // Retry connection after 5 seconds
        setTimeout(connectWebSocket, 5000);
    });
}

function loadExistingNotifications() {
    console.log('Loading existing notifications...');
    const userIdElement = document.getElementById("user-matricule");
    if (!userIdElement) {
        console.error('User matricule element not found!');
        return;
    }
    
    const userId = userIdElement.value;
    console.log('Fetching notifications for user:', userId);
    
    // Fetch existing notifications
    fetch(`/notifications/${userId}`)
        .then(response => {
            console.log('Notification response status:', response.status);
            return response.json();
        })
        .then(notifications => {
            console.log('Received notifications:', notifications);
            const notifList = document.getElementById("notifList");
            if (!notifList) {
                console.error('Notification list element not found!');
                return;
            }

            // Clear existing notifications
            notifList.innerHTML = '';

            // Sort notifications by date (newest first)
            notifications.sort((a, b) => {
                // Parse the dates, handling both ISO format and local string format
                const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
                const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
                
                // Log the dates for debugging
                console.log('Comparing dates:', {
                    a: new Date(dateA).toISOString(),
                    b: new Date(dateB).toISOString(),
                    result: dateB - dateA
                });
                
                return dateB - dateA; // Descending order (newest first)
            });

            // Add each notification to the list
            notifications.forEach(notification => {
                console.log('Adding notification to list:', notification);
                addNotificationToList(
                    notification.title,
                    notification.message,
                    notification.link,
                    notification.type,
                    notification.createdAt,
                    notification.id
                );
            });

            // Update badge count
            updateNotificationBadge();
        })
        .catch(error => {
            console.error('Error loading notifications:', error);
        });
}

function addNotificationToList(title, message, link, type, createdAt, id) {
    const notifList = document.getElementById("notifList");
    if (!notifList) return;

    // Create a new <li> element
    const newItem = document.createElement("li");
    newItem.classList.add("notif-item", "unread", `notif-${type.toLowerCase()}`);
    newItem.dataset.notificationId = id; // Store notification ID
    
    // Format the timestamp using the actual date from createdAt
    const timestamp = createdAt ? new Date(createdAt).toLocaleString() : new Date().toLocaleString();
    
    // Create notification content with only the message as a link
    const content = `
        <strong>${title}</strong><br>
        ${link ? 
            `<a href="${link}" class="notif-link">${message}</a>` : 
            `<span>${message}</span>`
        }<br>
        <small class="notif-time">${timestamp}</small>
    `;
    
    newItem.innerHTML = content;

   notifList.appendChild(newItem);
    // Mark as read when displayed if we have an ID
    if (id) {
        markNotificationAsRead(id);
    }
    
    // Update notification count
    updateNotificationBadge();
}

function markNotificationAsRead(notificationId) {
    if (!notificationId) {
        console.error('Cannot mark notification as read: No notification ID provided');
        return;
    }

    // Get CSRF token from meta tag
    const token = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch(`/notifications/${notificationId}/read`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        }
    })
    .then(response => {
        if (response.ok) {
            const notifItem = document.querySelector(`[data-notification-id="${notificationId}"]`);
            if (notifItem) {
                notifItem.classList.remove('unread');
                notifItem.classList.add('read');
                updateNotificationBadge();
            }
        } else {
            console.error('Failed to mark notification as read:', response.status);
        }
    })
    .catch(error => {
        console.error('Error marking notification as read:', error);
    });
}

function updateNotificationBadge() {
    const unreadCount = document.querySelectorAll('.notif-item.unread').length;
    const badge = document.getElementById('notificationBadge');
    if (badge) {
        badge.textContent = unreadCount;
        badge.style.display = unreadCount > 0 ? 'block' : 'none';
    }
}

// Handle notification clicks
document.addEventListener('click', function(event) {
    if (event.target.closest('.notif-item')) {
        const notifItem = event.target.closest('.notif-item');
        if (notifItem.classList.contains('unread')) {
            notifItem.classList.remove('unread');
            notifItem.classList.add('read');
            updateNotificationBadge();
        }
    }
});

// Handle notification filters
document.addEventListener('DOMContentLoaded', function() {
    const filterButtons = document.querySelectorAll('.filter-btn');
    const notifList = document.getElementById('notifList');

    if (filterButtons && notifList) {
        filterButtons.forEach(button => {
            button.addEventListener('click', function() {
                const filter = this.getAttribute('data-filter');
                const notifications = notifList.querySelectorAll('.notif-item');

                notifications.forEach(notif => {
                    if (filter === 'all') {
                        notif.style.display = 'block';
                    } else if (filter === 'unread' && notif.classList.contains('unread')) {
                        notif.style.display = 'block';
                    } else if (filter === 'read' && notif.classList.contains('read')) {
                        notif.style.display = 'block';
                    } else {
                        notif.style.display = 'none';
                    }
                });
            });
        });
    }
});
