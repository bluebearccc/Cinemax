const startTimeUpdate = serverData.startTime;
const endTimeUpdate = serverData.endTime;

let modalCurrentDateUpdate = new Date();
let modalSelectedDateUpdate = null;
let originalScheduleDate = null;

function addNewUpdateSchedule() {
    document.getElementById('editScheduleModal').classList.add('show');

    const today = new Date();
    // Chuyển đổi chuỗi thời gian từ server thành đối tượng Date
    const startDate = new Date(startTimeUpdate);
    const endDate = new Date(endTimeUpdate);

    today.setHours(0, 0, 0, 0);
    startDate.setHours(0, 0, 0, 0);
    endDate.setHours(0, 0, 0, 0);

    if (originalScheduleDate) {
        modalCurrentDateUpdate = new Date(originalScheduleDate);
    } else if (today < startDate) {
        modalCurrentDateUpdate = new Date(startDate);
    } else if (today > endDate) {
        modalCurrentDateUpdate = new Date(endDate);
    } else {
        modalCurrentDateUpdate = new Date(today);
    }

    generateMiniCalendarUpdate();
}

function closeEditModal() {
    document.getElementById('editScheduleModal').classList.remove('show');
    modalSelectedDateUpdate = null;
    originalScheduleDate = null;
}

function generateMiniCalendarUpdate() {
    const calendar = document.getElementById('miniCalendarUpdate');
    if (!calendar) return; // Dừng lại nếu không tìm thấy calendar

    const year = modalCurrentDateUpdate.getFullYear();
    const month = modalCurrentDateUpdate.getMonth();

    const startDate = new Date(startTimeUpdate);
    const endDate = new Date(endTimeUpdate);

    // Xóa các ngày cũ
    calendar.innerHTML = '';

    const headers = ['Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa', 'Su'];
    headers.forEach(header => {
        const headerElement = document.createElement('div');
        headerElement.className = 'mini-calendar-header';
        headerElement.textContent = header;
        calendar.appendChild(headerElement);
    });

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const today = new Date();
    const adjustedFirstDay = (firstDay === 0) ? 6 : firstDay - 1;

    for (let i = 0; i < adjustedFirstDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'mini-calendar-day';
        emptyDay.style.opacity = '0.3';
        calendar.appendChild(emptyDay);
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'mini-calendar-day';
        dayElement.textContent = day;
        const currentDayDate = new Date(year, month, day);
        const todayDate = new Date();
        todayDate.setHours(0, 0, 0, 0);
        currentDayDate.setHours(0, 0, 0, 0);


        const isOriginalScheduleDate = originalScheduleDate &&
            (currentDayDate.getTime() === new Date(originalScheduleDate).setHours(0, 0, 0, 0));

        const isInValidRange = currentDayDate >= startDate && currentDayDate <= endDate;
        const isPastDate = currentDayDate < todayDate;

        if (!isInValidRange) {
            dayElement.classList.add('disabled');
            dayElement.style.cssText = 'opacity: 0.3; cursor: not-allowed; background-color: #f5f5f5; color: #ccc;';
            dayElement.title = 'Out of range date';
        }
        else if (isPastDate && !isOriginalScheduleDate) {
            dayElement.classList.add('past-date');
            dayElement.style.cssText = 'opacity: 0.4; cursor: not-allowed; color: #999; background-color: #fafafa;';
            dayElement.title = 'Cannot select past dates';
        }
        else {
            if (modalSelectedDateUpdate && year === modalSelectedDateUpdate.getFullYear() &&
                month === modalSelectedDateUpdate.getMonth() && day === modalSelectedDateUpdate.getDate()) {
                dayElement.classList.add('selected');
            }
            dayElement.onclick = () => selectMiniDateUpdate(year, month, day);
            dayElement.style.cursor = 'pointer';
        }
        calendar.appendChild(dayElement);
    }

    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    document.getElementById('currentMonthMiniUpdate').textContent = `${monthNames[month]}, ${year}`;
}
function selectMiniDateUpdate(year, month, day) {
    const selectedDateObj = new Date(year, month, day);
    // SỬA LỖI 3: Sử dụng các biến ...Update
    const startDate = new Date(startTimeUpdate);
    const endDate = new Date(endTimeUpdate);
    const todayDate = new Date();
    todayDate.setHours(0, 0, 0, 0);
    selectedDateObj.setHours(0, 0, 0, 0);

    if (selectedDateObj < startDate || selectedDateObj > endDate) {
        alert('Selected date is not within allowed time range!');
        return;
    }
    if (selectedDateObj < todayDate) {
        alert('Cannot select past dates!');
        return;
    }

    // SỬA LỖI 3 & 4: Cập nhật đúng biến và (tạm thời) không cập nhật input ẩn vì chưa rõ ID
    modalSelectedDateUpdate = new Date(year, month, day);


    generateMiniCalendarUpdate();
}

function previousMonthMiniUpdate() {
    const newDate = new Date(modalCurrentDateUpdate);
    newDate.setMonth(newDate.getMonth() - 1);
    const startDate = new Date(startTimeUpdate);
    const firstDayOfNewMonth = new Date(newDate.getFullYear(), newDate.getMonth(), 1);

    if (firstDayOfNewMonth >= new Date(startDate.getFullYear(), startDate.getMonth(), 1)) {
        modalCurrentDateUpdate = newDate;
        generateMiniCalendarUpdate();
    }
}

function nextMonthMiniUpdate() {
    const newDate = new Date(modalCurrentDateUpdate);
    newDate.setMonth(newDate.getMonth() + 1);
    const endDate = new Date(endTimeUpdate);
    const lastDayOfNewMonth = new Date(newDate.getFullYear(), newDate.getMonth() + 1, 0);

    if (lastDayOfNewMonth <= new Date(endDate.getFullYear(), endDate.getMonth() + 1, 0)) {
        modalCurrentDateUpdate = newDate;
        generateMiniCalendarUpdate();
    }
}