🎬 Cinemax System
<div align="center"> <img src="https://img.shields.io/badge/Version-1.0.0-blue.svg" alt="Version"> <img src="https://img.shields.io/badge/Status-Active-green.svg" alt="Status"> <img src="https://img.shields.io/badge/Platform-Web-orange.svg" alt="Platform"> </div> 
📋 Mục lục
•	Giới thiệu
•	Kiến trúc hệ thống
•	Tính năng chính
•	Các thành phần hệ thống
•	Quy trình hoạt động
•	Cài đặt
•	Sử dụng
•	API Documentation
•	Đóng góp
🎯 Giới thiệu
Cinemax System là một hệ thống quản lý rạp chiếu phim toàn diện, được thiết kế để xử lý tất cả các hoạt động từ quản lý phim, đặt vé, thanh toán đến chăm sóc khách hàng. Hệ thống hỗ trợ nhiều loại người dùng với các quyền truy cập khác nhau và tích hợp với các dịch vụ bên ngoài.
🏗️ Kiến trúc hệ thống
Hệ thống được thiết kế theo mô hình MVC (Model-View-Controller) sử dụng Spring Boot với Thymeleaf template engine:
┌─────────────────────────────────────────────────────────┐
│                    CLIENT SIDE                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐│
│  │   HTML5     │ │    CSS3     │ │  JavaScript/AJAX    ││
│  └─────────────┘ └─────────────┘ └─────────────────────┘│
│  ┌─────────────────────────────────────────────────────┐│
│  │              Bootstrap Framework                    ││
│  └─────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
                            │
                     HTTP Requests
                            │
┌─────────────────────────────────────────────────────────┐
│                  SPRING BOOT APPLICATION                │
│  ┌─────────────────────────────────────────────────────┐│
│  │                 CONTROLLER LAYER                    ││
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐││
│  │  │AdminController│ │UserController│ │PaymentController│││
│  │  └─────────────┘ └─────────────┘ └─────────────────┘││
│  └─────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────┐│
│  │                 SERVICE LAYER                       ││
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐││
│  │  │MovieService │ │BookingService│ │PaymentService   │││
│  │  └─────────────┘ └─────────────┘ └─────────────────┘││
│  └─────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────┐│
│  │                REPOSITORY LAYER                     ││
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐││
│  │  │MovieRepository│ │UserRepository│ │BookingRepository│││
│  │  └─────────────┘ └─────────────┘ └─────────────────┘││
│  └─────────────────────────────────────────────────────┘│
│  ┌─────────────────────────────────────────────────────┐│
│  │                 VIEW LAYER                          ││
│  │            Thymeleaf Templates                      ││
│  └─────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────┘
                            │
                      JPA/Hibernate
                            │
┌─────────────────────────────────────────────────────────┐
│                    MySQL DATABASE                       │
└─────────────────────────────────────────────────────────┘

        ┌─────────────┐    ┌─────────────────┐
        │   VNPay     │    │ Google/Github   │
        │   Gateway   │    │    OAuth        │
        └─────────────┘    └─────────────────┘
Luồng xử lý request:
1.	Client gửi request qua trình duyệt (HTML/CSS/JS/Bootstrap)
2.	AJAX xử lý các request bất đồng bộ
3.	Spring Boot Controller nhận và xử lý request
4.	Service Layer thực hiện business logic
5.	Repository Layer tương tác với database qua JPA/Hibernate
6.	Thymeleaf render view và trả về response
✨ Tính năng chính
🎨 Bootstrap 5 Components
•	Responsive Design: Mobile-first approach
•	Navigation: Bootstrap navbar với dropdown menu
•	Forms: Styled forms với validation
•	Modals: Popup cho booking confirmation
•	Cards: Movie cards với hover effects
•	Carousel: Banner slider cho phim hot
•	Pagination: Phân trang danh sách phim
⚡ JavaScript & AJAX Features
•	Dynamic Loading: Load nội dung không reload trang
•	Form Validation: Client-side validation trước khi submit
•	Interactive Booking: Chọn ghế real-time
•	Payment Integration: AJAX calls tới VNPay
•	Search & Filter: Tìm kiếm phim theo thể loại, thời gian
•	Notifications: Toast messages cho user feedback
🎭 Thymeleaf Integration
•	Template Fragments: Header, footer, sidebar tái sử dụng
•	Conditional Rendering: Hiển thị content theo role
•	Form Binding: Two-way data binding với Spring models
•	Internationalization: Đa ngôn ngữ (Việt Nam, English)
•	Security Integration: Thymeleaf Security extras
🔐 Quản lý người dùng
•	Admin: Quản lý toàn hệ thống (RBAC, voucher, tài khoản, blog)
•	Guest: Đăng ký tài khoản, xem thông tin phim và rạp
•	Customer: Đặt vé, thanh toán, xem lịch sử, đánh giá phim
•	Staff: Quản lý phim, lịch chiếu, thực phẩm đồ uống
•	Cashier: Xử lý thanh toán, tạo hóa đơn, quản lý voucher
🎬 Quản lý nội dung
•	Quản lý thông tin phim và lịch chiếu
•	Quản lý phòng chiếu và ghế ngồi
•	Hệ thống blog và tin tức
•	Quản lý voucher và khuyến mãi
💳 Thanh toán và dịch vụ
•	Tích hợp VNPay cho thanh toán online
•	Xử lý thanh toán tại quầy
•	Quản lý hóa đơn và báo cáo
•	Hệ thống feedback khách hàng
🤖 Dịch vụ hỗ trợ
•	Chatbot tự động
•	Tích hợp Google/Github Gateway
•	Hệ thống thông báo và email
🔧 Các thành phần hệ thống
Core Components
Component	Mô tả	Chức năng chính
Cinemax System	Hệ thống trung tâm	Điều phối tất cả các hoạt động
Authentication	Xác thực và phân quyền	Quản lý đăng nhập, phân quyền người dùng
Database	Cơ sở dữ liệu	Lưu trữ tất cả thông tin hệ thống
User Interfaces
Interface	Người dùng	Tính năng
Admin Panel	Admin	RBAC, quản lý voucher, tài khoản, blog
Customer Portal	Customer/Guest	Đặt vé, xem phim, thanh toán, feedback
Staff Dashboard	Staff	Quản lý phim, lịch chiếu, F&B
Cashier System	Cashier	Thanh toán, hóa đơn, voucher
External Integrations
Service	Mục đích	Kết nối
VNPay	Thanh toán online	Payment gateway
Chatbot	Hỗ trợ khách hàng	Tự động hóa customer service
Google/Github	Đăng nhập xã hội	OAuth integration
🔄 Quy trình hoạt động
Quy trình đặt vé
graph TD
    A[Customer đăng nhập] --> B[Chọn phim và suất chiếu]
    B --> C[Chọn ghế ngồi]
    C --> D[Thêm F&B (tuỳ chọn)]
    D --> E[Áp dụng voucher (tuỳ chọn)]
    E --> F[Chọn phương thức thanh toán]
    F --> G{Thanh toán online?}
    G -->|Có| H[VNPay Gateway]
    G -->|Không| I[Thanh toán tại quầy]
    H --> J[Xác nhận thanh toán]
    I --> J
    J --> K[Tạo vé và gửi email]
Quy trình quản lý
graph TD
    A[Admin/Staff đăng nhập] --> B{Loại tác vụ?}
    B -->|Quản lý phim| C[Thêm/sửa/xóa phim]
    B -->|Quản lý lịch chiếu| D[Tạo/cập nhật lịch chiếu]
    B -->|Quản lý voucher| E[Tạo/quản lý voucher]
    B -->|Báo cáo| F[Xem báo cáo doanh thu]
    C --> G[Cập nhật database]
    D --> G
    E --> G
    F --> G
🚀 Cài đặt
Tech Stack
•	Backend Framework: Spring Boot 2.x+
•	Frontend: Thymeleaf, Bootstrap 5, HTML5, CSS3, JavaScript
•	AJAX: jQuery AJAX cho xử lý bất đồng bộ
•	Database: MySQL 8.0+
•	Payment Gateway: VNPay API
•	Authentication: Spring Security, OAuth 2.0
•	Build Tool: Maven/Gradle
•	Java Version: JDK 8+
Các bước cài đặt
1.	Clone repository
2.	git clone https://github.com/your-repo/cinemax-system.git
3.	cd cinemax-system
4.	Cài đặt Java và Maven
5.	# Kiểm tra Java version (cần JDK 8+)
6.	java -version
7.	
8.	# Kiểm tra Maven
9.	mvn -version
10.	Cấu hình database
11.	# Tạo database MySQL
12.	mysql -u root -p
13.	CREATE DATABASE cinemax;
14.	CREATE USER 'cinemax_user'@'localhost' IDENTIFIED BY 'password';
15.	GRANT ALL PRIVILEGES ON cinemax.* TO 'cinemax_user'@'localhost';
16.	FLUSH PRIVILEGES;
17.	Cấu hình application.properties
18.	# Database Configuration
19.	spring.datasource.url=jdbc:mysql://localhost:3306/cinemax
20.	spring.datasource.username=cinemax_user
21.	spring.datasource.password=password
22.	spring.jpa.hibernate.ddl-auto=update
23.	
24.	# Thymeleaf Configuration
25.	spring.thymeleaf.cache=false
26.	spring.thymeleaf.prefix=classpath:/templates/
27.	spring.thymeleaf.suffix=.html
28.	
29.	# VNPay Configuration
30.	vnpay.payUrl=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
31.	vnpay.tmnCode=YOUR_TMN_CODE
32.	vnpay.secretKey=YOUR_SECRET_KEY
33.	Build và chạy ứng dụng
34.	# Build project
35.	mvn clean install
36.	
37.	# Chạy ứng dụng
38.	mvn spring-boot:run
39.	
40.	# Hoặc chạy từ JAR file
41.	java -jar target/cinemax-system-1.0.0.jar
42.	Truy cập ứng dụng
o	Mở trình duyệt và truy cập: http://localhost:8080
o	Admin panel: http://localhost:8080/admin
📖 Sử dụng
Đăng nhập hệ thống
Admin:     admin@cinemax.com / admin123
Staff:     staff@cinemax.com / staff123
Cashier:   cashier@cinemax.com / cashier123
Customer:  Đăng ký tài khoản mới hoặc dùng Google/Github
Các chức năng chính
👤 Dành cho Customer
•	Xem phim: Duyệt danh sách phim, xem trailer, đọc review
•	Đặt vé: Chọn suất chiếu, ghế ngồi, thanh toán
•	Quản lý tài khoản: Xem lịch sử đặt vé, cập nhật thông tin
•	Feedback: Đánh giá phim, góp ý dịch vụ
🛠️ Dành cho Staff
•	Quản lý phim: Thêm phim mới, cập nhật thông tin
•	Lịch chiếu: Tạo và quản lý lịch chiếu
•	F&B: Quản lý thực phẩm và đồ uống
•	Báo cáo: Xem thống kê doanh thu
💰 Dành cho Cashier
•	Bán vé tại quầy: Xử lý giao dịch trực tiếp
•	Thanh toán: Nhận thanh toán tiền mặt/thẻ
•	In hóa đơn: Tạo và in hóa đơn
•	Voucher: Áp dụng và quản lý voucher
⚙️ Dành cho Admin
•	RBAC: Quản lý vai trò và quyền truy cập
•	Quản lý người dùng: Tạo, sửa, xóa tài khoản
•	Voucher: Tạo và quản lý chương trình khuyến mãi
•	Blog: Quản lý nội dung blog và tin tức
📚 API Documentation
Spring Boot REST Controllers
Authentication Endpoints
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    @PostMapping("/register") 
    @PostMapping("/logout")
    @GetMapping("/profile")
}
Movie Management
@RestController
@RequestMapping("/api/movies")
public class MovieController {
    @GetMapping("")                    // Lấy danh sách phim
    @GetMapping("/{id}")               // Lấy thông tin phim
    @PostMapping("")                   // Thêm phim mới (Staff/Admin)
    @PutMapping("/{id}")               // Cập nhật phim (Staff/Admin)
    @DeleteMapping("/{id}")            // Xóa phim (Admin only)
}
Booking System
@RestController  
@RequestMapping("/api/bookings")
public class BookingController {
    @GetMapping("/showings")           // Lấy lịch chiếu
    @PostMapping("")                   // Tạo booking mới
    @GetMapping("/{id}")               // Xem chi tiết booking
    @PutMapping("/{id}")               // Cập nhật booking
}
Payment Integration
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @PostMapping("/vnpay/create")      // Tạo payment VNPay
    @GetMapping("/vnpay/return")       // Xử lý VNPay callback
    @PostMapping("/cash")              // Thanh toán tiền mặt (Cashier)
}
Admin Functions
@RestController
@RequestMapping("/api/admin") 
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @GetMapping("/users")              // Quản lý users
    @PostMapping("/vouchers")          // Tạo voucher
    @GetMapping("/reports")            // Báo cáo thống kê
    @PutMapping("/rbac")               // Cấu hình RBAC
}
Thymeleaf Templates Structure
src/main/resources/templates/
├── admin/
│   ├── dashboard.html
│   ├── users.html
│   ├── movies.html
│   └── reports.html
├── customer/
│   ├── home.html
│   ├── movies.html
│   ├── booking.html
│   └── profile.html
├── staff/
│   ├── dashboard.html
│   ├── schedule.html
│   └── inventory.html
├── cashier/
│   ├── pos.html
│   └── transactions.html
├── fragments/
│   ├── header.html
│   ├── footer.html
│   └── sidebar.html
└── error/
    ├── 404.html
    └── 500.html
AJAX Integration Examples
// Đặt vé với AJAX
function bookTicket(movieId, seatIds) {
    $.ajax({
        url: '/api/bookings',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            movieId: movieId,
            seatIds: seatIds
        }),
        success: function(response) {
            window.location.href = '/payment/' + response.bookingId;
        },
        error: function(xhr, status, error) {
            alert('Đặt vé thất bại: ' + error);
        }
    });
}

// Load danh sách phim động
function loadMovies(page = 0) {
    $.get('/api/movies?page=' + page, function(data) {
        $('#movie-list').html('');
        data.content.forEach(function(movie) {
            $('#movie-list').append(createMovieCard(movie));
        });
    });
}
🤝 Đóng góp
Chúng tôi hoan nghênh mọi đóng góp cho dự án!
Quy trình đóng góp
1.	Fork repository
2.	Tạo feature branch (git checkout -b feature/AmazingFeature)
3.	Commit changes (git commit -m 'Add some AmazingFeature')
4.	Push to branch (git push origin feature/AmazingFeature)
5.	Tạo Pull Request
Coding Standards
•	Sử dụng Spring Boot best practices
•	Java Code Style: Google Java Style Guide
•	Thymeleaf: Fragment templates và layout dialect
•	JavaScript: ES6+ syntax, jQuery cho AJAX
•	CSS: Bootstrap 5 classes + custom CSS
•	Viết unit tests với JUnit 5 và Mockito
•	Integration tests với @SpringBootTest
•	Tuân thủ RESTful API conventions
Bug Reports
Khi báo cáo bug, vui lòng bao gồm:
•	Mô tả chi tiết vấn đề
•	Các bước tái tạo bug
•	Môi trường (OS, browser, version)
•	Screenshots nếu có thể
📞 Liên hệ & Hỗ trợ
•	Email: support@cinemax.com
•	Documentation: Wiki
•	Issues: GitHub Issues
•	Discussions: GitHub Discussions

________________________________________
<div align="center"> <p>Được phát triển với ❤️ bởi Cinemax Team</p> <p>© 2024 Cinemax System. All rights reserved.</p> </div>

