create database a

use a

CREATE TABLE Account (
  AccountID int IDENTITY PRIMARY KEY,
  Email     varchar(50) NOT NULL UNIQUE,
  Password  varchar(50) NOT NULL,
  Role      varchar(50) CHECK (Role IN ('Admin', 'Customer', 'Staff', 'Cashier', 'Customer_Officer')),
  Status	varchar(10) CHECK (Status IN ('Active', 'Banned'))
);

CREATE TABLE Genre (
  GenreID   int IDENTITY PRIMARY KEY,
  GenreName varchar(255) NOT NULL
);

CREATE TABLE Movie (
  MovieID     int IDENTITY PRIMARY KEY,
  MovieName   nvarchar(255) NOT NULL,
  Description nvarchar(1000) NULL,
  Image       varchar(255) NOT NULL,
  Studio      nvarchar(100) NULL,
  Genre       varchar(255) NOT NULL,
  Duration    int NOT NULL,
  Trailer     varchar(255) NOT NULL,
  MovieRate   DECIMAL(3,1) CHECK (MovieRate Between 0 and 5),
  Actor       nvarchar(MAX) NOT NULL,
  StartDate   Date NOT NULL,
  EndDate	  Date NOT NULL,
  Status	  varchar(10) CHECK (Status IN ('Active', 'Removed'))
);

CREATE TABLE Movie_Genre (
  Id      int IDENTITY PRIMARY KEY,
  GenreID int NOT NULL,
  MovieID int NOT NULL,
  FOREIGN KEY (GenreID) REFERENCES Genre(GenreID),
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID)
);

CREATE TABLE Customer (
  CustomerID int IDENTITY PRIMARY KEY,
  AccountID  int NOT NULL,
  FullName   nvarchar(100) NOT NULL,
  Phone      varchar(20),
  Point	     int,
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE Promotion (
  PromotionID   int IDENTITY PRIMARY KEY,  -- Sửa từ PromodtionID thành PromotionID
  PromotionCode varchar(10) NOT NULL UNIQUE,
  Discount      int NOT NULL,
  StartTime     datetime NOT NULL,
  EndTime       datetime NOT NULL,
  Quantity      int NOT NULL,
  Status        varchar(20) CHECK (Status IN ('Available', 'Expired'))
);

CREATE TABLE Theater (
  TheaterID    int IDENTITY PRIMARY KEY,
  TheaterName  nvarchar(100) NOT NULL,
  Address      nvarchar(100) NOT NULL,
  Image        varchar(255) NOT NULL,
  RoomQuantity int NOT NULL,
  Status	   varchar(20) CHECK (Status IN ('Active', 'Inactive'))	   
);

CREATE TABLE Room (
  RoomID     int IDENTITY PRIMARY KEY,
  TheaterID  int NOT NULL,
  Name       varchar(10) NOT NULL,
  Collumn    int NOT NULL,
  Row        int NOT NULL,
  TypeOfRoom varchar(20) CHECK (TypeOfRoom IN ('Couple', 'Single')),
  Status	 varchar(20) CHECK (Status IN ('Active', 'Inactive')),  -- Thêm dấu phẩy
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID)
);

CREATE TABLE Seat (
  SeatID    int IDENTITY PRIMARY KEY,
  RoomID    int NOT NULL,
  SeatType  varchar(10) CHECK (SeatType IN ('Couple', 'Single')), --Them trigger Tu dien seatTYpe Theo RoomType
  Position  varchar(10) NOT NULL,
  IsVIP     bit NOT NULL,
  UnitPrice decimal(10, 2) NOT NULL,
  Status	varchar(20) CHECK (Status IN ('Active', 'Inactive')),  -- Thêm dấu phẩy
  FOREIGN KEY (RoomID) REFERENCES Room(RoomID)
);

CREATE TABLE Schedule (
  ScheduleID int IDENTITY PRIMARY KEY,
  StartTime  datetime NOT NULL,
  EndTime    datetime NOT NULL,
  MovieID    int NOT NULL,
  RoomID     int NOT NULL,
  Status	 varchar(20) CHECK (Status IN ('Active', 'Inactive')),  -- Thêm dấu phẩy
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID),
  FOREIGN KEY (RoomID) REFERENCES Room(RoomID)
);

CREATE TABLE Employee (
  EmployeeID int IDENTITY PRIMARY KEY,
  Position   varchar(255) CHECK (Position IN ('Admin', 'Staff', 'Cashier', 'Customer_Officer')),
  Status     varchar(20) CHECK(Status IN ('Active', 'Inactive')),
  AccountID  int NOT NULL,
  TheaterID  int NOT NULL,
  AdminID    int NULL,
  FullName   nvarchar(100) NOT NULL,
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID),
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID),
  FOREIGN KEY (AdminID) REFERENCES Employee(EmployeeID)
);

CREATE TABLE Invoice (
  InvoiceID   int IDENTITY PRIMARY KEY,
  CustomerID  int NOT NULL,
  EmployeeID  int NULL,
  PromotionID int NULL,  -- Thêm cột PromotionID
  Discount	  float NULL,				--giá tiền
  BookingDate datetime NOT NULL,
  Totalprice  decimal(10, 2) NOT NULL,
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
  FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID),
  FOREIGN KEY (PromotionID) REFERENCES Promotion(PromotionID)  -- Sửa tham chiếu
);

CREATE TABLE Detail_Seat (
  ID         int IDENTITY PRIMARY KEY,
  InvoiceID  int NOT NULL,
  SeatID     int NOT NULL,
  Status     varchar(10) CHECK (Status IN ('Booked', 'Available')),
  ScheduleID int NOT NULL,
  FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID),
  FOREIGN KEY (SeatID) REFERENCES Seat(SeatID),
  FOREIGN KEY (ScheduleID) REFERENCES Schedule(ScheduleID)
);

CREATE TABLE Theater_Stock (	--đồ ăn thức uống
  Theater_StockID int IDENTITY PRIMARY KEY,
  TheaterID       int NOT NULL,
  FoodName        nvarchar(20) NOT NULL,
  Quantity        int NOT NULL,
  UnitPrice       decimal(10, 2) NOT NULL,
  Image           varchar(255) NOT NULL,
  Status          varchar(20) CHECK(Status IN ('Active', 'Inactive')),
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID)
);

CREATE TABLE Detail_FD (
  ID              int IDENTITY PRIMARY KEY,
  InvoiceID       int NOT NULL,
  Theater_StockID int NOT NULL,
  Quantity        int NOT NULL,
  TotalPrice      decimal(10, 2),
  FOREIGN KEY (InvoiceID) REFERENCES Invoice(InvoiceID),
  FOREIGN KEY (Theater_StockID) REFERENCES Theater_Stock(Theater_StockID)
);

CREATE TABLE MovieFeedback (
  ID          int IDENTITY PRIMARY KEY,
  CustomerID  int NOT NULL,
  MovieID     int NOT NULL,
  Content     varchar(255),
  MovieRate   int DEFAULT 5,
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID)
);

CREATE TABLE ServiceFeedback (
  ID         int IDENTITY PRIMARY KEY,
  CustomerID int NOT NULL,
  Content    nvarchar(100),
  Status     varchar(10) CHECK (Status IN ('Suported', 'Not_Suported')),
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

CREATE TABLE History (
  HistoryID int IDENTITY PRIMARY KEY,
  TableName varchar(100) NOT NULL,
  RecordID  int NOT NULL,
  OldValue  varchar(255) NOT NULL,
  NewValue  varchar(255) NOT NULL,
  Action    varchar(20) CHECK (Action IN ('Update', 'Delete')),
  AccountID int NOT NULL,
  UpdatedAt datetime NOT NULL,
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);