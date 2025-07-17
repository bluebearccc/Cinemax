// ==================================================================================
// PHẦN CODE CHUNG (KHÔNG ĐỔI)
// ==================================================================================
const scheduleData = serverData.schedules;
const startTime = serverData.startTime;
const endTime = serverData.endTime;
let currentDate = new Date();
let selectedDate = null;
let modalCurrentDate = new Date();
let modalSelectedDate = null;

function clearScheduleInputs() {
    $('#startTime').val('');
    $('#theater').val('').prop('disabled', true);
    $('#room-list-container').html('');
}

function generateCalendar() {
    const calendar = document.getElementById('calendar');
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    const existingDays = calendar.querySelectorAll('.calendar-day');
    existingDays.forEach(day => day.remove());
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const adjustedFirstDay = (firstDay === 0) ? 6 : firstDay - 1;
    for (let i = 0; i < adjustedFirstDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'calendar-day';
        emptyDay.style.opacity = '0.3';
        calendar.appendChild(emptyDay);
    }
    for (let day = 1; day <= daysInMonth; day++) {
        const dayElement = document.createElement('div');
        dayElement.className = 'calendar-day';
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        const scheduleCount = scheduleData[dateStr] ? scheduleData[dateStr].length : 0;
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
    const monthNames = ['January ', 'February ', 'March ', 'April ', 'May ', 'June ', 'July ', 'August ', 'September ', 'October ', 'November ', 'December '];
    document.getElementById('currentMonth').textContent = `${monthNames[month]}, ${year}`;
}

function selectDate(year, month, day) {
    selectedDate = new Date(year, month, day);
    generateCalendar();
    updateDailySchedule();
}

function groupSchedulesByTimeSlot(schedules) {
    const timeSlots = {};
    schedules.forEach(schedule => {
        const timeKey = `${schedule.time} ~ ${schedule.endTime}`;
        if (!timeSlots[timeKey]) {
            timeSlots[timeKey] = [];
        }
        timeSlots[timeKey].push(schedule);
    });
    return timeSlots;
}

let currentlyOpenTimeSlotId = null;

function toggleScheduleDetails(timeSlotId) {
    const cardsContainer = document.getElementById(timeSlotId);
    if (!cardsContainer) return;
    const isCurrentlyOpen = timeSlotId === currentlyOpenTimeSlotId;
    if (currentlyOpenTimeSlotId && currentlyOpenTimeSlotId !== timeSlotId) {
        closeScheduleDetails(currentlyOpenTimeSlotId);
    }
    if (isCurrentlyOpen) {
        closeScheduleDetails(timeSlotId);
        currentlyOpenTimeSlotId = null;
    } else {
        openScheduleDetails(timeSlotId);
        currentlyOpenTimeSlotId = timeSlotId;
    }
}

function openScheduleDetails(timeSlotId) {
    const cardsContainer = document.getElementById(timeSlotId);
    if (!cardsContainer) return;
    cardsContainer.style.display = 'flex';
    cardsContainer.style.opacity = '0';
    cardsContainer.style.transform = 'translateY(-10px) scaleY(0.95)';
    requestAnimationFrame(() => {
        cardsContainer.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
        cardsContainer.style.opacity = '1';
        cardsContainer.style.transform = 'translateY(0) scaleY(1)';
    });
}

function closeScheduleDetails(timeSlotId) {
    const cardsContainer = document.getElementById(timeSlotId);
    if (!cardsContainer) return;
    cardsContainer.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
    cardsContainer.style.opacity = '0';
    cardsContainer.style.transform = 'translateY(-10px) scaleY(0.95)';
    setTimeout(() => {
        cardsContainer.style.display = 'none';
    }, 300);
}

function closeAllScheduleDetails() {
    if (currentlyOpenTimeSlotId) {
        closeScheduleDetails(currentlyOpenTimeSlotId);
        currentlyOpenTimeSlotId = null;
    }
}

function updateDailySchedule() {
    closeAllScheduleDetails();
    const scheduleList = document.querySelector('.schedule-list');
    const scheduleDateElement = document.querySelector('.schedule-date');
    if (!selectedDate) {
        scheduleDateElement.textContent = 'Please select a date to view schedule slot';
        scheduleList.innerHTML = '<div class="no-schedule">Select a date to view schedule slot</div>';
        return;
    }
    const dateStr = `${selectedDate.getFullYear()}-${String(selectedDate.getMonth() + 1).padStart(2, '0')}-${String(selectedDate.getDate()).padStart(2, '0')}`;
    const schedules = scheduleData[dateStr] || [];
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    const monthNames = ['January ', 'February ', 'March ', 'April ', 'May ', 'June ', 'July ', 'August ', 'September ', 'October ', 'November ', 'December '];
    const dayName = dayNames[selectedDate.getDay()];
    const day = selectedDate.getDate();
    const month = monthNames[selectedDate.getMonth()];
    const year = selectedDate.getFullYear();
    scheduleDateElement.textContent = `${dayName}, ${String(day).padStart(2, '0')} ${month}, ${year}`;
    if (schedules.length === 0) {
        scheduleList.innerHTML = '<div class="no-schedule">No schedule</div>';
    } else {
        const timeSlots = groupSchedulesByTimeSlot(schedules);
        let timeSlotsHTML = '';
        Object.keys(timeSlots).forEach(timeSlot => {
            const schedulesInSlot = timeSlots[timeSlot];
            const timeSlotId = `timeSlot-${dateStr}-${timeSlot.replace(/[^a-zA-Z0-9]/g, '')}`;
            timeSlotsHTML += `
                <div class="schedule-time-slot" onclick="toggleScheduleDetails('${timeSlotId}')">
                    <div class="time-display">${timeSlot}</div>
                </div>
                <div class="schedule-cards-container" id="${timeSlotId}" style="display: none;">
                    ${schedulesInSlot.map(schedule => `
                        <div class="schedule-card" onclick="editSchedule('${schedule.id}')">
                           <div class="schedule-card-time">${schedule.time} ~ ${schedule.endTime}</div>
                           <div class="schedule-card-theater">🏢 <span>${schedule.theater}</span></div>
                           <div class="schedule-card-room">🎬 <span>${schedule.room}</span></div>
                        </div>
                    `).join('')}
                </div>
            `;
        });
        scheduleList.innerHTML = timeSlotsHTML;
    }
}

function previousMonth() { currentDate.setMonth(currentDate.getMonth() - 1); generateCalendar(); }
function nextMonth() { currentDate.setMonth(currentDate.getMonth() + 1); generateCalendar(); }
function addNewSchedule() {
    document.getElementById('scheduleModal').classList.add('show');
    const today = new Date();
    const startDate = new Date(startTime);
    const endDate = new Date(endTime);
    if (today < startDate) { modalCurrentDate = new Date(startDate); }
    else if (today > endDate) { modalCurrentDate = new Date(endDate); }
    else { modalCurrentDate = new Date(today); }
    generateMiniCalendar();
}
function closeModal() { document.getElementById('scheduleModal').classList.remove('show'); }

function generateMiniCalendar() {
    const calendar = document.getElementById('miniCalendar');
    const year = modalCurrentDate.getFullYear();
    const month = modalCurrentDate.getMonth();
    const startDate = new Date(startTime);
    const endDate = new Date(endTime);
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
        const isInValidRange = currentDayDate >= startDate && currentDayDate <= endDate;
        const isPastDate = currentDayDate < todayDate;
        if (!isInValidRange) {
            dayElement.classList.add('disabled');
            dayElement.style.cssText = 'opacity: 0.3; cursor: not-allowed; background-color: #f5f5f5; color: #ccc;';
            dayElement.title = 'Out of range date';
        } else if (isPastDate) {
            dayElement.classList.add('past-date');
            dayElement.style.cssText = 'opacity: 0.4; cursor: not-allowed; color: #999; background-color: #fafafa;';
            dayElement.title = 'Cannot select past dates';
        } else {
            if (modalSelectedDate && year === modalSelectedDate.getFullYear() && month === modalSelectedDate.getMonth() && day === modalSelectedDate.getDate()) {
                dayElement.classList.add('selected');
            }
            dayElement.onclick = () => selectMiniDate(year, month, day);
            dayElement.style.cursor = 'pointer';
        }
        calendar.appendChild(dayElement);
    }
    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
    document.getElementById('currentMonthMini').textContent = `${monthNames[month]}, ${year}`;
}

function selectMiniDate(year, month, day) {
    const selectedDateObj = new Date(year, month, day);
    const startDate = new Date(startTime);
    const endDate = new Date(endTime);
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
    modalSelectedDate = new Date(year, month, day);
    const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
    document.getElementById('selectedDateInput').value = dateStr;
    clearScheduleInputs();
    $('#selectedDateInput').trigger('change');
    generateMiniCalendar();
}

function previousMonthMini() {
    const newDate = new Date(modalCurrentDate);
    newDate.setMonth(newDate.getMonth() - 1);
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
    const endDate = new Date(endTime);
    const lastDayOfNewMonth = new Date(newDate.getFullYear(), newDate.getMonth() + 1, 0);
    if (lastDayOfNewMonth <= new Date(endDate.getFullYear(), endDate.getMonth() + 1, 0)) {
        modalCurrentDate = newDate;
        generateMiniCalendar();
    }
}

document.getElementById('scheduleModal').addEventListener('click', function(e) { if (e.target === this) { closeModal(); } });

function editSchedule(id) {
    addNewUpdateSchedule();

    $('#saveChangesBtn').prop('disabled', false);
    $('#deleteScheduleBtn').prop('disabled', false);
    $('#editScheduleForm :input').prop('disabled', false);


    $('#scheduleIDUpdate').val(id);

    // 4. Gọi AJAX để lấy thông tin chi tiết
    $.ajax({
        type: 'GET',
        url: '/staff/movie_schedule/get_schedule_details',
        data: {
            scheduleID: id
        },
        success: function(response) {
            if (response.success) {
                // Điền thông tin cơ bản vào form
                $('#startTimeUpdate').val(response.startTime);
                $('#dateUpdateInput').val(response.date);
                $('#theaterNameUpdate').val(response.theaterId);

                if (response.status === "Active") {
                    $('#statusActive').prop('checked', true);
                } else if (response.status === "Inactive") {
                    $('#statusInactive').prop('checked', true);
                }

                // Cập nhật lịch mini
                const dateParts = response.date.split('-');
                originalScheduleDate = new Date(parseInt(dateParts[0]), parseInt(dateParts[1]) - 1, parseInt(dateParts[2]));
                modalCurrentDateUpdate = new Date(originalScheduleDate);
                modalSelectedDateUpdate = new Date(originalScheduleDate);
                generateMiniCalendarUpdate();

                // 5. Kích hoạt sự kiện change để load danh sách phòng lần đầu
                $('#theaterNameUpdate').trigger('change');

                // 6. KIỂM TRA VÀ VÔ HIỆU HÓA FORM NẾU CẦN
                if (response.isExisted) {
                    $('#editScheduleMessage').text('This schedule cannot be edited or deleted because it has associated bookings.').show();
                    $('#saveChangesBtn').prop('disabled', true);
                    $('#deleteScheduleBtn').prop('disabled', true);
                    $('#editScheduleForm :input').not('.btn-cancel').prop('disabled', true);
                }

            } else {
                alert('Failed to load schedule details: ' + response.message);
            }
        },
        error: function() {
            alert('Error fetching schedule details.');
        }
    });
}

$(function(){

    $('#theaterNameUpdate, #startTimeUpdate, #dateUpdateInput').on('change input', function() {
        const theaterId = $('#theaterNameUpdate').val();
        console.log('theaterId:', theaterId);

        const startTime = $('#startTimeUpdate').val();
        console.log('startTime:', startTime);

        const date = $('#dateUpdateInput').val();
        console.log('date:', date);

        const movieId = $('#movieIDUpdate').val();
        console.log('movieId:', movieId);

        const scheduleId = $('#scheduleIDUpdate').val();
        console.log('scheduleId:', scheduleId);

        if (theaterId && startTime && date && movieId && scheduleId) {
            $.ajax({
                type: 'GET',
                url: '/staff/movie_schedule/get_rooms_for_edit',
                data: {
                    theaterId: theaterId,
                    startTime: startTime,
                    date: date,
                    movieId: movieId,
                    scheduleId: scheduleId
                },
                success: function(fragment) {
                    $('#edit-room-list-container').html(fragment);
                },
                error: function() {
                    $('#edit-room-list-container').html('<p>Error loading rooms.</p>');
                }
            });
        } else {
            $('#edit-room-list-container').html('');
        }
    });
});

generateCalendar();
updateDailySchedule();