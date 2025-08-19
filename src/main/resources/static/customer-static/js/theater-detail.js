function generateDates(selectedIndex = 0) {
    const dateSelector = document.getElementById('date-selector');
    dateSelector.innerHTML = '';
    const today = new Date();
    const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

    for (let i = 0; i < 14; i++) {
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
            const theaterId = parseInt(document.querySelector('#theaterId').value);
            const selectedIndex = parseInt(this.dataset.index);
            const roomType = document.querySelector('.custom-button-item.active span').innerText;
            dateItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            $.ajax({
                url: "/customer/theater-detail/loadSchedule",
                type: "get",
                data: {
                    theaterId: theaterId,
                    selectedIndex: selectedIndex,
                    roomType: roomType
                },
                success: function (data) {
                    document.querySelector('#book-theater-detail').innerHTML = data;

                    // Gọi lại generateDates để gắn active đúng ngày
                    generateDates(selectedIndex);
                    attachDateItemEvents();
                    attachRoomItemEvents();
                },
                error: function (e) {
                    alert("Lỗi khi tải lịch chiếu");
                }
            });
        });
    });
}

function attachRoomItemEvents() {
    const roomButtonItems = document.querySelectorAll('.custom-button-item');
    roomButtonItems.forEach(item => {
        item.addEventListener('click', () => {
            roomButtonItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            const theaterId = parseInt(document.querySelector('#theaterId').value);
            const selectedIndex = parseInt(document.querySelector('.date-item.active')?.dataset.index || 0);
            const roomType = document.querySelector('.custom-button-item.active span').innerText;

            $.ajax({
                url: "/customer/theater-detail/loadSchedule",
                type: "get",
                data: {
                    theaterId: theaterId,
                    selectedIndex: selectedIndex,
                    roomType: roomType
                },
                success: function (data) {
                    console.log(data);
                    document.querySelector('#book-theater-detail').innerHTML = data;

                    // Gọi lại generateDates để gắn active đúng ngày
                    generateDates(selectedIndex);
                    attachDateItemEvents();
                    attachRoomItemEvents();
                },
                error: function (e) {
                    alert("Lỗi khi tải lịch chiếu");
                }
            });
        });
    });
}

function showMap() {
    let lat = document.getElementById("theaterLat").value;
    let lng = document.getElementById("theaterLng").value;
    let placeName = document.querySelector(".theater-name").dataset.name;

    console.log("lat =", lat);
    console.log("lng =", lng);
    console.log("placeName =", placeName);

    mapboxgl.accessToken = 'pk.eyJ1IjoiYmx1ZWJlYXIxMjMiLCJhIjoiY21jcnQzYzVqMDZkNjJvczkyem5qcDYyNCJ9.4qcFRtXFMsoTuDCxr1aMyw';

    const map = new mapboxgl.Map({
        container: 'map',
        style: 'mapbox://styles/mapbox/streets-v12',
        center: [lng, lat],
        zoom: 17,
        pitch: 60,
        bearing: -20
    });

    map.on('style.load', () => {
        map.addLayer({
            id: '3d-buildings',
            source: 'composite',
            'source-layer': 'building',
            filter: ['==', 'extrude', 'true'],
            type: 'fill-extrusion',
            paint: {
                'fill-extrusion-color': '#aaa',
                'fill-extrusion-height': ['get', 'height'],
                'fill-extrusion-base': ['get', 'min_height'],
                'fill-extrusion-opacity': 0.6
            }
        });
    });

    new mapboxgl.Marker()
        .setLngLat([lng, lat])
        .setPopup(new mapboxgl.Popup().setText(placeName))
        .addTo(map);
}

document.addEventListener('DOMContentLoaded', attachDateItemEvents);
document.addEventListener('DOMContentLoaded', attachRoomItemEvents);
document.addEventListener('DOMContentLoaded', showMap);