// Chatbot functionality
// let chatOpen = false;
let isOpen = false;
let messageCount = 0;

// function toggleChat() {
//     const chatWindow = document.getElementById('chatWindow');
//     chatOpen = !chatOpen;
//
//     if (chatOpen) {
//         chatWindow.classList.add('active');
//     } else {
//         chatWindow.classList.remove('active');
//     }
// }

// function addMessage(message, sender) {
//     const messagesContainer = document.getElementById('chatMessages');
//     const messageDiv = document.createElement('div');
//     messageDiv.className = `message ${sender}`;
//     messageDiv.textContent = message;
//     messagesContainer.appendChild(messageDiv);
//     messagesContainer.scrollTop = messagesContainer.scrollHeight;
// }

function generateAIResponse(userMessage) {
    console.log('userMessage: ', userMessage);

    $.ajax({
        url: "/chat/ask",
        type: "POST",
        contentType: "application/json",
        data: JSON.stringify({
            message: userMessage
        }),
        success: function (data) {
            console.log(data);
            addMessage('bot', data);
        },
        error: function (e) {
            addMessage('bot', 'I\'m sorry, I\'m not able to understand your message. Please provide more specific context so i can help you better.');
        }
    });
}

function handleEnter(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

const chatToggle = document.getElementById('chatToggle');
const chatWidget = document.getElementById('chatWidget');
const chatMessages = document.getElementById('chatMessages');
const chatInput = document.getElementById('chatInput');
const notificationBadge = document.getElementById('notificationBadge');

// Toggle chat widget
chatToggle.addEventListener('click', function() {
    isOpen = !isOpen;

    if (isOpen) {
        chatWidget.classList.add('active');
        chatToggle.classList.add('active');
        chatInput.focus();
        notificationBadge.style.display = 'none';
    } else {
        chatWidget.classList.remove('active');
        chatToggle.classList.remove('active');
    }
});

// Send message on Enter
chatInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
});

// function sendMessage() {
//     const input = document.getElementById('chatInput');
//     const message = input.value.trim();
//
//     if (message) {
//         addMessage(message, 'user');
//         input.value = '';
//
//         generateAIResponse(message);
//     }
// }

function sendMessage() {
    const message = chatInput.value.trim();
    if (!message) return;
    const lowerCaseMessage = message.toLowerCase();

    addMessage('user', message);
    chatInput.value = '';

    setTimeout(() => {
        if (message) {
            generateAIResponse(message);
        }
    }, 0);
}

function sendQuickMessage(message) {
    addMessage('user', message);

    setTimeout(() => {
        generateAIResponse(message);
    }, 0);
}

function addMessage(sender, text) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message-chat-bot ${sender}`;
    // Using innerHTML to allow for bold tags and line breaks in bot responses
    messageDiv.innerHTML = `<div class="message-bubble-chat-bot">${text}</div>`;

    const welcomeMsgContainer = document.querySelector('.welcome-msg-chat-bot');
    const quickOptionsContainer = document.querySelector('.quick-options-chat-bot');
    if (welcomeMsgContainer && messageCount === 0) {
        welcomeMsgContainer.style.display = 'none';
        quickOptionsContainer.style.display = 'none';
    }

    chatMessages.appendChild(messageDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;
    messageCount++;

    if (!isOpen && sender === 'bot') {
        notificationBadge.style.display = 'flex';
        notificationBadge.textContent = '!';

        chatToggle.style.animation = 'bounce 0.6s ease-in-out 3';
        setTimeout(() => {
            chatToggle.style.animation = 'bounce 2s infinite';
        }, 0);
    }
}