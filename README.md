
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

<div align="center">
    <hr />
    <sub>
        🚀 Dự án được phát triển bởi <strong>Team Cinemax</strong><br/>
        💼 Một sản phẩm của nhóm phát triển phần mềm tại Việt Nam<br/>
        📅 © 2025 Cinemax. All rights reserved.
    </sub>
</div>
