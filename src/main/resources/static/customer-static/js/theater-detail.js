function generateDates(selectedIndex = 0) {
    const dateSelector = document.getElementById('date-selector');
    dateSelector.innerHTML = '';
    const today = new Date();
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    for (let i = 0; i < 7; i++) {
        const date = new Date(today);
        date.setDate(today.getDate() + i);

        const dayLabel = i === 0 ? 'Today' : dayNames[date.getDay()];

        const dateItem = document.createElement('div');
        dateItem.className = 'date-item';
        dateItem.dataset.index = i; // 👈 dùng index (0–6)

        if (i === selectedIndex) {
            dateItem.classList.add('active');
        }

        dateItem.innerHTML = `
            <div class="date-number">${date.getDate()}</div>
            <div class="date-day">${dayLabel}</div>
        `;

        dateSelector.appendChild(dateItem);
    }
}

generateDates();


function attachDateItemEvents() {
    const dateItems = document.querySelectorAll('.date-item');

    dateItems.forEach(item => {
        item.addEventListener('click', function () {
            const selectedIndex = parseInt(this.dataset.index);
            const roomType = document.querySelector('.custom-button-item.active span').innerText;
            dateItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            // $.ajax({
            //     url: "/customer/home/loadBookMovie",
            //     type: "get",
            //     data: {
            //         selectedIndex: selectedIndex,
            //         roomType: roomType
            //     },
            //     success: function (data) {
            //         document.querySelector('div.book-movie-area').innerHTML = data;
            //
            //         // Gọi lại generateDates để gắn active đúng ngày
            //         generateDates(selectedIndex);
            //         attachDateItemEvents();
            //         attachCinemaItemEvents();
            //         attachRoomItemEvents();
            //     },
            //     error: function (e) {
            //         alert("Lỗi khi tải lịch chiếu");
            //     }
            // });
        });
    });
}

function attachRoomItemEvents() {
    const roomButtonItems = document.querySelectorAll('.custom-button-item');
    roomButtonItems.forEach(item => {
        item.addEventListener('click', () => {
            roomButtonItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            const selectedIndex = parseInt(document.querySelector('.date-item.active')?.dataset.index || 0);
            const roomType = document.querySelector('.custom-button-item.active span').innerText;

            // $.ajax({
            //     url: "/customer/home/loadBookMovie",
            //     type: "get",
            //     data: {
            //         selectedIndex: selectedIndex,
            //         roomType: roomType
            //     },
            //     success: function (data) {
            //         console.log(data);
            //         document.querySelector('.book-movie-area').innerHTML = data;
            //
            //         // Gọi lại generateDates để gắn active đúng ngày
            //         generateDates(selectedIndex);
            //         attachDateItemEvents();
            //         attachCinemaItemEvents();
            //         attachRoomItemEvents();
            //     },
            //     error: function (e) {
            //         alert("Lỗi khi tải lịch chiếu");
            //     }
            // });
        });
    });
}

document.addEventListener('DOMContentLoaded', attachDateItemEvents);
document.addEventListener('DOMContentLoaded', attachRoomItemEvents);
