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

        // Simulate AI response
        setTimeout(() => {
            generateAIResponse(message);
        }, 1000);
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
    const responses = {
        'xin chào': 'Xin chào! Tôi có thể giúp gì cho bạn hôm nay?',
        'phim hay': 'Hiện tại chúng tôi có nhiều phim hay như "Siêu Anh Hùng", "Tình Yêu Mùa Hè", "Bí Ẩn Đêm Tối". Bạn thích thể loại nào?',
        'đặt vé': 'Tôi sẽ hướng dẫn bạn đặt vé. Trước tiên, bạn muốn xem phim nào và vào lúc nào?',
        'giá vé': 'Giá vé dao động từ 80.000đ - 150.000đ tùy theo suất chiếu và loại ghế. Bạn có muốn biết chi tiết không?',
        'rạp chiếu': 'Chúng tôi có rạp tại nhiều địa điểm: Trung tâm, Quận 1, Quận 7, Thủ Đức. Bạn muốn xem ở đâu?'
    };

    const lowerMessage = userMessage.toLowerCase();
    let response = 'Tôi hiểu bạn đang hỏi về "' + userMessage + '". Tôi sẽ kết nối bạn với nhân viên tư vấn để được hỗ trợ tốt nhất. Bạn có câu hỏi nào khác không?';

    for (let key in responses) {
        if (lowerMessage.includes(key)) {
            response = responses[key];
            break;
        }
    }

    addMessage(response, 'bot');
}

function handleEnter(event) {
    if (event.key === 'Enter') {
        sendMessage();
    }
}

function scrollToMovies() {
    document.getElementById('movies').scrollIntoView({behavior: 'smooth'});
}

// Header scroll effect
window.addEventListener('scroll', () => {
    const header = document.querySelector('.header');
    if (window.scrollY > 100) {
        header.style.background = 'rgba(0, 0, 0, 0.95)';
    } else {
        header.style.background = 'rgba(0, 0, 0, 0.9)';
    }
});

// Movie card click effects
document.querySelectorAll('.movie-card').forEach(card => {
    card.addEventListener('click', () => {
        card.style.transform = 'scale(0.98)';
        setTimeout(() => {
            card.style.transform = 'translateY(-10px)';
        }, 100);
    });
});
