// Chatbot functionality
let chatOpen = false;

function toggleChat() {
    const chatWindow = document.getElementById('chatWindow');
    chatOpen = !chatOpen;

    if (chatOpen) {
        chatWindow.classList.add('active');
    } else {
        chatWindow.classList.remove('active');
    }
}

function sendMessage() {
    const input = document.getElementById('chatInput');
    const message = input.value.trim();

    if (message) {
        addMessage(message, 'user');
        input.value = '';

        generateAIResponse(message);
    }
}

function addMessage(message, sender) {
    const messagesContainer = document.getElementById('chatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}`;
    messageDiv.textContent = message;
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

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
            addMessage(data, 'bot');
        },
        error: function (e) {
            alert("Lỗi khi dùng chatbot");
        }
    });

}

function handleEnter(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}
