CREATE DATABASE a;

USE a;


CREATE TABLE Account (
  AccountID int IDENTITY PRIMARY KEY,
  Email     varchar(50) NOT NULL UNIQUE,
  Password  varchar(50) NOT NULL,
  Role      varchar(50) CHECK (Role IN ('Admin', 'Customer', 'Staff', 'Cashier', 'Customer Officer')),
  Status bit DEFAULT 0
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
  MovieRate   int NOT NULL,
  Actor       nvarchar(MAX) NOT NULL
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
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE Promotion (
  PromodtionID  int IDENTITY PRIMARY KEY,
  PromotionCode varchar(10) NOT NULL UNIQUE,
  Discount      int NOT NULL,
  StartTime     datetime NOT NULL,
  EndTime       datetime NOT NULL,
  Quantity      int NOT NULL,
  Active        varchar(20) CHECK (Active IN ('Available', 'Expired'))
);

CREATE TABLE Theater (
  TheaterID    int IDENTITY PRIMARY KEY,
  TheaterName  nvarchar(100) NOT NULL,
  Address      nvarchar(100) NOT NULL,
  Image        varchar(255) NOT NULL,
  RoomQuantity int NOT NULL,
  AdminID   int NULL -- để tránh vòng lặp, thêm FOREIGN KEY ở cuối
);

CREATE TABLE Room (
  RoomID     int IDENTITY PRIMARY KEY,
  TheaterID  int NOT NULL,
  Name       varchar(10) NOT NULL,
  Collumn    int NOT NULL,
  Row        int NOT NULL,
  TypeOfRoom varchar(20) CHECK (TypeOfRoom IN ('Couple', 'Single')),
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID)
);

CREATE TABLE Seat (
  SeatID    int IDENTITY PRIMARY KEY,
  RoomID    int NOT NULL,
  SeatType  varchar(10) CHECK (SeatType IN ('Couple', 'Single')), --Them trigger Tu dien seatTYpe Theo RoomType
  Position  varchar(10) NOT NULL,
  IsVIP     bit NOT NULL,
  UnitPrice decimal(10, 2) NOT NULL,
  FOREIGN KEY (RoomID) REFERENCES Room(RoomID)
);

CREATE TABLE Schedule (
  ScheduleID int IDENTITY PRIMARY KEY,
  StartTime  datetime NOT NULL,
  EndTime    datetime NOT NULL,
  MovieID    int NOT NULL,
  RoomID     int NOT NULL,
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID),
  FOREIGN KEY (RoomID) REFERENCES Room(RoomID)
);

CREATE TABLE Employee (
  EmployeeID int IDENTITY PRIMARY KEY,
  Position   varchar(255) CHECK (Position IN ('Admin', 'Staff', 'Cashier', 'Customer Officer')),
  Status     varchar(20) NOT NULL,
  AccountID  int NOT NULL,
  TheaterID  int NOT NULL,
  AdminID  int NULL,
  FullName   nvarchar(100) NOT NULL,
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID),
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID),
  FOREIGN KEY (AdminID) REFERENCES Employee(EmployeeID)
);

-- Thêm FK ManagerID vào Theater (sau khi Customer đã có)
ALTER TABLE Theater
ADD CONSTRAINT FK_Theater_Manager FOREIGN KEY (AdminID) REFERENCES Employee(EmployeeID);

CREATE TABLE Invoice (
  InvoiceID   int IDENTITY PRIMARY KEY,
  CustomerID  int NOT NULL,
  EmployeeID  int NULL,
  PromotionID int NULL,
  BookingDate datetime NOT NULL,
  Totalprice  decimal(10, 2) NOT NULL,
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
  FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID),
  FOREIGN KEY (PromotionID) REFERENCES Promotion(PromodtionID)
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

CREATE TABLE Theater_Stock (
  Theater_StockID int IDENTITY PRIMARY KEY,
  TheaterID       int NOT NULL,
  FoodName            nvarchar(20) NOT NULL,
  Quantity        int NOT NULL,
  UnitPrice       decimal(10, 2) NOT NULL,
  Image           varchar(255) NOT NULL,
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
  Status      varchar(10) CHECK (Status IN ('Suported', 'Not_Suported')),
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID)
);

CREATE TABLE History (
  HistoryID int IDENTITY PRIMARY KEY,
  TableName varchar(100) NOT NULL,
  RecordID  int NOT NULL,
  OldValue  varchar(255) NOT NULL,
  NewValue  varchar(255) NOT NULL,
  Action    varchar(255) NOT NULL,
  AccountID int NOT NULL,
  UpdatedAt datetime NOT NULL,
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

-- Account
INSERT INTO Account (Email, Password, Role, Status) VALUES ('user0@example.com', 'pass0', 'Customer', 1);
INSERT INTO Account (Email, Password, Role, Status) VALUES ('user1@example.com', 'pass1', 'Customer', 1);
INSERT INTO Account (Email, Password, Role, Status) VALUES ('user2@example.com', 'pass2', 'Customer', 1);
INSERT INTO Account (Email, Password, Role, Status) VALUES ('user3@example.com', 'pass3', 'Customer', 1);
INSERT INTO Account (Email, Password, Role, Status) VALUES ('user4@example.com', 'pass4', 'Customer', 1);

-- Genre
INSERT INTO Genre (GenreName) VALUES ('Action');
INSERT INTO Genre (GenreName) VALUES ('Comedy');
INSERT INTO Genre (GenreName) VALUES ('Drama');
INSERT INTO Genre (GenreName) VALUES ('Horror');
INSERT INTO Genre (GenreName) VALUES ('Sci-Fi');

-- MovieDTO
INSERT INTO Movie (MovieName, Description, Image, Studio, Genre, Duration, Trailer, MovieRate, Actor)
VALUES ('MovieDTO 0', 'Description 0', 'img0.jpg', 'Studio 0', 'Action', 90, 'trailer0.mp4', 8, 'Actor A, Actor B');
INSERT INTO Movie (MovieName, Description, Image, Studio, Genre, Duration, Trailer, MovieRate, Actor)
VALUES ('MovieDTO 1', 'Description 1', 'img1.jpg', 'Studio 1', 'Comedy', 100, 'trailer1.mp4', 6, 'Actor A, Actor B');
INSERT INTO Movie (MovieName, Description, Image, Studio, Genre, Duration, Trailer, MovieRate, Actor)
VALUES ('MovieDTO 2', 'Description 2', 'img2.jpg', 'Studio 2', 'Drama', 110, 'trailer2.mp4', 7, 'Actor A, Actor B');
INSERT INTO Movie (MovieName, Description, Image, Studio, Genre, Duration, Trailer, MovieRate, Actor)
VALUES ('MovieDTO 3', 'Description 3', 'img3.jpg', 'Studio 3', 'Horror', 120, 'trailer3.mp4', 9, 'Actor A, Actor B');
INSERT INTO Movie (MovieName, Description, Image, Studio, Genre, Duration, Trailer, MovieRate, Actor)
VALUES ('MovieDTO 4', 'Description 4', 'img4.jpg', 'Studio 4', 'Sci-Fi', 130, 'trailer4.mp4', 5, 'Actor A, Actor B');

-- Movie_Genre
INSERT INTO Movie_Genre (GenreID, MovieID) VALUES (1, 1);
INSERT INTO Movie_Genre (GenreID, MovieID) VALUES (2, 2);
INSERT INTO Movie_Genre (GenreID, MovieID) VALUES (3, 3);
INSERT INTO Movie_Genre (GenreID, MovieID) VALUES (4, 4);
INSERT INTO Movie_Genre (GenreID, MovieID) VALUES (5, 5);

-- Customer
INSERT INTO Customer (AccountID, FullName, Phone) VALUES (1, 'Customer 1', '0123456781');
INSERT INTO Customer (AccountID, FullName, Phone) VALUES (2, 'Customer 2', '0123456782');
INSERT INTO Customer (AccountID, FullName, Phone) VALUES (3, 'Customer 3', '0123456783');
INSERT INTO Customer (AccountID, FullName, Phone) VALUES (4, 'Customer 4', '0123456784');
INSERT INTO Customer (AccountID, FullName, Phone) VALUES (5, 'Customer 5', '0123456785');

-- Promotion
INSERT INTO Promotion (PromotionCode, Discount, StartTime, EndTime, Quantity, Active)
VALUES ('PROMO0', 10, GETDATE(), DATEADD(DAY, 30, GETDATE()), 100, 'Available');
INSERT INTO Promotion (PromotionCode, Discount, StartTime, EndTime, Quantity, Active)
VALUES ('PROMO1', 15, GETDATE(), DATEADD(DAY, 30, GETDATE()), 110, 'Available');
INSERT INTO Promotion (PromotionCode, Discount, StartTime, EndTime, Quantity, Active)
VALUES ('PROMO2', 20, GETDATE(), DATEADD(DAY, 30, GETDATE()), 120, 'Available');

-- Theater
INSERT INTO Theater (TheaterName, Address, Image, RoomQuantity) VALUES ('Theater 0', 'Address 0', 'theater0.jpg', 5);
INSERT INTO Theater (TheaterName, Address, Image, RoomQuantity) VALUES ('Theater 1', 'Address 1', 'theater1.jpg', 6);

-- Room
INSERT INTO Room (TheaterID, Name, Collumn, Row, TypeOfRoom) VALUES (1, 'Room1', 10, 10, 'Single');
INSERT INTO Room (TheaterID, Name, Collumn, Row, TypeOfRoom) VALUES (1, 'Room2', 10, 10, 'Single');
INSERT INTO Room (TheaterID, Name, Collumn, Row, TypeOfRoom) VALUES (1, 'Room3', 10, 10, 'Single');

-- Seat
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice)
VALUES (1, 'Single', 'A1', 0, 50.00), (1, 'Single', 'A2', 0, 50.00), (1, 'Single', 'A3', 0, 50.00),
       (2, 'Single', 'A1', 0, 50.00), (2, 'Single', 'A2', 0, 50.00),
       (3, 'Single', 'A1', 0, 50.00), (3, 'Single', 'A2', 0, 50.00);

-- Schedule
INSERT INTO Schedule (StartTime, EndTime, MovieID, RoomID)
VALUES (DATEADD(DAY, 1, GETDATE()), DATEADD(HOUR, 2, DATEADD(DAY, 1, GETDATE())), 1, 1);
INSERT INTO Schedule (StartTime, EndTime, MovieID, RoomID)
VALUES (DATEADD(DAY, 2, GETDATE()), DATEADD(HOUR, 2, DATEADD(DAY, 2, GETDATE())), 2, 2);
INSERT INTO Schedule (StartTime, EndTime, MovieID, RoomID)
VALUES (DATEADD(DAY, 3, GETDATE()), DATEADD(HOUR, 2, DATEADD(DAY, 3, GETDATE())), 3, 3);

-- Thêm tài khoản cho nhân viên
INSERT INTO Account (Email, Password, Role, Status) VALUES ('employee5@example.com', 'pass5', 'Staff', 1);
INSERT INTO Account (Email, Password, Role, Status) VALUES ('employee6@example.com', 'pass6', 'Staff', 1);
INSERT INTO Account (Email, Password, Role, Status) VALUES ('employee7@example.com', 'pass7', 'Staff', 1);

-- Employee
INSERT INTO Employee (Position, Status, AccountID, TheaterID, FullName)
VALUES ('Staff', 'Active', 6, 1, 'Employee 6');
INSERT INTO Employee (Position, Status, AccountID, TheaterID, FullName)
VALUES ('Staff', 'Active', 7, 1, 'Employee 7');
INSERT INTO Employee (Position, Status, AccountID, TheaterID, FullName)
VALUES ('Staff', 'Active', 8, 1, 'Employee 8');

-- Gán AdminID cho Theater
UPDATE Theater SET AdminID = 1 WHERE TheaterID = 1;

-- InvoiceDTO
INSERT INTO Invoice (CustomerID, EmployeeID, PromotionID, BookingDate, Totalprice)
VALUES (1, 1, 1, GETDATE(), 100.00);
INSERT INTO Invoice (CustomerID, EmployeeID, PromotionID, BookingDate, Totalprice)
VALUES (2, 1, 1, GETDATE(), 100.00);
INSERT INTO Invoice (CustomerID, EmployeeID, PromotionID, BookingDate, Totalprice)
VALUES (3, 1, 1, GETDATE(), 100.00);

-- Detail_Seat
INSERT INTO Detail_Seat (InvoiceID, SeatID, Status, ScheduleID) VALUES (1, 1, 'Booked', 1);
INSERT INTO Detail_Seat (InvoiceID, SeatID, Status, ScheduleID) VALUES (2, 2, 'Booked', 2);
INSERT INTO Detail_Seat (InvoiceID, SeatID, Status, ScheduleID) VALUES (3, 3, 'Booked', 3);

-- Theater_Stock
INSERT INTO Theater_Stock (TheaterID, FoodName, Quantity, UnitPrice, Image)
VALUES (1, 'Popcorn 1', 10, 20.00, 'popcorn1.jpg');
INSERT INTO Theater_Stock (TheaterID, FoodName, Quantity, UnitPrice, Image)
VALUES (1, 'Popcorn 2', 20, 20.00, 'popcorn2.jpg');
INSERT INTO Theater_Stock (TheaterID, FoodName, Quantity, UnitPrice, Image)
VALUES (1, 'Popcorn 3', 30, 20.00, 'popcorn3.jpg');

-- Detail_FD
INSERT INTO Detail_FD (InvoiceID, Theater_StockID, Quantity, TotalPrice)
VALUES (1, 1, 2, 40.00);
INSERT INTO Detail_FD (InvoiceID, Theater_StockID, Quantity, TotalPrice)
VALUES (2, 2, 2, 40.00);
INSERT INTO Detail_FD (InvoiceID, Theater_StockID, Quantity, TotalPrice)
VALUES (3, 3, 2, 40.00);

-- MovieFeedback
INSERT INTO MovieFeedback (CustomerID, MovieID, Content, MovieRate)
VALUES (1, 1, 'Great movie 1', 5);
INSERT INTO MovieFeedback (CustomerID, MovieID, Content, MovieRate)
VALUES (2, 2, 'Great movie 2', 4);
INSERT INTO MovieFeedback (CustomerID, MovieID, Content, MovieRate)
VALUES (3, 3, 'Great movie 3', 5);

-- ServiceFeedback
INSERT INTO ServiceFeedback (CustomerID, Content, Status)
VALUES (1, 'Service feedback 1', 'Suported');
INSERT INTO ServiceFeedback (CustomerID, Content, Status)
VALUES (2, 'Service feedback 2', 'Suported');
INSERT INTO ServiceFeedback (CustomerID, Content, Status)
VALUES (3, 'Service feedback 3', 'Suported');

-- History
INSERT INTO History (TableName, RecordID, OldValue, NewValue, Action, AccountID, UpdatedAt)
VALUES ('MovieDTO', 1, 'Old MovieDTO 1', 'New MovieDTO 1', 'Update', 1, GETDATE());
INSERT INTO History (TableName, RecordID, OldValue, NewValue, Action, AccountID, UpdatedAt)
VALUES ('MovieDTO', 2, 'Old MovieDTO 2', 'New MovieDTO 2', 'Update', 1, GETDATE());
INSERT INTO History (TableName, RecordID, OldValue, NewValue, Action, AccountID, UpdatedAt)
VALUES ('MovieDTO', 3, 'Old MovieDTO 3', 'New MovieDTO 3', 'Update', 1, GETDATE());
