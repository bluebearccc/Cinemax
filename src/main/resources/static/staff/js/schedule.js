const scheduleData = serverData.schedules;
const startTime = serverData.startTime;
const endTime = serverData.endTime;
let currentDate = new Date();
let selectedDate = null; // Giữ null để không tự động chọn ngày
let modalCurrentDate = new Date();
let modalSelectedDate = null;
function clearScheduleInputs() {
    // Xóa giá trị ô StartTime
    $('#startTime').val('');

    // Reset và vô hiệu hóa ô chọn Theater
    $('#theater').val('').prop('disabled', true);

    // Xóa danh sách phòng đã hiển thị
    $('#room-list-container').html('');
}
function generateCalendar() {
    const calendar = document.getElementById('calendar');
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    // Clear existing calendar days
    const existingDays = calendar.querySelectorAll('.calendar-day');
    existingDays.forEach(day => day.remove());
    // First day of month and number of days
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const today = new Date(); // Vẫn giữ lại để dùng cho các logic khác nếu cần
    // Adjust first day (Monday = 0)
    const adjustedFirstDay = (firstDay === 0) ? 6 : firstDay - 1;
    // Add empty cells for previous month days
    for (let i = 0; i < adjustedFirstDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'calendar-day';
        emptyDay.style.opacity = '0.3';
        calendar.appendChild(emptyDay);
    }
    // Add days of current month
    for (let day = 1; day <= daysInMonth; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day';
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        const scheduleCount = scheduleData[dateStr] ? scheduleData[dateStr].length : 0;
        // Check if it's selected date
        if (selectedDate && year === selectedDate.getFullYear() && month === selectedDate.getMonth() && day === selectedDate.getDate()) {
            dayElement.classList.add('selected');
        }
        dayElement.innerHTML = `
<div class="day-number">${day}</div>
${scheduleCount > 0 ? `<div class="schedule-count">${scheduleCount} Slot</div>` : ''}
`;
        dayElement.onclick = () => selectDate(year, month, day);
        calendar.appendChild(dayElement);
    }
    // Update month display
    const monthNames = [
        'January ', 'February ', 'March ', 'April ', 'May ', 'June ',
        'July ', 'August ', 'September ', 'October ', 'November ', 'December '
    ];
    document.getElementById('currentMonth').textContent = `${monthNames[month]}, ${year}`;
}
function selectDate(year, month, day) {
    selectedDate = new Date(year, month, day);
    generateCalendar();
    updateDailySchedule();
}
function updateDailySchedule() {
    const scheduleList = document.querySelector('.schedule-list');
    const scheduleDateElement = document.querySelector('.schedule-date');
    if (!selectedDate) {
        scheduleDateElement.textContent = 'Please select a date to view schedule slot';
        scheduleList.innerHTML = '<div class="no-schedule">Select a date to view schedule slot</div>';
        return; // Dừng hàm tại đây
    }
    const dateStr = `${selectedDate.getFullYear()}-${String(selectedDate.getMonth() + 1).padStart(2, '0')}-${String(selectedDate.getDate()).padStart(2, '0')}`;
    const schedules = scheduleData[dateStr] || [];
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const monthNames = [
        'January ', 'February ', 'March ', 'April ', 'May ', 'June ',
        'July ', 'August ', 'September ', 'October ', 'November ', 'December '
    ];
    const dayName = dayNames[selectedDate.getDay()];
    const day = selectedDate.getDate();
    const month = monthNames[selectedDate.getMonth()];
    const year = selectedDate.getFullYear();
    scheduleDateElement.textContent = `${dayName}, ${String(day).padStart(2, '0')} ${month}, ${year}`;
    if (schedules.length === 0) {
        scheduleList.innerHTML = '<div class="no-schedule">No schedule</div>';
    } else {
        scheduleList.innerHTML = schedules.map(schedule => `
<div class="schedule-item" onclick="editSchedule('${schedule.id}')">
<div class="schedule-time">${schedule.time} ~ ${schedule.endTime}</div>
<div class="schedule-details">
<span class="room-info">${schedule.theater}</span>
<span class="room-info">${schedule.room}</span>
</div>
</div>
`).join('');
    }
}

function previousMonth() {
    currentDate.setMonth(currentDate.getMonth() - 1);
    generateCalendar();
}

function nextMonth() {
    currentDate.setMonth(currentDate.getMonth() + 1);
    generateCalendar();
}

function addNewSchedule() {
    document.getElementById('scheduleModal').classList.add('show');
    // Đặt modalCurrentDate về tháng hiện tại hoặc tháng startTime nếu hiện tại < startTime
    const today = new Date();
    const startDate = new Date(startTime);
    const endDate = new Date(endTime);
    if (today < startDate) {
        modalCurrentDate = new Date(startDate);
    } else if (today > endDate) {
        modalCurrentDate = new Date(endDate);
    } else {
        modalCurrentDate = new Date(today);
    }
    generateMiniCalendar();
}
function closeModal() {
    document.getElementById('scheduleModal').classList.remove('show');
}
function generateMiniCalendar() {
    const calendar = document.getElementById('miniCalendar');
    const year = modalCurrentDate.getFullYear();
    const month = modalCurrentDate.getMonth();
    // Chuyển đổi startTime và endTime thành Date objects để so sánh
    const startDate = new Date(startTime);
    const endDate = new Date(endTime);
    // Clear existing calendar days
    calendar.innerHTML = '';
    // Add headers
    const headers = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
    headers.forEach(header => {
        const headerElement = document.createElement('div');
        headerElement.className = 'mini-calendar-header';
        headerElement.textContent = header;
        calendar.appendChild(headerElement);
    });
    // First day of month and number of days
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const today = new Date();
    // Adjust first day (Monday = 0)
    const adjustedFirstDay = (firstDay === 0) ? 6 : firstDay - 1;
    // Add empty cells for previous month days
    for (let i = 0; i < adjustedFirstDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'mini-calendar-day';
        emptyDay.style.opacity = '0.3';
        calendar.appendChild(emptyDay);
    }
    // Add days of current month
    for (let day = 1; day <= daysInMonth; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'mini-calendar-day';
        dayElement.textContent = day;
        // Tạo date object cho ngày hiện tại trong vòng lặp
        const currentDayDate = new Date(year, month, day);
        // Tạo ngày hôm nay để so sánh (chỉ lấy ngày, không lấy giờ)
        const todayDate = new Date();
        todayDate.setHours(0, 0, 0, 0);
        currentDayDate.setHours(0, 0, 0, 0);
        // Kiểm tra xem ngày có nằm trong khoảng cho phép không
        const isInValidRange = currentDayDate >= startDate && currentDayDate <= endDate;
        // Kiểm tra xem có phải là ngày trong quá khứ không
        const isPastDate = currentDayDate < todayDate;
        if (!isInValidRange) {
            // Vô hiệu hóa các ngày ngoài khoảng thời gian cho phép
            dayElement.classList.add('disabled');
            dayElement.style.opacity = '0.3';
            dayElement.style.cursor = 'not-allowed';
            dayElement.style.backgroundColor = '#f5f5f5';
            dayElement.style.color = '#ccc';
            dayElement.title = 'Out of range date';
        } else if (isPastDate) {
            // Làm mờ các ngày trong quá khứ nhưng vẫn hiển thị
            dayElement.classList.add('past-date');
            dayElement.style.opacity = '0.4';
            dayElement.style.cursor = 'not-allowed';
            dayElement.style.color = '#999';
            dayElement.style.backgroundColor = '#fafafa';
            dayElement.title = 'Cannot select past dates';
            // Không thêm onclick cho ngày quá khứ
        } else {
            // Ngày hợp lệ có thể chọn được
            // Check if it's selected date
            if (modalSelectedDate && year === modalSelectedDate.getFullYear() &&
                month === modalSelectedDate.getMonth() && day === modalSelectedDate.getDate()) {
                dayElement.classList.add('selected');
            }
            // Chỉ cho phép click vào các ngày hợp lệ
            dayElement.onclick = () => selectMiniDate(year, month, day);
            dayElement.style.cursor = 'pointer';
        }
        calendar.appendChild(dayElement);
    }
    // Update month display
    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'

    ];
    document.getElementById('currentMonthMini').textContent = `${monthNames[month]}, ${year}`;
}
function selectMiniDate(year, month, day) {
    const selectedDateObj = new Date(year, month, day);
    const startDate = new Date(startTime);
    const endDate = new Date(endTime);
    // Tạo ngày hôm nay để so sánh
    const todayDate = new Date();
    todayDate.setHours(0, 0, 0, 0);
    selectedDateObj.setHours(0, 0, 0, 0);
    // Kiểm tra xem ngày được chọn có hợp lệ không
    if (selectedDateObj < startDate || selectedDateObj > endDate) {
        alert('Selected date is not within allowed time range!');
        return;
    }
    // Kiểm tra xem có phải là ngày trong quá khứ không
    if (selectedDateObj < todayDate) {
        alert('Cannot select past dates!');
        return;
    }
    modalSelectedDate = new Date(year, month, day);
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    document.getElementById('selectedDateInput').value = dateStr;
    clearScheduleInputs()
    $('#selectedDateInput').trigger('change');
    generateMiniCalendar();
}
function previousMonthMini() {
    const newDate = new Date(modalCurrentDate);
    newDate.setMonth(newDate.getMonth() - 1);
    // Kiểm tra xem tháng mới có nằm trong khoảng cho phép không
    const startDate = new Date(startTime);
    const firstDayOfNewMonth = new Date(newDate.getFullYear(), newDate.getMonth(), 1);
    if (firstDayOfNewMonth >= new Date(startDate.getFullYear(), startDate.getMonth(), 1)) {
        modalCurrentDate = newDate;
        generateMiniCalendar();
    }
}
function nextMonthMini() {
    const newDate = new Date(modalCurrentDate);
    newDate.setMonth(newDate.getMonth() + 1);
    // Kiểm tra xem tháng mới có nằm trong khoảng cho phép không
    const endDate = new Date(endTime);
    const lastDayOfNewMonth = new Date(newDate.getFullYear(), newDate.getMonth() + 1, 0);
    if (lastDayOfNewMonth <= new Date(endDate.getFullYear(), endDate.getMonth() + 1, 0)) {
        modalCurrentDate = newDate;
        generateMiniCalendar();
    }
}
// Handle form submission
// Close modal when clicking outside
document.getElementById('scheduleModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeModal();
    }
});

function editSchedule(id) {
    alert(`Hiện ra edit form cho schedule có id: ${id}?`);
}
// Initialize calendar
generateCalendar();
updateDailySchedule();