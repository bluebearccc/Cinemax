CREATE DATABASE CINEMAX;

USE CINEMAX;	

CREATE TABLE Account (
  AccountID int IDENTITY PRIMARY KEY,
  Email     varchar(50) NOT NULL UNIQUE,
  Password  varchar(50) NOT NULL,
  Role      varchar(50) CHECK (Role IN ('Admin', 'Customer', 'Staff', 'Cashier', 'Customer_Officer')) NOT NULL,
  Status	varchar(10) CHECK (Status IN ('Active', 'Banned')) NOT NULL
);

CREATE TABLE Genre (
  GenreID   int IDENTITY PRIMARY KEY,
  GenreName varchar(255) NOT NULL
);

CREATE TABLE Actor (
  ActorID   int IDENTITY PRIMARY KEY,
  ActorName varchar(255) NOT NULL,
  Image     varchar(255) NOT NULL
);

CREATE TABLE Movie (
  MovieID     int IDENTITY PRIMARY KEY,
  MovieName   nvarchar(255) NOT NULL,
  Description nvarchar(MAX) NULL,
  Image       varchar(255) NOT NULL,
  Banner	  varchar(255) NOT NULL,
  Studio      nvarchar(100) NULL,
  Duration    int NOT NULL,
  Trailer     varchar(255) NOT NULL,
  MovieRate   DECIMAL(3,1) CHECK (MovieRate Between 0 and 5) NULL,
  StartDate   Date NOT NULL,
  EndDate	  Date NOT NULL,
  Status	  varchar(10) CHECK (Status IN ('Active', 'Removed')) NOT NULL
);

CREATE TABLE Movie_Genre (
  Id      int IDENTITY PRIMARY KEY,
  GenreID int NOT NULL,
  MovieID int NOT NULL,
  FOREIGN KEY (GenreID) REFERENCES Genre(GenreID),
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID)
);

CREATE TABLE Movie_Actor (
  Id	  int IDENTITY PRIMARY KEY,
  ActorID int NOT NULL,
  MovieID int NOT NULL,
  FOREIGN KEY (ActorID) REFERENCES Actor(ActorID),
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID)
);

CREATE TABLE Customer (
  CustomerID int IDENTITY PRIMARY KEY,
  AccountID  int NOT NULL,
  FullName   nvarchar(100) NOT NULL,
  Phone      varchar(20),
  Point	     int
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE Promotion (  
  PromotionID  int IDENTITY PRIMARY KEY,
  PromotionCode varchar(10) NOT NULL UNIQUE,
  Discount      int NOT NULL,
  StartTime     datetime NOT NULL,
  EndTime       datetime NOT NULL,
  Quantity      int NOT NULL,
  Status        varchar(20) CHECK (Status IN ('Available', 'Expired')) NOT NULL
);

CREATE TABLE Theater (
  TheaterID    int IDENTITY PRIMARY KEY,
  TheaterName  nvarchar(100) NOT NULL,
  Address      nvarchar(100) NOT NULL,
  Image        varchar(255) NOT NULL,
  RoomQuantity int NOT NULL,
  ServiceRate  int DECIMAL(3,1) CHECK (MovieRate Between 0 and 5) NULL, 
  Status	   varchar(20) CHECK (Status IN ('Active', 'Inactive')) NOT NULL	   
);

CREATE TABLE Room (0
  RoomID     int IDENTITY PRIMARY KEY,
  TheaterID  int NOT NULL,
  Name       varchar(10) NOT NULL,
  Collumn    int NOT NULL,
  Row        int NOT NULL,
  TypeOfRoom varchar(20) CHECK (TypeOfRoom IN ('Couple', 'Single')) NOT NULL,
  Status	 varchar(20) CHECK (Status IN ('Active', 'Inactive')) NOT NULL	  
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID)
);

CREATE TABLE Seat (
  SeatID    int IDENTITY PRIMARY KEY,
  RoomID    int NOT NULL,
  SeatType  varchar(10) CHECK (SeatType IN ('Couple', 'Single')) NOT NULL, --Them trigger Tu dien seatTYpe Theo RoomType
  Position  varchar(10) NOT NULL,
  IsVIP     bit NOT NULL,
  UnitPrice decimal(10, 2) NOT NULL,
  Status	varchar(20) CHECK (Status IN ('Active', 'Inactive')) NOT NULL	-- status để check nếu ghế đó bị hỏng (ko check ghế đã đặt hay chưa cái đấy check trong hóa đơn theo scheduleID)
  FOREIGN KEY (RoomID) REFERENCES Room(RoomID)
);

CREATE TABLE Schedule (
  ScheduleID int IDENTITY PRIMARY KEY,
  StartTime  datetime NOT NULL,
  EndTime    datetime NOT NULL,
  MovieID    int NOT NULL,
  RoomID     int NOT NULL,
  Status	 varchar(20) CHECK (Status IN ('Active', 'Inactive')) NOT NULL	
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID),
  FOREIGN KEY (RoomID) REFERENCES Room(RoomID)
);

CREATE TABLE Employee (
  EmployeeID int IDENTITY PRIMARY KEY,
  Position   varchar(50) CHECK (Position IN ('Admin', 'Staff', 'Cashier', 'Customer_Officer')) NOT NULL,
  Status     varchar(20) CHECK(Status IN ('Active', 'Inactive')) NOT NULL,
  AccountID  int NOT NULL,
  TheaterID  int NOT NULL,
  AdminID  int NULL,
  FullName   nvarchar(100) NOT NULL,
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID),
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID),
  FOREIGN KEY (AdminID) REFERENCES Employee(EmployeeID)
);

CREATE TABLE Invoice (
  InvoiceID   int IDENTITY PRIMARY KEY,
  CustomerID  int NOT NULL,
  EmployeeID  int NULL,
  PromotionID  int,
  Discount	  float NULL,				--giá tiền
  BookingDate datetime NOT NULL,
  Totalprice  decimal(10, 2) NOT NULL,
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
  FOREIGN KEY (EmployeeID) REFERENCES Employee(EmployeeID),
  FOREIGN KEY (PromotionID) REFERENCES Promotion(PromotionID)
);

CREATE TABLE Detail_Seat (
  ID         int IDENTITY PRIMARY KEY,
  InvoiceID  int NOT NULL,
  SeatID     int NOT NULL,
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
  Content     nvarchar(MAX),
  MovieRate   int,
  CreatedDate datetime DEFAULT GETDATE(),
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
  FOREIGN KEY (MovieID) REFERENCES Movie(MovieID)
);

CREATE TABLE ServiceFeedback (
  ID         int IDENTITY PRIMARY KEY,
  CustomerID int NOT NULL,
  CreatedDate datetime DEFAULT GETDATE(),
  Content    nvarchar(MAX),
  TheaterID  int NOT NULL,
  Status      varchar(20) CHECK (Status IN ('Suported', 'Not_Suported')) NOT NULL,
  FOREIGN KEY (CustomerID) REFERENCES Customer(CustomerID),
  FOREIGN KEY (TheaterID) REFERENCES Theater(TheaterID)
);

CREATE TABLE History (
  HistoryID int IDENTITY PRIMARY KEY,
  TableName varchar(100) NOT NULL,
  RecordID  int NOT NULL,
  OldValue  varchar(255) NOT NULL,
  NewValue  varchar(255) NOT NULL,
  Action    varchar(20) CHECK (Action IN ('Update', 'Delete')) NOT NULL,
  AccountID int NOT NULL,
  UpdatedAt datetime NOT NULL,
  FOREIGN KEY (AccountID) REFERENCES Account(AccountID)
);

CREATE TABLE ForgotPassword (
    id INT IDENTITY PRIMARY KEY,
    otp INT NOT NULL UNIQUE,
    Accountid INT NOT NULL,
    expiry_date DATETIME NOT NULL,
    FOREIGN KEY (Accountid) REFERENCES Account(AccountID)
);

CREATE TABLE VerifyToken (
    id INT IDENTITY PRIMARY KEY,
    Email VARCHAR(255) NOT NULL UNIQUE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiresAt DATETIME NOT NULL,
    [Password] VARCHAR(255) NOT NULL,
    FullName VARCHAR(255) NOT NULL
);

--TRIGGER

--trigger tính MovieRate
GO
CREATE TRIGGER trg_UpdateMovieRateAfterInsert
ON MovieFeedback
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Cập nhật MovieRate cho từng MovieID vừa được thêm feedback
    UPDATE M
    SET M.MovieRate = (
        SELECT CAST(AVG(CAST(F.MovieRate AS DECIMAL(3,1))) AS DECIMAL(3,1))
        FROM MovieFeedback F
        WHERE F.MovieID = M.MovieID
    )
    FROM Movie M
    INNER JOIN (
        SELECT DISTINCT MovieID
        FROM INSERTED
    ) I ON M.MovieID = I.MovieID;
END;

GO
CREATE TRIGGER trg_UpdateServiceRate
ON ServiceFeedback
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Cập nhật ServiceRate cho mỗi TheaterID bị ảnh hưởng
    UPDATE T
    SET T.ServiceRate = (
        SELECT CAST(AVG(CAST(Rate AS DECIMAL(3,1))) AS DECIMAL(3,1))
        FROM ServiceFeedback SF
        WHERE SF.TheaterID = T.TheaterID
    )
    FROM Theater T
    INNER JOIN (
        SELECT DISTINCT TheaterID
        FROM inserted
    ) i ON T.TheaterID = i.TheaterID;
END;

GO
CREATE TRIGGER trg_DecreasePromotionQuantity
ON Invoice
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Giảm số lượng Promotion nếu được sử dụng
    UPDATE P
    SET 
        P.Quantity = P.Quantity - 1,
        P.Status = CASE 
                      WHEN (P.Quantity - 1) <= 0 THEN 'Expired'
                      ELSE P.Status
                   END
    FROM Promotion P
    INNER JOIN inserted i ON P.PromotionID = i.PromotionID
    WHERE i.PromotionID IS NOT NULL;
END;

GO
CREATE TRIGGER trg_DecreaseTheaterStock
ON Detail_FD
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    -- Trừ số lượng thực phẩm đã bán từ kho của rạp
    UPDATE TS
    SET TS.Quantity = TS.Quantity - i.Quantity
    FROM Theater_Stock TS
    INNER JOIN inserted i ON TS.Theater_StockID = i.Theater_StockID;
END;