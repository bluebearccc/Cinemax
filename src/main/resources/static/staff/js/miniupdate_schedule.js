const startTimeUpdate = serverData.startTime;
const endTimeUpdate = serverData.endTime;

let modalCurrentDateUpdate = new Date();
let modalSelectedDateUpdate = null;
let originalScheduleDate = null;

function addNewUpdateSchedule() {
    document.getElementById('editScheduleModal').classList.add('show');

    const today = new Date();
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

    // Vô hiệu hóa các nút điều hướng và lịch mini khi modal mở
    $('.nav-btn-mini').addClass('disabled-btn').off('click'); // Vô hiệu hóa nút
    $('#miniCalendarUpdate').css({'pointer-events': 'none', 'opacity': '0.7'}); // Vô hiệu hóa lịch
}

function closeEditModal() {
    document.getElementById('editScheduleModal').classList.remove('show');
    modalSelectedDateUpdate = null;
    originalScheduleDate = null;

    // Đảm bảo loại bỏ các trạng thái vô hiệu hóa khi đóng modal (nếu các nút này được dùng ở nơi khác)
    $('.nav-btn-mini').removeClass('disabled-btn').on('click', function() { /* re-add original click handler if needed */ });
    $('#miniCalendarUpdate').css({'pointer-events': 'auto', 'opacity': '1'});
}

function generateMiniCalendarUpdate() {
    const calendar = document.getElementById('miniCalendarUpdate');
    const year = modalCurrentDateUpdate.getFullYear();
    const month = modalCurrentDateUpdate.getMonth();

    const startDate = new Date(startTimeUpdate);
    const endDate = new Date(endTimeUpdate);
    startDate.setHours(0, 0, 0, 0);
    endDate.setHours(0, 0, 0, 0);

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
    const adjustedFirstDay = (firstDay === 0) ? 6 : firstDay - 1;

    for (let i = 0; i < adjustedFirstDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'mini-calendar-day empty';
        emptyDay.style.opacity = '0.3';
        calendar.appendChild(emptyDay);
    }

    for (let day = 1; day <= daysInMonth; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'mini-calendar-day';
        dayElement.textContent = day;

        const currentDayDate = new Date(year, month, day);
        currentDayDate.setHours(0, 0, 0, 0);

        const todayDate = new Date();
        todayDate.setHours(0, 0, 0, 0);

        const isInValidMovieRange = currentDayDate >= startDate && currentDayDate <= endDate;
        const isOriginalScheduleDate = originalScheduleDate &&
            currentDayDate.getFullYear() === originalScheduleDate.getFullYear() &&
            currentDayDate.getMonth() === originalScheduleDate.getMonth() &&
            currentDayDate.getDate() === originalScheduleDate.getDate();

        // Chỉ đánh dấu là 'selected' nếu là ngày lịch trình cũ
        if (modalSelectedDateUpdate && year === modalSelectedDateUpdate.getFullYear() &&
            month === modalSelectedDateUpdate.getMonth() && day === modalSelectedDateUpdate.getDate()) {
            dayElement.classList.add('selected');
        }

        // Vô hiệu hóa tất cả các ngày trong lịch mini của modal chỉnh sửa
        dayElement.classList.add('disabled');
        dayElement.style.opacity = '0.5';
        dayElement.style.cursor = 'not-allowed';
        dayElement.style.backgroundColor = '#f5f5f5';
        dayElement.style.color = '#ccc';
        dayElement.title = 'Date cannot be changed in edit mode.';
        // Không thêm onclick cho bất kỳ ngày nào
        calendar.appendChild(dayElement);
    }

    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];
    document.getElementById('currentMonthMiniUpdate').textContent = `${monthNames[month]}, ${year}`;
}

// Các hàm selectMiniDateUpdate, previousMonthMiniUpdate, nextMonthMiniUpdate
// sẽ không được gọi hoặc sẽ không có tác dụng do các ngày bị disabled và không có onclick
// Bạn có thể giữ chúng hoặc xóa chúng nếu bạn chắc chắn không bao giờ muốn tương tác với lịch.
// Tuy nhiên, vì các nút điều hướng cũng đã bị vô hiệu hóa, việc gọi chúng sẽ không xảy ra qua UI.
function selectMiniDateUpdate(year, month, day) {
    // Không cho phép chọn ngày
    alert('Date cannot be changed in edit mode!');
    return;
}

function previousMonthMiniUpdate() {
    // Không cho phép thay đổi tháng
    console.log("Cannot change month in edit mode.");
    return;
}

function nextMonthMiniUpdate() {
    // Không cho phép thay đổi tháng
    console.log("Cannot change month in edit mode.");
    return;
}