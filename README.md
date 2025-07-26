Chắc chắn rồi! Bạn muốn tôi lưu nội dung này vào một file tài liệu (.doc hoặc .docx) nhưng là định dạng văn bản thô. Vì tôi là một mô hình ngôn ngữ và không thể trực tiếp tạo hay xuất file .doc hay .docx được định dạng.

Tuy nhiên, tôi có thể cung cấp cho bạn toàn bộ nội dung đã định dạng Markdown trong một khối văn bản. Bạn có thể dễ dàng copy nội dung này và paste vào một trình soạn thảo văn bản như Microsoft Word, Google Docs, hoặc bất kỳ trình soạn thảo nào hỗ trợ Markdown. Sau đó, bạn có thể lưu nó dưới dạng .doc hoặc .docx và chỉnh sửa thêm nếu cần.

Dưới đây là nội dung đầy đủ đã được chỉnh sửa theo chuẩn Markdown mà bạn có thể dán vào file của mình:

Markdown

# 🎬 Cinemax System

<div align="center">
    <img src="https://img.shields.io/badge/Version-1.0.0-blue.svg" alt="Version">
    <img src="https://img.shields.io/badge/Status-Active-green.svg" alt="Status">
    <img src="https://img.shields.io/badge/Platform-Web-orange.svg" alt="Platform">
</div>

---

## 📋 Mục lục

* [🎯 Giới thiệu](#-giới-thiệu)
* [🏗️ Kiến trúc hệ thống](#️-kiến-trúc-hệ-thống)
* [✨ Tính năng chính](#-tính-năng-chính)
    * [🎨 Bootstrap 5 Components](#-bootstrap-5-components)
    * [⚡ JavaScript & AJAX Features](#-javascript--ajax-features)
    * [🎭 Thymeleaf Integration](#-thymeleaf-integration)
    * [🔐 Quản lý người dùng](#-quản-lý-người-dùng)
    * [🎬 Quản lý nội dung](#-quản-lý-nội-dung)
    * [💳 Thanh toán và dịch vụ](#-thanh-toán-và-dịch-vụ)
    * [🤖 Dịch vụ hỗ trợ](#-dịch-vụ-hỗ-trợ)
* [🔧 Các thành phần hệ thống](#-các-thành-phần-hệ-thống)
* [🔄 Quy trình hoạt động](#-quy-trình-hoạt-động)
    * [Quy trình đặt vé](#quy-trình-đặt-vé)
    * [Quy trình quản lý](#quy-trình-quản-lý)
* [🚀 Cài đặt](#-cài-đặt)
    * [Tech Stack](#tech-stack)
    * [Các bước cài đặt](#các-bước-cài-đặt)
* [📖 Sử dụng](#-sử-dụng)
    * [Đăng nhập hệ thống](#đăng-nhập-hệ-thống)
    * [Các chức năng chính](#các-chức-năng-chính)
* [📚 API Documentation](#-api-documentation)
    * [Spring Boot REST Controllers](#spring-boot-rest-controllers)
    * [Thymeleaf Templates Structure](#thymeleaf-templates-structure)
    * [AJAX Integration Examples](#ajax-integration-examples)
* [🤝 Đóng góp](#-đóng-góp)
    * [Quy trình đóng góp](#quy-trình-đóng-góp)
    * [Coding Standards](#coding-standards)
    * [Bug Reports](#bug-reports)
* [📞 Liên hệ & Hỗ trợ](#-liên-hệ--hỗ-trợ)
* [📄 License](#-license)

---

## 🎯 Giới thiệu

Cinemax System là một hệ thống quản lý rạp chiếu phim toàn diện, được thiết kế để xử lý tất cả các hoạt động từ quản lý phim, đặt vé, thanh toán đến chăm sóc khách hàng. Hệ thống hỗ trợ nhiều loại người dùng với các quyền truy cập khác nhau và tích hợp với các dịch vụ bên ngoài.

## 🏗️ Kiến trúc hệ thống

Hệ thống được thiết kế theo mô hình **MVC (Model-View-Controller)** sử dụng **Spring Boot** với **Thymeleaf template engine**.

┌─────────────────────────────────────────────────────────┐
│                     CLIENT SIDE                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────────────┐│
│  │    HTML5    │ │    CSS3     │ │  JavaScript/AJAX    ││
│  └─────────────┘ └─────────────┘ └─────────────────────┘│
│ ┌─────────────────────────────────────────────────────┐ │
│ │            Bootstrap Framework                      │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
│
│ HTTP Requests
│
┌─────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATION                    │
│ ┌─────────────────────────────────────────────────────┐ │
│ │                CONTROLLER LAYER                     │ │
│ │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐││
│ │  │AdminController│ │UserController│ │PaymentController││
│ │  └─────────────┘ └─────────────┘ └─────────────────┘││
│ └─────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │                  SERVICE LAYER                      │ │
│ │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐││
│ │  │MovieService │ │BookingService│ │PaymentService   │││
│ │  └─────────────┘ └─────────────┘ └─────────────────┘││
│ └─────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │                REPOSITORY LAYER                     │ │
│ │  ┌─────────────┐ ┌─────────────┐ ┌─────────────────┐││
│ │  │MovieRepository│ │UserRepository│ │BookingRepository││
│ │  └─────────────┘ └─────────────┘ └─────────────────┘││
│ └─────────────────────────────────────────────────────┘ │
│ ┌─────────────────────────────────────────────────────┐ │
│ │                   VIEW LAYER                        │ │
│ │             Thymeleaf Templates                     │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
│
│ JPA/Hibernate
│
┌─────────────────────────────────────────────────────────┐
│                  MySQL DATABASE                         │
└─────────────────────────────────────────────────────────┘

    ┌─────────────┐       ┌─────────────────┐
    │    VNPay    │       │ Google/Github   │
    │    Gateway  │       │     OAuth       │
    └─────────────┘       └─────────────────┘

**Luồng xử lý request:**

1.  **Client** gửi request qua trình duyệt (HTML/CSS/JS/Bootstrap).
2.  **AJAX** xử lý các request bất đồng bộ.
3.  **Spring Boot Controller** nhận và xử lý request.
4.  **Service Layer** thực hiện business logic.
5.  **Repository Layer** tương tác với database qua JPA/Hibernate.
6.  **Thymeleaf** render view và trả về response.

## ✨ Tính năng chính

### 🎨 Bootstrap 5 Components

* **Responsive Design**: Mobile-first approach.
* **Navigation**: Bootstrap navbar với dropdown menu.
* **Forms**: Styled forms với validation.
* **Modals**: Popup cho booking confirmation.
* **Cards**: Movie cards với hover effects.
* **Carousel**: Banner slider cho phim hot.
* **Pagination**: Phân trang danh sách phim.

### ⚡ JavaScript & AJAX Features

* **Dynamic Loading**: Load nội dung không reload trang.
* **Form Validation**: Client-side validation trước khi submit.
* **Interactive Booking**: Chọn ghế real-time.
* **Payment Integration**: AJAX calls tới VNPay.
* **Search & Filter**: Tìm kiếm phim theo thể loại, thời gian.
* **Notifications**: Toast messages cho user feedback.

### 🎭 Thymeleaf Integration

* **Template Fragments**: Header, footer, sidebar tái sử dụng.
* **Conditional Rendering**: Hiển thị content theo role.
* **Form Binding**: Two-way data binding với Spring models.
* **Internationalization**: Đa ngôn ngữ (Việt Nam, English).
* **Security Integration**: Thymeleaf Security extras.

### 🔐 Quản lý người dùng

* **Admin**: Quản lý toàn hệ thống (RBAC, voucher, tài khoản, blog).
* **Guest**: Đăng ký tài khoản, xem thông tin phim và rạp.
* **Customer**: Đặt vé, thanh toán, xem lịch sử, đánh giá phim.
* **Staff**: Quản lý phim, lịch chiếu, thực phẩm đồ uống.
* **Cashier**: Xử lý thanh toán, tạo hóa đơn, quản lý voucher.

### 🎬 Quản lý nội dung

* Quản lý thông tin phim và lịch chiếu.
* Quản lý phòng chiếu và ghế ngồi.
* Hệ thống blog và tin tức.
* Quản lý voucher và khuyến mãi.

### 💳 Thanh toán và dịch vụ

* Tích hợp VNPay cho thanh toán online.
* Xử lý thanh toán tại quầy.
* Quản lý hóa đơn và báo cáo.
* Hệ thống feedback khách hàng.

### 🤖 Dịch vụ hỗ trợ

* Chatbot tự động.
* Tích hợp Google/Github Gateway.
* Hệ thống thông báo và email.

## 🔧 Các thành phần hệ thống

### Core Components

| Component      | Mô tả            | Chức năng chính                     |
| :------------- | :--------------- | :--------------------------------- |
| Cinemax System | Hệ thống trung tâm | Điều phối tất cả các hoạt động       |
| Authentication | Xác thực và phân quyền | Quản lý đăng nhập, phân quyền người dùng |
| Database       | Cơ sở dữ liệu    | Lưu trữ tất cả thông tin hệ thống   |

### User Interfaces

| Interface       | Người dùng       | Tính năng                         |
| :-------------- | :--------------- | :-------------------------------- |
| Admin Panel     | Admin            | RBAC, quản lý voucher, tài khoản, blog |
| Customer Portal | Customer/Guest   | Đặt vé, xem phim, thanh toán, feedback |
| Staff Dashboard | Staff            | Quản lý phim, lịch chiếu, F&B     |
| Cashier System  | Cashier          | Thanh toán, hóa đơn, voucher      |

### External Integrations

| Service       | Mục đích           | Kết nối            |
| :------------ | :----------------- | :----------------- |
| VNPay         | Thanh toán online  | Payment gateway    |
| Chatbot       | Hỗ trợ khách hàng  | Tự động hóa customer service |
| Google/Github | Đăng nhập xã hội   | OAuth integration  |

## 🔄 Quy trình hoạt động

### Quy trình đặt vé

```mermaid
graph TD
    A[Customer đăng nhập] --> B[Chọn phim và suất chiếu]
    B --> C[Chọn ghế ngồi]
    C --> D[Thêm F&B (tuỳ chọn)]
    D --> E[Áp dụng voucher (tuỳ chọn)]
    E --> F[Chọn phương thức thanh toán]
    F --> G{Thanh toán online?}
    G -- Có --> H[VNPay Gateway]
    G -- Không --> I[Thanh toán tại quầy]
    H --> J[Xác nhận thanh toán]
    I --> J
    J --> K[Tạo vé và gửi email]