-- Account table data
INSERT INTO Account (Email, Password, Role, Status) VALUES
('admin1@cinema.com', 'admin123', 'Admin', 'Active'),
('staff1@cinema.com', 'staff123', 'Staff', 'Active'),
('cashier1@cinema.com', 'cashier123', 'Cashier', 'Active'),
('officer1@cinema.com', 'officer123', 'Customer_Officer', 'Active'),
('customer1@gmail.com', 'cust123', 'Customer', 'Active'),
('customer2@gmail.com', 'cust456', 'Customer', 'Active'),
('customer3@gmail.com', 'cust789', 'Customer', 'Active'),
('customer4@gmail.com', 'cust012', 'Customer', 'Banned'),
('staff2@cinema.com', 'staff456', 'Staff', 'Active'),
('customer5@gmail.com', 'cust345', 'Customer', 'Active'),
('admin2@cinema.com', 'admin456', 'Admin', 'Active'),
('admin3@cinema.com', 'admin789', 'Admin', 'Active'),
('staff3@cinema.com', 'staff789', 'Staff', 'Active'),
('cashier2@cinema.com', 'cashier456', 'Cashier', 'Active'),
('officer2@cinema.com', 'officer456', 'Customer_Officer', 'Active'),
('customer6@gmail.com', 'cust678', 'Customer', 'Active'),
('customer7@gmail.com', 'cust901', 'Customer', 'Active'),
('customer8@gmail.com', 'cust234', 'Customer', 'Active');

-- INSERT vào bảng Customer (chỉ những account có Role = 'Customer')
INSERT INTO Customer (AccountID, FullName, Phone, Point) VALUES
(5, N'Nguyễn Văn An', '0901234567', 100),
(6, N'Trần Thị Bình', '0902345678', 50),
(7, N'Lê Văn Cường', '0903456789', 200),
(8, N'Phạm Thị Dung', '0904567890', 0),
(10, N'Hoàng Văn Em', '0905678901', 150),
(16, N'Võ Thị Fang', '0906789012', 75),
(17, N'Đặng Văn Giang', '0907890123', 300),
(18, N'Bùi Thị Hạnh', '0908901234', 25);

-- Genre table data
INSERT INTO Genre (GenreName) VALUES
('Action'),
('Horror'),
('Comedy'),
('Romance'),
('Science Fiction'),
('Adventure'),
('Animation'),
('Drama'),
('Crime'),
('Documentary');

-- Dữ liệu bảng Movie đã được cập nhật và thêm mới
INSERT INTO Movie (MovieName, Description, Image, Banner, Studio, Duration, Trailer, MovieRate, Actor, StartDate, EndDate, Status) VALUES
('Avengers: Endgame', 'In a climactic showdown that spans across time and space, Earth''s mightiest heroes unite for one final stand against Thanos. With lives lost and worlds shattered, they must risk everything to undo the devastation and restore balance to the universe.', '/customer-static/images/movie/movie1.png', '/customer-static/images/movie/banner1.png', 'Marvel Studios', 181, 'https://youtube.com/watch?v=trailer1', 4.8, 'Robert Downey Jr., Chris Evans, Scarlett Johansson, Mark Ruffalo', '2025-06-12', '2025-09-12', 'Active'),
('The Dark Knight', 'As chaos grips Gotham City, Batman must confront the psychological warfare and moral dilemmas posed by the Joker — a criminal mastermind who seeks to dismantle the city''s sense of order. It''s a battle not just for justice, but for the soul of Gotham.', '/customer-static/images/movie/movie2.png', '/customer-static/images/movie/banner2.png', 'Warner Bros', 152, 'https://youtube.com/watch?v=trailer2', 4.9, 'Christian Bale, Heath Ledger, Aaron Eckhart, Michael Caine', '2025-06-12', '2025-09-12', 'Active'),
('Inception', 'Dom Cobb is a master of extraction — stealing secrets from within the subconscious. But now he faces his greatest challenge: planting an idea rather than stealing one. This dangerous mission takes him and his team deep into layers of dreams, where reality begins to blur.', '/customer-static/images/movie/movie3.png', '/customer-static/images/movie/banner3.png', 'Warner Bros', 148, 'https://youtube.com/watch?v=trailer3', 4.7, 'Leonardo DiCaprio, Marion Cotillard, Tom Hardy, Cillian Murphy', '2025-06-12', '2025-09-12', 'Active'),
('Parasite', 'The struggling Kim family infiltrates the wealthy Park household, setting off a chain of deception and dark revelations. A satirical yet haunting commentary on social inequality, this thriller blurs the line between the privileged and the oppressed.', '/customer-static/images/movie/movie4.png', '/customer-static/images/movie/banner4.png', 'CJ Entertainment', 132, 'https://youtube.com/watch?v=trailer4', 4.6, 'Song Kang-ho, Lee Sun-kyun, Cho Yeo-jeong, Choi Woo-shik', '2025-06-12', '2025-09-12', 'Active'),
('Spider-Man: No Way Home', 'When Peter''s secret identity is exposed, he turns to Doctor Strange for help. But tampering with the multiverse unleashes iconic villains from other realities. Peter must rise to the occasion and confront choices that will change his life forever.', '/customer-static/images/movie/movie5.png', '/customer-static/images/movie/banner5.png', 'Sony Pictures', 148, 'https://youtube.com/watch?v=trailer5', 4.5, 'Tom Holland, Zendaya, Benedict Cumberbatch, Willem Dafoe', '2025-06-12', '2025-09-12', 'Active'),
('Titanic', 'Rose, a young aristocrat, and Jack, a penniless artist, find unexpected love aboard the ill-fated RMS Titanic. As the ship collides with destiny, their brief but powerful romance is tested by disaster and the divide of social class.', '/customer-static/images/movie/movie6.png', '/customer-static/images/movie/banner6.png', 'Paramount Pictures', 195, 'https://youtube.com/watch?v=trailer6', 4.4, 'Leonardo DiCaprio, Kate Winslet, Billy Zane, Gloria Stuart', '2025-06-12', '2025-09-12', 'Removed'),
('The Shawshank Redemption', 'Wrongfully convicted, Andy Dufresne maintains his dignity and hope over decades in Shawshank prison. His unlikely friendship with fellow inmate Red becomes a lifeline, as both seek redemption and the promise of freedom beyond the walls.', '/customer-static/images/movie/movie7.png', '/customer-static/images/movie/banner7.png', 'Columbia Pictures', 142, 'https://youtube.com/watch?v=trailer7', 4.9, 'Tim Robbins, Morgan Freeman, Bob Gunton, James Whitmore', '2025-06-12', '2025-09-12', 'Active'),
('Pulp Fiction', 'Through a web of hitmen, gangsters, and drifters, this iconic crime saga unfolds with sharp dialogue and nonlinear storytelling. Quentin Tarantino''s masterpiece blends violence, humor, and redemption in the seedy underworld of Los Angeles.', '/customer-static/images/movie/movie8.png', '/customer-static/images/movie/banner8.png', 'Miramax Films', 154, 'https://youtube.com/watch?v=trailer8', 4.3, 'John Travolta, Samuel L. Jackson, Uma Thurman, Bruce Willis', '2025-06-12', '2025-10-12', 'Active'),
('The Godfather', 'Follow the Corleone family as they navigate loyalty, betrayal, and power in post-war America. As Michael Corleone is drawn into his family''s empire, he transforms from reluctant outsider to ruthless mafia boss in this cinematic epic.', '/customer-static/images/movie/movie9.png', '/customer-static/images/movie/banner9.png', 'Paramount Pictures', 175, 'https://youtube.com/watch?v=trailer9', 4.8, 'Marlon Brando, Al Pacino, James Caan, Robert Duvall', '2025-06-12', '2025-10-12', 'Active'),
('Forrest Gump', 'Forrest Gump, a kind-hearted man with a low IQ but pure intentions, unknowingly influences historical events across decades. Through love, war, and loss, his story is a poignant reminder that greatness can come from the most unexpected places.', '/customer-static/images/movie/movie10.png', '/customer-static/images/movie/banner10.png', 'Paramount Pictures', 142, 'https://youtube.com/watch?v=trailer10', 4.6, 'Tom Hanks, Robin Wright, Gary Sinise, Sally Field', '2025-06-12', '2025-10-12', 'Active'),
('The Matrix', 'Neo, a hacker questioning the nature of his reality, discovers the truth: the world is an illusion created by machines. As the chosen one, he must awaken others and lead the fight to free humanity from the Matrix''s grip.', '/customer-static/images/movie/movie11.png', '/customer-static/images/movie/banner11.png', 'Warner Bros', 136, 'https://youtube.com/watch?v=trailer11', 4.4, 'Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss, Hugo Weaving', '2025-06-12', '2025-10-12', 'Active'),
('Interstellar', 'In a future where Earth is dying, a group of astronauts travel through a wormhole in search of a new planet. Interstellar explores time, love, and survival across galaxies — with humanity''s fate hanging in the balance.', '/customer-static/images/movie/movie12.png', '/customer-static/images/movie/banner12.png', 'Warner Bros', 169, 'https://youtube.com/watch?v=trailer12', 4.5, 'Matthew McConaughey, Anne Hathaway, Jessica Chastain, Michael Caine', '2025-06-12', '2025-10-12', 'Active'),
('Fight Club', 'Disillusioned by consumerism and routine, an unnamed narrator meets the anarchic Tyler Durden. Together, they form a secret fight club that spirals into chaos. A dark psychological tale of identity, rebellion, and inner conflict.', '/customer-static/images/movie/movie13.png', '/customer-static/images/movie/banner13.png', '20th Century Fox', 139, 'https://youtube.com/watch?v=trailer13', 4.2, 'Brad Pitt, Edward Norton, Helena Bonham Carter, Meat Loaf', '2025-06-12', '2025-10-12', 'Removed'),
('Goodfellas', 'From petty crimes to major heists, Henry Hill rises through the ranks of the mob in 1970s New York. But the lifestyle of wealth and violence comes at a price. Based on a true story, this film delves into the allure and downfall of organized crime.', '/customer-static/images/movie/movie14.png', '/customer-static/images/movie/banner14.png', 'Warner Bros', 146, 'https://youtube.com/watch?v=trailer14', 4.3, 'Robert De Niro, Ray Liotta, Joe Pesci, Lorraine Bracco', '2025-06-12', '2025-11-12', 'Active'),
('The Lord of the Rings', 'As Middle-earth hangs in the balance, Frodo and Sam push toward Mount Doom to destroy the One Ring. With darkness spreading, alliances are tested and great battles waged in the epic conclusion to the legendary saga.', '/customer-static/images/movie/movie15.png', '/customer-static/images/movie/banner15.png', 'New Line Cinema', 201, 'https://youtube.com/watch?v=trailer15', 4.7, 'Elijah Wood, Ian McKellen, Viggo Mortensen, Sean Astin', '2025-06-12', '2025-11-12', 'Active'),
('Schindler List', 'Oskar Schindler, a German businessman, risks everything to save over a thousand Jews from the horrors of the Holocaust. A heartbreaking yet inspiring portrayal of courage, humanity, and the impact of individual action in the darkest of times.', '/customer-static/images/movie/movie16.png', '/customer-static/images/movie/banner16.png', 'Universal Pictures', 195, 'https://youtube.com/watch?v=trailer16', 4.8, 'Liam Neeson, Ralph Fiennes, Ben Kingsley, Caroline Goodall', '2025-08-01', '2025-11-01', 'Active'),
('12 Angry Men', 'Locked in a tense jury room, twelve men must reach a unanimous verdict in a murder trial. As tempers flare and biases emerge, one juror challenges the others to reconsider what justice truly means in a life-or-death decision.', '/customer-static/images/movie/movie17.png', '/customer-static/images/movie/banner17.png', 'United Artists', 96, 'https://youtube.com/watch?v=trailer17', 4.6, 'Henry Fonda, Lee J. Cobb, Ed Begley, E.G. Marshall', '2025-08-05', '2025-11-05', 'Active'),
('Gladiator', 'When a Roman general is betrayed and his family murdered by the corrupt son of the Emperor, he comes to Rome as a gladiator to seek his revenge. Forced into slavery, Maximus rises through the ranks of the gladiatorial arena to avenge the murders and restore honor to Rome.', '/customer-static/images/movie/movie18.png', '/customer-static/images/movie/banner18.png', 'DreamWorks', 155, 'https://youtube.com/watch?v=trailer27', 4.8, 'Russell Crowe, Joaquin Phoenix, Connie Nielsen, Oliver Reed', '2025-08-10', '2025-11-10', 'Active'),
('The Silence of the Lambs', 'Clarice Starling, a top student at the FBI''s training academy, is sent to interview Dr. Hannibal Lecter, a brilliant psychiatrist and cannibalistic serial killer. She hopes to gain his insight to help track down another serial killer, "Buffalo Bill," who skins his female victims, leading to a twisted psychological battle of wits.', '/customer-static/images/movie/movie19.png', '/customer-static/images/movie/banner19.png', 'Orion Pictures', 118, 'https://youtube.com/watch?v=trailer28', 4.7, 'Jodie Foster, Anthony Hopkins, Scott Glenn, Ted Levine', '2025-08-15', '2025-11-15', 'Active'),
('Saving Private Ryan', 'Captain John Miller leads a squad of U.S. soldiers behind enemy lines during the Normandy invasion of World War II. Their perilous mission is to find and bring home Private James Ryan, the last surviving brother of four servicemen. The film is renowned for its visceral and unflinching depiction of combat.', '/customer-static/images/movie/movie20.png', '/customer-static/images/movie/banner20.png', 'DreamWorks', 169, 'https://youtube.com/watch?v=trailer29', 4.6, 'Tom Hanks, Matt Damon, Tom Sizemore, Edward Burns', '2025-08-20', '2025-11-20', 'Active'),
('Jurassic Park', 'Billionaire philanthropist John Hammond creates a theme park of cloned dinosaurs on a remote island. Before opening to the public, he invites a team of experts and his two grandchildren to experience the park. But when a power failure allows the deadly predators to roam free, the visit turns into a desperate fight for survival.', '/customer-static/images/movie/movie21.png', '/customer-static/images/movie/banner21.png', 'Universal Pictures', 127, 'https://youtube.com/watch?v=trailer30', 4.4, 'Sam Neill, Laura Dern, Jeff Goldblum, Richard Attenborough', '2025-09-01', '2025-12-01', 'Active'),
('Avatar', 'On the lush alien world of Pandora live the Na''vi, beings who appear primitive but are highly evolved. Jake Sully, a paraplegic former Marine, is sent on a mission to infiltrate their society using an "avatar" body. As he falls in love with a Na''vi woman and learns their ways, he finds himself caught between his orders and the fight to protect Pandora.', '/customer-static/images/movie/movie22.png', '/customer-static/images/movie/banner22.png', '20th Century Fox', 162, 'https://youtube.com/watch?v=trailer31', 4.3, 'Sam Worthington, Zoe Saldana, Sigourney Weaver, Stephen Lang', '2025-09-05', '2025-12-05', 'Active');

-- Movie_Genre table data (more data as requested)
INSERT INTO Movie_Genre (MovieID, GenreID) VALUES
-- Avengers: Endgame (Action, Science Fiction, Adventure)
(1, 1),
(1, 5),
(1, 6),

-- The Dark Knight (Action, Crime, Drama)
(2, 1),
(2, 9),
(2, 8),

-- Inception (Action, Science Fiction, Adventure)
(3, 1),
(3, 5),
(3, 6),

-- Parasite (Drama)
(4, 8),

-- Spider-Man: No Way Home (Action, Science Fiction, Adventure)
(5, 1),
(5, 5),
(5, 6),

-- Titanic (Romance, Drama)
(6, 4),
(6, 8),

-- The Shawshank Redemption (Drama)
(7, 8),

-- Pulp Fiction (Crime, Drama)
(8, 9),
(8, 8),

-- The Godfather (Crime, Drama)
(9, 9),
(9, 8),

-- Forrest Gump (Romance, Drama)
(10, 4),
(10, 8),

-- The Matrix (Action, Science Fiction)
(11, 1),
(11, 5),

-- Interstellar (Science Fiction, Adventure, Drama)
(12, 5),
(12, 6),
(12, 8),

-- Fight Club (Drama)
(13, 8),

-- Goodfellas (Crime, Drama)
(14, 9),
(14, 8),

-- The Lord of the Rings (Action, Adventure, Drama)
(15, 1),
(15, 6),
(15, 8),

-- Schindler's List (Drama)
(16, 8),

-- 12 Angry Men (Drama)
(17, 8),

-- Gladiator (Action, Adventure, Drama)
(18, 1),
(18, 6),
(18, 8),

-- The Silence of the Lambs (Horror, Crime, Drama)
(19, 2),
(19, 9),
(19, 8),

-- Saving Private Ryan (Action, Drama)
(20, 1),
(20, 8),

-- Jurassic Park (Science Fiction, Adventure)
(21, 5),
(21, 6),

-- Avatar (Action, Science Fiction, Adventure)
(22, 1),
(22, 5),
(22, 6);

-- Promotion table data
INSERT INTO Promotion (PromotionCode, Discount, StartTime, EndTime, Quantity, Status) VALUES
('SALE10', 10, '2025-01-01 00:00:00', '2025-03-31 23:59:59', 100, 'Available'),
('SALE20', 20, '2025-02-01 00:00:00', '2025-04-30 23:59:59', 50, 'Available'),
('WEEKEND15', 15, '2025-01-15 00:00:00', '2025-12-31 23:59:59', 200, 'Available'),
('STUDENT25', 25, '2025-01-01 00:00:00', '2025-06-30 23:59:59', 150, 'Available'),
('VIP30', 30, '2025-03-01 00:00:00', '2025-05-31 23:59:59', 30, 'Available'),
('NEWCUST5', 5, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 500, 'Available'),
('FAMILY18', 18, '2025-04-01 00:00:00', '2025-06-30 23:59:59', 75, 'Available'),
('BIRTHDAY50', 50, '2025-01-01 00:00:00', '2025-12-31 23:59:59', 20, 'Available'),
('EXPIRED10', 10, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 0, 'Expired'),
('FLASH12', 12, '2025-05-01 00:00:00', '2025-05-31 23:59:59', 80, 'Available');

-- Theater table data
INSERT INTO Theater (TheaterName, Address, Image, RoomQuantity, Status) VALUES
('CGV Times Square', '1500 Broadway, New York, NY 10036', 'cgv_timessquare.jpg', 3, 'Active'),
('CGV Beverly Center', '8500 Beverly Blvd, Los Angeles, CA 90048', 'amc_beverly.jpg', 3, 'Active'),
('CGV Union Square', '850 Broadway, New York, NY 10003', 'regal_union.jpg', 3, 'Active');

-- INSERT vào bảng Employee (Role = 'Admin', 'Staff', 'Cashier', 'Customer_Officer')
-- Lưu ý: Insert Admin trước (AdminID = NULL), sau đó insert các nhân viên khác

-- 1. Insert Admin trước
INSERT INTO Employee (Position, Status, AccountID, TheaterID, AdminID, FullName) VALUES
('Admin', 'Active', 1, 1, NULL, N'Quản trị viên chính Nguyễn Văn A'),
('Admin', 'Active', 11, 1, NULL, N'Phó quản trị viên Trần Văn B'),
('Admin', 'Active', 12, 2, NULL, N'Quản trị viên chi nhánh Lê Thị C');

-- 2. Insert các nhân viên khác (AdminID tham chiếu đến EmployeeID của Admin vừa tạo)
-- Giả sử Admin có EmployeeID = 1
INSERT INTO Employee (Position, Status, AccountID, TheaterID, AdminID, FullName) VALUES
('Staff', 'Active', 2, 1, 1, N'Nhân viên Phạm Văn D'),
('Staff', 'Active', 9, 2, 1, N'Nhân viên Hoàng Thị E'),
('Staff', 'Active', 13, 3, 3, N'Nhân viên Võ Văn F'),
('Cashier', 'Active', 3, 1, 1, N'Thu ngân Đặng Thị G'),
('Cashier', 'Active', 14, 2, 3, N'Thu ngân Bùi Văn H'),
('Cashier', 'Active', 14, 3, 3, N'Thu ngân Bùi Văn L'),
('Customer_Officer', 'Active', 4, 1, 2, N'CSKH Ngô Thị I'),
('Customer_Officer', 'Active', 15, 2, 3, N'CSKH Trương Văn J'),
('Customer_Officer', 'Active', 15, 3, 3, N'CSKH Trương Văn K');

-- Room table data
INSERT INTO Room (TheaterID, Name, Collumn, Row, TypeOfRoom, Status) VALUES
(1, 'R01', 12, 8, 'Single', 'Active'),
(1, 'R02', 7, 10, 'Single', 'Active'),
(1, 'R03', 8, 10, 'Couple', 'Active'),
(2, 'R01', 10, 6, 'Single', 'Active'),
(2, 'R02', 8, 8, 'Single', 'Active'),
(2, 'R03', 6, 10, 'Couple', 'Active'),
(3, 'R01', 12, 8, 'Single', 'Active'),
(3, 'R02', 7, 10, 'Single', 'Active'),
(3, 'R03', 6, 10, 'Couple', 'Active');

-- Room 1: Theater 1, R01, 12x8 = 96 ghế, Single
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (8 ghế)
(1, 'Single', 'A1', 0, 80000, 'Active'),
(1, 'Single', 'A2', 0, 80000, 'Active'),
(1, 'Single', 'A3', 0, 80000, 'Active'),
(1, 'Single', 'A4', 0, 80000, 'Active'),
(1, 'Single', 'A5', 0, 80000, 'Active'),
(1, 'Single', 'A6', 0, 80000, 'Active'),
(1, 'Single', 'A7', 0, 80000, 'Active'),
(1, 'Single', 'A8', 0, 80000, 'Active'),
-- Hàng B (8 ghế)
(1, 'Single', 'B1', 0, 80000, 'Active'),
(1, 'Single', 'B2', 0, 80000, 'Active'),
(1, 'Single', 'B3', 0, 80000, 'Active'),
(1, 'Single', 'B4', 0, 80000, 'Active'),
(1, 'Single', 'B5', 0, 80000, 'Active'),
(1, 'Single', 'B6', 0, 80000, 'Active'),
(1, 'Single', 'B7', 0, 80000, 'Active'),
(1, 'Single', 'B8', 0, 80000, 'Active'),
-- Hàng C (8 ghế)
(1, 'Single', 'C1', 0, 80000, 'Active'),
(1, 'Single', 'C2', 0, 80000, 'Active'),
(1, 'Single', 'C3', 0, 80000, 'Active'),
(1, 'Single', 'C4', 0, 80000, 'Active'),
(1, 'Single', 'C5', 0, 80000, 'Active'),
(1, 'Single', 'C6', 0, 80000, 'Active'),
(1, 'Single', 'C7', 0, 80000, 'Active'),
(1, 'Single', 'C8', 0, 80000, 'Active'),
-- Hàng D (8 ghế)
(1, 'Single', 'D1', 0, 80000, 'Active'),
(1, 'Single', 'D2', 0, 80000, 'Active'),
(1, 'Single', 'D3', 0, 80000, 'Active'),
(1, 'Single', 'D4', 0, 80000, 'Active'),
(1, 'Single', 'D5', 0, 80000, 'Active'),
(1, 'Single', 'D6', 0, 80000, 'Active'),
(1, 'Single', 'D7', 0, 80000, 'Active'),
(1, 'Single', 'D8', 0, 80000, 'Active'),
-- Hàng E (8 ghế)
(1, 'Single', 'E1', 0, 80000, 'Active'),
(1, 'Single', 'E2', 0, 80000, 'Active'),
(1, 'Single', 'E3', 0, 80000, 'Active'),
(1, 'Single', 'E4', 0, 80000, 'Active'),
(1, 'Single', 'E5', 0, 80000, 'Active'),
(1, 'Single', 'E6', 0, 80000, 'Active'),
(1, 'Single', 'E7', 0, 80000, 'Active'),
(1, 'Single', 'E8', 0, 80000, 'Active'),
-- Hàng F (8 ghế) - VIP
(1, 'Single', 'F1', 1, 120000, 'Active'),
(1, 'Single', 'F2', 1, 120000, 'Active'),
(1, 'Single', 'F3', 1, 120000, 'Active'),
(1, 'Single', 'F4', 1, 120000, 'Active'),
(1, 'Single', 'F5', 1, 120000, 'Active'),
(1, 'Single', 'F6', 1, 120000, 'Active'),
(1, 'Single', 'F7', 1, 120000, 'Active'),
(1, 'Single', 'F8', 1, 120000, 'Active'),
-- Hàng G (8 ghế) - VIP
(1, 'Single', 'G1', 1, 120000, 'Active'),
(1, 'Single', 'G2', 1, 120000, 'Active'),
(1, 'Single', 'G3', 1, 120000, 'Active'),
(1, 'Single', 'G4', 1, 120000, 'Active'),
(1, 'Single', 'G5', 1, 120000, 'Active'),
(1, 'Single', 'G6', 1, 120000, 'Active'),
(1, 'Single', 'G7', 1, 120000, 'Active'),
(1, 'Single', 'G8', 1, 120000, 'Active'),
-- Hàng H (8 ghế) - VIP
(1, 'Single', 'H1', 1, 120000, 'Active'),
(1, 'Single', 'H2', 1, 120000, 'Active'),
(1, 'Single', 'H3', 1, 120000, 'Active'),
(1, 'Single', 'H4', 1, 120000, 'Active'),
(1, 'Single', 'H5', 1, 120000, 'Active'),
(1, 'Single', 'H6', 1, 120000, 'Active'),
(1, 'Single', 'H7', 1, 120000, 'Active'),
(1, 'Single', 'H8', 1, 120000, 'Active'),
-- Hàng I (8 ghế) - VIP
(1, 'Single', 'I1', 1, 120000, 'Active'),
(1, 'Single', 'I2', 1, 120000, 'Active'),
(1, 'Single', 'I3', 1, 120000, 'Active'),
(1, 'Single', 'I4', 1, 120000, 'Active'),
(1, 'Single', 'I5', 1, 120000, 'Active'),
(1, 'Single', 'I6', 1, 120000, 'Active'),
(1, 'Single', 'I7', 1, 120000, 'Active'),
(1, 'Single', 'I8', 1, 120000, 'Active'),
-- Hàng J (8 ghế) - VIP
(1, 'Single', 'J1', 1, 120000, 'Active'),
(1, 'Single', 'J2', 1, 120000, 'Active'),
(1, 'Single', 'J3', 1, 120000, 'Active'),
(1, 'Single', 'J4', 1, 120000, 'Active'),
(1, 'Single', 'J5', 1, 120000, 'Active'),
(1, 'Single', 'J6', 1, 120000, 'Active'),
(1, 'Single', 'J7', 1, 120000, 'Active'),
(1, 'Single', 'J8', 1, 120000, 'Active'),
-- Hàng K (8 ghế) - VIP
(1, 'Single', 'K1', 1, 120000, 'Active'),
(1, 'Single', 'K2', 1, 120000, 'Active'),
(1, 'Single', 'K3', 1, 120000, 'Active'),
(1, 'Single', 'K4', 1, 120000, 'Active'),
(1, 'Single', 'K5', 1, 120000, 'Active'),
(1, 'Single', 'K6', 1, 120000, 'Active'),
(1, 'Single', 'K7', 1, 120000, 'Active'),
(1, 'Single', 'K8', 1, 120000, 'Active'),
-- Hàng L (8 ghế) - VIP
(1, 'Single', 'L1', 1, 120000, 'Active'),
(1, 'Single', 'L2', 1, 120000, 'Active'),
(1, 'Single', 'L3', 1, 120000, 'Active'),
(1, 'Single', 'L4', 1, 120000, 'Active'),
(1, 'Single', 'L5', 1, 120000, 'Active'),
(1, 'Single', 'L6', 1, 120000, 'Active'),
(1, 'Single', 'L7', 1, 120000, 'Active'),
(1, 'Single', 'L8', 1, 120000, 'Active');

-- Room 2: Theater 1, R02, 7x10 = 70 ghế, Single
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (10 ghế)
(2, 'Single', 'A1', 0, 80000, 'Active'),
(2, 'Single', 'A2', 0, 80000, 'Active'),
(2, 'Single', 'A3', 0, 80000, 'Active'),
(2, 'Single', 'A4', 0, 80000, 'Active'),
(2, 'Single', 'A5', 0, 80000, 'Active'),
(2, 'Single', 'A6', 0, 80000, 'Active'),
(2, 'Single', 'A7', 0, 80000, 'Active'),
(2, 'Single', 'A8', 0, 80000, 'Active'),
(2, 'Single', 'A9', 0, 80000, 'Active'),
(2, 'Single', 'A10', 0, 80000, 'Active'),
-- Hàng B (10 ghế)
(2, 'Single', 'B1', 0, 80000, 'Active'),
(2, 'Single', 'B2', 0, 80000, 'Active'),
(2, 'Single', 'B3', 0, 80000, 'Active'),
(2, 'Single', 'B4', 0, 80000, 'Active'),
(2, 'Single', 'B5', 0, 80000, 'Active'),
(2, 'Single', 'B6', 0, 80000, 'Active'),
(2, 'Single', 'B7', 0, 80000, 'Active'),
(2, 'Single', 'B8', 0, 80000, 'Active'),
(2, 'Single', 'B9', 0, 80000, 'Active'),
(2, 'Single', 'B10', 0, 80000, 'Active'),
-- Hàng C (10 ghế)
(2, 'Single', 'C1', 0, 80000, 'Active'),
(2, 'Single', 'C2', 0, 80000, 'Active'),
(2, 'Single', 'C3', 0, 80000, 'Active'),
(2, 'Single', 'C4', 0, 80000, 'Active'),
(2, 'Single', 'C5', 0, 80000, 'Active'),
(2, 'Single', 'C6', 0, 80000, 'Active'),
(2, 'Single', 'C7', 0, 80000, 'Active'),
(2, 'Single', 'C8', 0, 80000, 'Active'),
(2, 'Single', 'C9', 0, 80000, 'Active'),
(2, 'Single', 'C10', 0, 80000, 'Active'),
-- Hàng D (10 ghế)
(2, 'Single', 'D1', 0, 80000, 'Active'),
(2, 'Single', 'D2', 0, 80000, 'Active'),
(2, 'Single', 'D3', 0, 80000, 'Active'),
(2, 'Single', 'D4', 0, 80000, 'Active'),
(2, 'Single', 'D5', 0, 80000, 'Active'),
(2, 'Single', 'D6', 0, 80000, 'Active'),
(2, 'Single', 'D7', 0, 80000, 'Active'),
(2, 'Single', 'D8', 0, 80000, 'Active'),
(2, 'Single', 'D9', 0, 80000, 'Active'),
(2, 'Single', 'D10', 0, 80000, 'Active'),
-- Hàng E (10 ghế)
(2, 'Single', 'E1', 0, 80000, 'Active'),
(2, 'Single', 'E2', 0, 80000, 'Active'),
(2, 'Single', 'E3', 0, 80000, 'Active'),
(2, 'Single', 'E4', 0, 80000, 'Active'),
(2, 'Single', 'E5', 0, 80000, 'Active'),
(2, 'Single', 'E6', 0, 80000, 'Active'),
(2, 'Single', 'E7', 0, 80000, 'Active'),
(2, 'Single', 'E8', 0, 80000, 'Active'),
(2, 'Single', 'E9', 0, 80000, 'Active'),
(2, 'Single', 'E10', 0, 80000, 'Active'),
-- Hàng F (10 ghế) - VIP
(2, 'Single', 'F1', 1, 120000, 'Active'),
(2, 'Single', 'F2', 1, 120000, 'Active'),
(2, 'Single', 'F3', 1, 120000, 'Active'),
(2, 'Single', 'F4', 1, 120000, 'Active'),
(2, 'Single', 'F5', 1, 120000, 'Active'),
(2, 'Single', 'F6', 1, 120000, 'Active'),
(2, 'Single', 'F7', 1, 120000, 'Active'),
(2, 'Single', 'F8', 1, 120000, 'Active'),
(2, 'Single', 'F9', 1, 120000, 'Active'),
(2, 'Single', 'F10', 1, 120000, 'Active'),
-- Hàng G (10 ghế) - VIP
(2, 'Single', 'G1', 1, 120000, 'Active'),
(2, 'Single', 'G2', 1, 120000, 'Active'),
(2, 'Single', 'G3', 1, 120000, 'Active'),
(2, 'Single', 'G4', 1, 120000, 'Active'),
(2, 'Single', 'G5', 1, 120000, 'Active'),
(2, 'Single', 'G6', 1, 120000, 'Active'),
(2, 'Single', 'G7', 1, 120000, 'Active'),
(2, 'Single', 'G8', 1, 120000, 'Active'),
(2, 'Single', 'G9', 1, 120000, 'Active'),
(2, 'Single', 'G10', 1, 120000, 'Active');

-- Room 3: Theater 1, R03, 10x6 = 60 ghế, Couple
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (6 ghế couple = 3 đôi)
(4, 'Single', 'A1', 0, 150000, 'Active'),
(4, 'Single', 'A2', 0, 150000, 'Active'),
(4, 'Single', 'A3', 0, 150000, 'Active'),
(4, 'Single', 'A4', 0, 150000, 'Active'),
(4, 'Single', 'A5', 0, 150000, 'Active'),
(4, 'Single', 'A6', 0, 150000, 'Active'),
-- Hàng B (6 ghế couple)
(4, 'Single', 'B1', 0, 150000, 'Active'),
(4, 'Single', 'B2', 0, 150000, 'Active'),
(4, 'Single', 'B3', 0, 150000, 'Active'),
(4, 'Single', 'B4', 0, 150000, 'Active'),
(4, 'Single', 'B5', 0, 150000, 'Active'),
(4, 'Single', 'B6', 0, 150000, 'Active'),
-- Hàng C (6 ghế couple)
(4, 'Single', 'C1', 0, 150000, 'Active'),
(4, 'Single', 'C2', 0, 150000, 'Active'),
(4, 'Single', 'C3', 0, 150000, 'Active'),
(4, 'Single', 'C4', 0, 150000, 'Active'),
(4, 'Single', 'C5', 0, 150000, 'Active'),
(4, 'Single', 'C6', 0, 150000, 'Active'),
-- Hàng D (6 ghế couple)
(4, 'Single', 'D1', 0, 150000, 'Active'),
(4, 'Single', 'D2', 0, 150000, 'Active'),
(4, 'Single', 'D3', 0, 150000, 'Active'),
(4, 'Single', 'D4', 0, 150000, 'Active'),
(4, 'Single', 'D5', 0, 150000, 'Active'),
(4, 'Single', 'D6', 0, 150000, 'Active'),
-- Hàng E (6 ghế couple)
(4, 'Single', 'E1', 0, 150000, 'Active'),
(4, 'Single', 'E2', 0, 150000, 'Active'),
(4, 'Single', 'E3', 0, 150000, 'Active'),
(4, 'Single', 'E4', 0, 150000, 'Active'),
(4, 'Single', 'E5', 0, 150000, 'Active'),
(4, 'Single', 'E6', 0, 150000, 'Active'),
-- Hàng F (6 ghế couple - VIP)
(4, 'Single', 'F1', 1, 220000, 'Active'),
(4, 'Single', 'F2', 1, 220000, 'Active'),
(4, 'Single', 'F3', 1, 220000, 'Active'),
(4, 'Single', 'F4', 1, 220000, 'Active'),
(4, 'Single', 'F5', 1, 220000, 'Active'),
(4, 'Single', 'F6', 1, 220000, 'Active'),
-- Hàng G (6 ghế couple - VIP)
(4, 'Single', 'G1', 1, 220000, 'Active'),
(4, 'Single', 'G2', 1, 220000, 'Active'),
(4, 'Single', 'G3', 1, 220000, 'Active'),
(4, 'Single', 'G4', 1, 220000, 'Active'),
(4, 'Single', 'G5', 1, 220000, 'Active'),
(4, 'Single', 'G6', 1, 220000, 'Active'),
-- Hàng H (6 ghế couple - VIP)
(4, 'Single', 'H1', 1, 220000, 'Active'),
(4, 'Single', 'H2', 1, 220000, 'Active'),
(4, 'Single', 'H3', 1, 220000, 'Active'),
(4, 'Single', 'H4', 1, 220000, 'Active'),
(4, 'Single', 'H5', 1, 220000, 'Active'),
(4, 'Single', 'H6', 1, 220000, 'Active'),
-- Hàng I (6 ghế couple - VIP)
(4, 'Single', 'I1', 1, 220000, 'Active'),
(4, 'Single', 'I2', 1, 220000, 'Active'),
(4, 'Single', 'I3', 1, 220000, 'Active'),
(4, 'Single', 'I4', 1, 220000, 'Active'),
(4, 'Single', 'I5', 1, 220000, 'Active'),
(4, 'Single', 'I6', 1, 220000, 'Active'),
-- Hàng J (6 ghế couple - VIP)
(4, 'Single', 'J1', 1, 220000, 'Active'),
(4, 'Single', 'J2', 1, 220000, 'Active'),
(4, 'Single', 'J3', 1, 220000, 'Active'),
(4, 'Single', 'J4', 1, 220000, 'Active'),
(4, 'Single', 'J5', 1, 220000, 'Active'),
(4, 'Single', 'J6', 1, 220000, 'Active');

-- Room 4: Theater 2, R01, 8x10 = 80 ghế, Single
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (12 ghế)
(4, 'Single', 'A1', 0, 80000, 'Active'),
(4, 'Single', 'A2', 0, 80000, 'Active'),
(4, 'Single', 'A3', 0, 80000, 'Active'),
(4, 'Single', 'A4', 0, 80000, 'Active'),
(4, 'Single', 'A5', 0, 80000, 'Active'),
(4, 'Single', 'A6', 0, 80000, 'Active'),
(4, 'Single', 'A7', 0, 80000, 'Active'),
(4, 'Single', 'A8', 0, 80000, 'Active'),
(4, 'Single', 'A9', 0, 80000, 'Active'),
(4, 'Single', 'A10', 0, 80000, 'Active'),
(4, 'Single', 'A11', 0, 80000, 'Active'),
(4, 'Single', 'A12', 0, 80000, 'Active'),
-- Hàng B (12 ghế)
(4, 'Single', 'B1', 0, 80000, 'Active'),
(4, 'Single', 'B2', 0, 80000, 'Active'),
(4, 'Single', 'B3', 0, 80000, 'Active'),
(4, 'Single', 'B4', 0, 80000, 'Active'),
(4, 'Single', 'B5', 0, 80000, 'Active'),
(4, 'Single', 'B6', 0, 80000, 'Active'),
(4, 'Single', 'B7', 0, 80000, 'Active'),
(4, 'Single', 'B8', 0, 80000, 'Active'),
(4, 'Single', 'B9', 0, 80000, 'Active'),
(4, 'Single', 'B10', 0, 80000, 'Active'),
(4, 'Single', 'B11', 0, 80000, 'Active'),
(4, 'Single', 'B12', 0, 80000, 'Active'),
-- Hàng C (12 ghế)
(4, 'Single', 'C1', 0, 80000, 'Active'),
(4, 'Single', 'C2', 0, 80000, 'Active'),
(4, 'Single', 'C3', 0, 80000, 'Active'),
(4, 'Single', 'C4', 0, 80000, 'Active'),
(4, 'Single', 'C5', 0, 80000, 'Active'),
(4, 'Single', 'C6', 0, 80000, 'Active'),
(4, 'Single', 'C7', 0, 80000, 'Active'),
(4, 'Single', 'C8', 0, 80000, 'Active'),
(4, 'Single', 'C9', 0, 80000, 'Active'),
(4, 'Single', 'C10', 0, 80000, 'Active'),
(4, 'Single', 'C11', 0, 80000, 'Active'),
(4, 'Single', 'C12', 0, 80000, 'Active'),
-- Hàng D (12 ghế)
(4, 'Single', 'D1', 0, 80000, 'Active'),
(4, 'Single', 'D2', 0, 80000, 'Active'),
(4, 'Single', 'D3', 0, 80000, 'Active'),
(4, 'Single', 'D4', 0, 80000, 'Active'),
(4, 'Single', 'D5', 0, 80000, 'Active'),
(4, 'Single', 'D6', 0, 80000, 'Active'),
(4, 'Single', 'D7', 0, 80000, 'Active'),
(4, 'Single', 'D8', 0, 80000, 'Active'),
(4, 'Single', 'D9', 0, 80000, 'Active'),
(4, 'Single', 'D10', 0, 80000, 'Active'),
(4, 'Single', 'D11', 0, 80000, 'Active'),
(4, 'Single', 'D12', 0, 80000, 'Active'),
-- Hàng E (12 ghế)
(4, 'Single', 'E1', 1, 120000, 'Active'),
(4, 'Single', 'E2', 1, 120000, 'Active'),
(4, 'Single', 'E3', 1, 120000, 'Active'),
(4, 'Single', 'E4', 1, 120000, 'Active'),
(4, 'Single', 'E5', 1, 120000, 'Active'),
(4, 'Single', 'E6', 1, 120000, 'Active'),
(4, 'Single', 'E7', 1, 120000, 'Active'),
(4, 'Single', 'E8', 1, 120000, 'Active'),
(4, 'Single', 'E9', 1, 120000, 'Active'),
(4, 'Single', 'E10', 1, 120000, 'Active'),
(4, 'Single', 'E11', 1, 120000, 'Active'),
(4, 'Single', 'E12', 1, 120000, 'Active');

-- Room 5: Theater 2, R02, 8x8 = 64 ghế, Single
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (8 ghế)
(5, 'Single', 'A1', 0, 80000, 'Active'),
(5, 'Single', 'A2', 0, 80000, 'Active'),
(5, 'Single', 'A3', 0, 80000, 'Active'),
(5, 'Single', 'A4', 0, 80000, 'Active'),
(5, 'Single', 'A5', 0, 80000, 'Active'),
(5, 'Single', 'A6', 0, 80000, 'Active'),
(5, 'Single', 'A7', 0, 80000, 'Active'),
(5, 'Single', 'A8', 0, 80000, 'Active'),
-- Hàng B (8 ghế)
(5, 'Single', 'B1', 0, 80000, 'Active'),
(5, 'Single', 'B2', 0, 80000, 'Active'),
(5, 'Single', 'B3', 0, 80000, 'Active'),
(5, 'Single', 'B4', 0, 80000, 'Active'),
(5, 'Single', 'B5', 0, 80000, 'Active'),
(5, 'Single', 'B6', 0, 80000, 'Active'),
(5, 'Single', 'B7', 0, 80000, 'Active'),
(5, 'Single', 'B8', 0, 80000, 'Active'),
-- Hàng C (8 ghế)
(5, 'Single', 'C1', 0, 80000, 'Active'),
(5, 'Single', 'C2', 0, 80000, 'Active'),
(5, 'Single', 'C3', 0, 80000, 'Active'),
(5, 'Single', 'C4', 0, 80000, 'Active'),
(5, 'Single', 'C5', 0, 80000, 'Active'),
(5, 'Single', 'C6', 0, 80000, 'Active'),
(5, 'Single', 'C7', 0, 80000, 'Active'),
(5, 'Single', 'C8', 0, 80000, 'Active'),
-- Hàng D (8 ghế)
(5, 'Single', 'D1', 0, 80000, 'Active'),
(5, 'Single', 'D2', 0, 80000, 'Active'),
(5, 'Single', 'D3', 0, 80000, 'Active'),
(5, 'Single', 'D4', 0, 80000, 'Active'),
(5, 'Single', 'D5', 0, 80000, 'Active'),
(5, 'Single', 'D6', 0, 80000, 'Active'),
(5, 'Single', 'D7', 0, 80000, 'Active'),
(5, 'Single', 'D8', 0, 80000, 'Active'),
-- Hàng E (8 ghế)
(5, 'Single', 'E1', 0, 80000, 'Active'),
(5, 'Single', 'E2', 0, 80000, 'Active'),
(5, 'Single', 'E3', 0, 80000, 'Active'),
(5, 'Single', 'E4', 0, 80000, 'Active'),
(5, 'Single', 'E5', 0, 80000, 'Active'),
(5, 'Single', 'E6', 0, 80000, 'Active'),
(5, 'Single', 'E7', 0, 80000, 'Active'),
(5, 'Single', 'E8', 0, 80000, 'Active'),
-- Hàng F (8 ghế)
(5, 'Single', 'F1', 0, 80000, 'Active'),
(5, 'Single', 'F2', 0, 80000, 'Active'),
(5, 'Single', 'F3', 0, 80000, 'Active'),
(5, 'Single', 'F4', 0, 80000, 'Active'),
(5, 'Single', 'F5', 0, 80000, 'Active'),
(5, 'Single', 'F6', 0, 80000, 'Active'),
(5, 'Single', 'F7', 0, 80000, 'Active'),
(5, 'Single', 'F8', 0, 80000, 'Active'),
-- Hàng G (8 ghế) - VIP
(5, 'Single', 'G1', 1, 120000, 'Active'),
(5, 'Single', 'G2', 1, 120000, 'Active'),
(5, 'Single', 'G3', 1, 120000, 'Active'),
(5, 'Single', 'G4', 1, 120000, 'Active'),
(5, 'Single', 'G5', 1, 120000, 'Active'),
(5, 'Single', 'G6', 1, 120000, 'Active'),
(5, 'Single', 'G7', 1, 120000, 'Active'),
(5, 'Single', 'G8', 1, 120000, 'Active'),
-- Hàng H (8 ghế) - VIP
(5, 'Single', 'H1', 1, 120000, 'Active'),
(5, 'Single', 'H2', 1, 120000, 'Active'),
(5, 'Single', 'H3', 1, 120000, 'Active'),
(5, 'Single', 'H4', 1, 120000, 'Active'),
(5, 'Single', 'H5', 1, 120000, 'Active'),
(5, 'Single', 'H6', 1, 120000, 'Active'),
(5, 'Single', 'H7', 1, 120000, 'Active'),
(5, 'Single', 'H8', 1, 120000, 'Active');

-- Room 6: Theater 2, R03, 10x6 = 60 ghế, Couple
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (6 ghế couple = 3 đôi)
(6, 'Couple', 'A1', 0, 150000, 'Active'),
(6, 'Couple', 'A2', 0, 150000, 'Active'),
(6, 'Couple', 'A3', 0, 150000, 'Active'),
(6, 'Couple', 'A4', 0, 150000, 'Active'),
(6, 'Couple', 'A5', 0, 150000, 'Active'),
(6, 'Couple', 'A6', 0, 150000, 'Active'),
-- Hàng B (6 ghế couple)
(6, 'Couple', 'B1', 0, 150000, 'Active'),
(6, 'Couple', 'B2', 0, 150000, 'Active'),
(6, 'Couple', 'B3', 0, 150000, 'Active'),
(6, 'Couple', 'B4', 0, 150000, 'Active'),
(6, 'Couple', 'B5', 0, 150000, 'Active'),
(6, 'Couple', 'B6', 0, 150000, 'Active'),
-- Hàng C (6 ghế couple)
(6, 'Couple', 'C1', 0, 150000, 'Active'),
(6, 'Couple', 'C2', 0, 150000, 'Active'),
(6, 'Couple', 'C3', 0, 150000, 'Active'),
(6, 'Couple', 'C4', 0, 150000, 'Active'),
(6, 'Couple', 'C5', 0, 150000, 'Active'),
(6, 'Couple', 'C6', 0, 150000, 'Active'),
-- Hàng D (6 ghế couple)
(6, 'Couple', 'D1', 0, 150000, 'Active'),
(6, 'Couple', 'D2', 0, 150000, 'Active'),
(6, 'Couple', 'D3', 0, 150000, 'Active'),
(6, 'Couple', 'D4', 0, 150000, 'Active'),
(6, 'Couple', 'D5', 0, 150000, 'Active'),
(6, 'Couple', 'D6', 0, 150000, 'Active'),
-- Hàng E (6 ghế couple)
(6, 'Couple', 'E1', 0, 150000, 'Active'),
(6, 'Couple', 'E2', 0, 150000, 'Active'),
(6, 'Couple', 'E3', 0, 150000, 'Active'),
(6, 'Couple', 'E4', 0, 150000, 'Active'),
(6, 'Couple', 'E5', 0, 150000, 'Active'),
(6, 'Couple', 'E6', 0, 150000, 'Active'),
-- Hàng F (6 ghế couple - VIP)
(6, 'Couple', 'F1', 1, 220000, 'Active'),
(6, 'Couple', 'F2', 1, 220000, 'Active'),
(6, 'Couple', 'F3', 1, 220000, 'Active'),
(6, 'Couple', 'F4', 1, 220000, 'Active'),
(6, 'Couple', 'F5', 1, 220000, 'Active'),
(6, 'Couple', 'F6', 1, 220000, 'Active'),
-- Hàng G (6 ghế couple - VIP)
(6, 'Couple', 'G1', 1, 220000, 'Active'),
(6, 'Couple', 'G2', 1, 220000, 'Active'),
(6, 'Couple', 'G3', 1, 220000, 'Active'),
(6, 'Couple', 'G4', 1, 220000, 'Active'),
(6, 'Couple', 'G5', 1, 220000, 'Active'),
(6, 'Couple', 'G6', 1, 220000, 'Active'),
-- Hàng H (6 ghế couple - VIP)
(6, 'Couple', 'H1', 1, 220000, 'Active'),
(6, 'Couple', 'H2', 1, 220000, 'Active'),
(6, 'Couple', 'H3', 1, 220000, 'Active'),
(6, 'Couple', 'H4', 1, 220000, 'Active'),
(6, 'Couple', 'H5', 1, 220000, 'Active'),
(6, 'Couple', 'H6', 1, 220000, 'Active'),
-- Hàng I (6 ghế couple - VIP)
(6, 'Couple', 'I1', 1, 220000, 'Active'),
(6, 'Couple', 'I2', 1, 220000, 'Active'),
(6, 'Couple', 'I3', 1, 220000, 'Active'),
(6, 'Couple', 'I4', 1, 220000, 'Active'),
(6, 'Couple', 'I5', 1, 220000, 'Active'),
(6, 'Couple', 'I6', 1, 220000, 'Active'),
-- Hàng J (6 ghế couple - VIP)
(6, 'Couple', 'J1', 1, 220000, 'Active'),
(6, 'Couple', 'J2', 1, 220000, 'Active'),
(6, 'Couple', 'J3', 1, 220000, 'Active'),
(6, 'Couple', 'J4', 1, 220000, 'Active'),
(6, 'Couple', 'J5', 1, 220000, 'Active'),
(6, 'Couple', 'J6', 1, 220000, 'Active');

-- Room 7: Theater 3, R01, 12x8 = 96 ghế, Single
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (8 ghế)
(7, 'Single', 'A1', 0, 80000, 'Active'),
(7, 'Single', 'A2', 0, 80000, 'Active'),
(7, 'Single', 'A3', 0, 80000, 'Active'),
(7, 'Single', 'A4', 0, 80000, 'Active'),
(7, 'Single', 'A5', 0, 80000, 'Active'),
(7, 'Single', 'A6', 0, 80000, 'Active'),
(7, 'Single', 'A7', 0, 80000, 'Active'),
(7, 'Single', 'A8', 0, 80000, 'Active'),
-- Hàng B (8 ghế)
(7, 'Single', 'B1', 0, 80000, 'Active'),
(7, 'Single', 'B2', 0, 80000, 'Active'),
(7, 'Single', 'B3', 0, 80000, 'Active'),
(7, 'Single', 'B4', 0, 80000, 'Active'),
(7, 'Single', 'B5', 0, 80000, 'Active'),
(7, 'Single', 'B6', 0, 80000, 'Active'),
(7, 'Single', 'B7', 0, 80000, 'Active'),
(7, 'Single', 'B8', 0, 80000, 'Active'),
-- Hàng C (8 ghế)
(7, 'Single', 'C1', 0, 80000, 'Active'),
(7, 'Single', 'C2', 0, 80000, 'Active'),
(7, 'Single', 'C3', 0, 80000, 'Active'),
(7, 'Single', 'C4', 0, 80000, 'Active'),
(7, 'Single', 'C5', 0, 80000, 'Active'),
(7, 'Single', 'C6', 0, 80000, 'Active'),
(7, 'Single', 'C7', 0, 80000, 'Active'),
(7, 'Single', 'C8', 0, 80000, 'Active'),
-- Hàng D (8 ghế)
(7, 'Single', 'D1', 0, 80000, 'Active'),
(7, 'Single', 'D2', 0, 80000, 'Active'),
(7, 'Single', 'D3', 0, 80000, 'Active'),
(7, 'Single', 'D4', 0, 80000, 'Active'),
(7, 'Single', 'D5', 0, 80000, 'Active'),
(7, 'Single', 'D6', 0, 80000, 'Active'),
(7, 'Single', 'D7', 0, 80000, 'Active'),
(7, 'Single', 'D8', 0, 80000, 'Active'),
-- Hàng E (8 ghế)
(7, 'Single', 'E1', 0, 80000, 'Active'),
(7, 'Single', 'E2', 0, 80000, 'Active'),
(7, 'Single', 'E3', 0, 80000, 'Active'),
(7, 'Single', 'E4', 0, 80000, 'Active'),
(7, 'Single', 'E5', 0, 80000, 'Active'),
(7, 'Single', 'E6', 0, 80000, 'Active'),
(7, 'Single', 'E7', 0, 80000, 'Active'),
(7, 'Single', 'E8', 0, 80000, 'Active'),
-- Hàng F (8 ghế) - VIP
(7, 'Single', 'F1', 1, 120000, 'Active'),
(7, 'Single', 'F2', 1, 120000, 'Active'),
(7, 'Single', 'F3', 1, 120000, 'Active'),
(7, 'Single', 'F4', 1, 120000, 'Active'),
(7, 'Single', 'F5', 1, 120000, 'Active'),
(7, 'Single', 'F6', 1, 120000, 'Active'),
(7, 'Single', 'F7', 1, 120000, 'Active'),
(7, 'Single', 'F8', 1, 120000, 'Active'),
-- Hàng G (8 ghế) - VIP
(7, 'Single', 'G1', 1, 120000, 'Active'),
(7, 'Single', 'G2', 1, 120000, 'Active'),
(7, 'Single', 'G3', 1, 120000, 'Active'),
(7, 'Single', 'G4', 1, 120000, 'Active'),
(7, 'Single', 'G5', 1, 120000, 'Active'),
(7, 'Single', 'G6', 1, 120000, 'Active'),
(7, 'Single', 'G7', 1, 120000, 'Active'),
(7, 'Single', 'G8', 1, 120000, 'Active'),
-- Hàng H (8 ghế) - VIP
(7, 'Single', 'H1', 1, 120000, 'Active'),
(7, 'Single', 'H2', 1, 120000, 'Active'),
(7, 'Single', 'H3', 1, 120000, 'Active'),
(7, 'Single', 'H4', 1, 120000, 'Active'),
(7, 'Single', 'H5', 1, 120000, 'Active'),
(7, 'Single', 'H6', 1, 120000, 'Active'),
(7, 'Single', 'H7', 1, 120000, 'Active'),
(7, 'Single', 'H8', 1, 120000, 'Active'),
-- Hàng I (8 ghế) - VIP
(7, 'Single', 'I1', 1, 120000, 'Active'),
(7, 'Single', 'I2', 1, 120000, 'Active'),
(7, 'Single', 'I3', 1, 120000, 'Active'),
(7, 'Single', 'I4', 1, 120000, 'Active'),
(7, 'Single', 'I5', 1, 120000, 'Active'),
(7, 'Single', 'I6', 1, 120000, 'Active'),
(7, 'Single', 'I7', 1, 120000, 'Active'),
(7, 'Single', 'I8', 1, 120000, 'Active'),
-- Hàng J (8 ghế) - VIP
(7, 'Single', 'J1', 1, 120000, 'Active'),
(7, 'Single', 'J2', 1, 120000, 'Active'),
(7, 'Single', 'J3', 1, 120000, 'Active'),
(7, 'Single', 'J4', 1, 120000, 'Active'),
(7, 'Single', 'J5', 1, 120000, 'Active'),
(7, 'Single', 'J6', 1, 120000, 'Active'),
(7, 'Single', 'J7', 1, 120000, 'Active'),
(7, 'Single', 'J8', 1, 120000, 'Active'),
-- Hàng K (8 ghế) - VIP
(7, 'Single', 'K1', 1, 120000, 'Active'),
(7, 'Single', 'K2', 1, 120000, 'Active'),
(7, 'Single', 'K3', 1, 120000, 'Active'),
(7, 'Single', 'K4', 1, 120000, 'Active'),
(7, 'Single', 'K5', 1, 120000, 'Active'),
(7, 'Single', 'K6', 1, 120000, 'Active'),
(7, 'Single', 'K7', 1, 120000, 'Active'),
(7, 'Single', 'K8', 1, 120000, 'Active'),
-- Hàng L (8 ghế) - VIP
(7, 'Single', 'L1', 1, 120000, 'Active'),
(7, 'Single', 'L2', 1, 120000, 'Active'),
(7, 'Single', 'L3', 1, 120000, 'Active'),
(7, 'Single', 'L4', 1, 120000, 'Active'),
(7, 'Single', 'L5', 1, 120000, 'Active'),
(7, 'Single', 'L6', 1, 120000, 'Active'),
(7, 'Single', 'L7', 1, 120000, 'Active'),
(7, 'Single', 'L8', 1, 120000, 'Active');

-- Room 8: Theater 3, R02, 7x10 = 6 ghế, Single
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (10 ghế)
(8, 'Single', 'A1', 0, 80000, 'Active'),
(8, 'Single', 'A2', 0, 80000, 'Active'),
(8, 'Single', 'A3', 0, 80000, 'Active'),
(8, 'Single', 'A4', 0, 80000, 'Active'),
(8, 'Single', 'A5', 0, 80000, 'Active'),
(8, 'Single', 'A6', 0, 80000, 'Active'),
(8, 'Single', 'A7', 0, 80000, 'Active'),
(8, 'Single', 'A8', 0, 80000, 'Active'),
(8, 'Single', 'A9', 0, 80000, 'Active'),
(8, 'Single', 'A10', 0, 80000, 'Active'),
-- Hàng B (10 ghế)
(8, 'Single', 'B1', 0, 80000, 'Active'),
(8, 'Single', 'B2', 0, 80000, 'Active'),
(8, 'Single', 'B3', 0, 80000, 'Active'),
(8, 'Single', 'B4', 0, 80000, 'Active'),
(8, 'Single', 'B5', 0, 80000, 'Active'),
(8, 'Single', 'B6', 0, 80000, 'Active'),
(8, 'Single', 'B7', 0, 80000, 'Active'),
(8, 'Single', 'B8', 0, 80000, 'Active'),
(8, 'Single', 'B9', 0, 80000, 'Active'),
(8, 'Single', 'B10', 0, 80000, 'Active'),
-- Hàng C (10 ghế)
(8, 'Single', 'C1', 0, 80000, 'Active'),
(8, 'Single', 'C2', 0, 80000, 'Active'),
(8, 'Single', 'C3', 0, 80000, 'Active'),
(8, 'Single', 'C4', 0, 80000, 'Active'),
(8, 'Single', 'C5', 0, 80000, 'Active'),
(8, 'Single', 'C6', 0, 80000, 'Active'),
(8, 'Single', 'C7', 0, 80000, 'Active'),
(8, 'Single', 'C8', 0, 80000, 'Active'),
(8, 'Single', 'C9', 0, 80000, 'Active'),
(8, 'Single', 'C10', 0, 80000, 'Active'),
-- Hàng D (10 ghế)
(8, 'Single', 'D1', 0, 80000, 'Active'),
(8, 'Single', 'D2', 0, 80000, 'Active'),
(8, 'Single', 'D3', 0, 80000, 'Active'),
(8, 'Single', 'D4', 0, 80000, 'Active'),
(8, 'Single', 'D5', 0, 80000, 'Active'),
(8, 'Single', 'D6', 0, 80000, 'Active'),
(8, 'Single', 'D7', 0, 80000, 'Active'),
(8, 'Single', 'D8', 0, 80000, 'Active'),
(8, 'Single', 'D9', 0, 80000, 'Active'),
(8, 'Single', 'D10', 0, 80000, 'Active'),
-- Hàng E (10 ghế)
(8, 'Single', 'E1', 0, 80000, 'Active'),
(8, 'Single', 'E2', 0, 80000, 'Active'),
(8, 'Single', 'E3', 0, 80000, 'Active'),
(8, 'Single', 'E4', 0, 80000, 'Active'),
(8, 'Single', 'E5', 0, 80000, 'Active'),
(8, 'Single', 'E6', 0, 80000, 'Active'),
(8, 'Single', 'E7', 0, 80000, 'Active'),
(8, 'Single', 'E8', 0, 80000, 'Active'),
(8, 'Single', 'E9', 0, 80000, 'Active'),
(8, 'Single', 'E10', 0, 80000, 'Active'),
-- Hàng F (10 ghế) - VIP
(8, 'Single', 'F1', 1, 120000, 'Active'),
(8, 'Single', 'F2', 1, 120000, 'Active'),
(8, 'Single', 'F3', 1, 120000, 'Active'),
(8, 'Single', 'F4', 1, 120000, 'Active'),
(8, 'Single', 'F5', 1, 120000, 'Active'),
(8, 'Single', 'F6', 1, 120000, 'Active'),
(8, 'Single', 'F7', 1, 120000, 'Active'),
(8, 'Single', 'F8', 1, 120000, 'Active'),
(8, 'Single', 'F9', 1, 120000, 'Active'),
(8, 'Single', 'F10', 1, 120000, 'Active'),
-- Hàng G (10 ghế) - VIP
(8, 'Single', 'G1', 1, 120000, 'Active'),
(8, 'Single', 'G2', 1, 120000, 'Active'),
(8, 'Single', 'G3', 1, 120000, 'Active'),
(8, 'Single', 'G4', 1, 120000, 'Active'),
(8, 'Single', 'G5', 1, 120000, 'Active'),
(8, 'Single', 'G6', 1, 120000, 'Active'),
(8, 'Single', 'G7', 1, 120000, 'Active'),
(8, 'Single', 'G8', 1, 120000, 'Active'),
(8, 'Single', 'G9', 1, 120000, 'Active'),
(8, 'Single', 'G10', 1, 120000, 'Active');

-- Room 9: Theater 3, R03, 6x10 = 60 ghế, Couple
INSERT INTO Seat (RoomID, SeatType, Position, IsVIP, UnitPrice, Status) VALUES
-- Hàng A (6 ghế couple = 3 đôi)
(9, 'Couple', 'A1', 0, 150000, 'Active'),
(9, 'Couple', 'A2', 0, 150000, 'Active'),
(9, 'Couple', 'A3', 0, 150000, 'Active'),
(9, 'Couple', 'A4', 0, 150000, 'Active'),
(9, 'Couple', 'A5', 0, 150000, 'Active'),
(9, 'Couple', 'A6', 0, 150000, 'Active'),
-- Hàng B (6 ghế couple)
(9, 'Couple', 'B1', 0, 150000, 'Active'),
(9, 'Couple', 'B2', 0, 150000, 'Active'),
(9, 'Couple', 'B3', 0, 150000, 'Active'),
(9, 'Couple', 'B4', 0, 150000, 'Active'),
(9, 'Couple', 'B5', 0, 150000, 'Active'),
(9, 'Couple', 'B6', 0, 150000, 'Active'),
-- Hàng C (6 ghế couple)
(9, 'Couple', 'C1', 0, 150000, 'Active'),
(9, 'Couple', 'C2', 0, 150000, 'Active'),
(9, 'Couple', 'C3', 0, 150000, 'Active'),
(9, 'Couple', 'C4', 0, 150000, 'Active'),
(9, 'Couple', 'C5', 0, 150000, 'Active'),
(9, 'Couple', 'C6', 0, 150000, 'Active'),
-- Hàng D (6 ghế couple)
(9, 'Couple', 'D1', 0, 150000, 'Active'),
(9, 'Couple', 'D2', 0, 150000, 'Active'),
(9, 'Couple', 'D3', 0, 150000, 'Active'),
(9, 'Couple', 'D4', 0, 150000, 'Active'),
(9, 'Couple', 'D5', 0, 150000, 'Active'),
(9, 'Couple', 'D6', 0, 150000, 'Active'),
-- Hàng E (6 ghế couple)
(9, 'Couple', 'E1', 0, 150000, 'Active'),
(9, 'Couple', 'E2', 0, 150000, 'Active'),
(9, 'Couple', 'E3', 0, 150000, 'Active'),
(9, 'Couple', 'E4', 0, 150000, 'Active'),
(9, 'Couple', 'E5', 0, 150000, 'Active'),
(9, 'Couple', 'E6', 0, 150000, 'Active'),
-- Hàng F (6 ghế couple - VIP)
(9, 'Couple', 'F1', 1, 220000, 'Active'),
(9, 'Couple', 'F2', 1, 220000, 'Active'),
(9, 'Couple', 'F3', 1, 220000, 'Active'),
(9, 'Couple', 'F4', 1, 220000, 'Active'),
(9, 'Couple', 'F5', 1, 220000, 'Active'),
(9, 'Couple', 'F6', 1, 220000, 'Active'),
-- Hàng G (6 ghế couple - VIP)
(9, 'Couple', 'G1', 1, 220000, 'Active'),
(9, 'Couple', 'G2', 1, 220000, 'Active'),
(9, 'Couple', 'G3', 1, 220000, 'Active'),
(9, 'Couple', 'G4', 1, 220000, 'Active'),
(9, 'Couple', 'G5', 1, 220000, 'Active'),
(9, 'Couple', 'G6', 1, 220000, 'Active'),
-- Hàng H (6 ghế couple - VIP)
(9, 'Couple', 'H1', 1, 220000, 'Active'),
(9, 'Couple', 'H2', 1, 220000, 'Active'),
(9, 'Couple', 'H3', 1, 220000, 'Active'),
(9, 'Couple', 'H4', 1, 220000, 'Active'),
(9, 'Couple', 'H5', 1, 220000, 'Active'),
(9, 'Couple', 'H6', 1, 220000, 'Active'),
-- Hàng I (6 ghế couple - VIP)
(9, 'Couple', 'I1', 1, 220000, 'Active'),
(9, 'Couple', 'I2', 1, 220000, 'Active'),
(9, 'Couple', 'I3', 1, 220000, 'Active'),
(9, 'Couple', 'I4', 1, 220000, 'Active'),
(9, 'Couple', 'I5', 1, 220000, 'Active'),
(9, 'Couple', 'I6', 1, 220000, 'Active'),
-- Hàng J (6 ghế couple - VIP)
(9, 'Couple', 'J1', 1, 220000, 'Active'),
(9, 'Couple', 'J2', 1, 220000, 'Active'),
(9, 'Couple', 'J3', 1, 220000, 'Active'),
(9, 'Couple', 'J4', 1, 220000, 'Active'),
(9, 'Couple', 'J5', 1, 220000, 'Active'),
(9, 'Couple', 'J6', 1, 220000, 'Active');

-- Schedule table data
INSERT INTO Schedule (StartTime, EndTime, MovieID, RoomID, Status) VALUES
-- Chủ Nhật, 15/06/2025 (Phim: 1, 2, 3, 4, 5, 7, 8, 9, 10)
('2025-06-15 09:00:00', DATEADD(minute, 181, '2025-06-15 09:00:00'), 1, 1, 'Active'),
('2025-06-15 09:30:00', DATEADD(minute, 152, '2025-06-15 09:30:00'), 2, 2, 'Active'),
('2025-06-15 10:00:00', DATEADD(minute, 148, '2025-06-15 10:00:00'), 3, 3, 'Active'),
('2025-06-15 10:30:00', DATEADD(minute, 132, '2025-06-15 10:30:00'), 4, 4, 'Active'),
('2025-06-15 11:00:00', DATEADD(minute, 148, '2025-06-15 11:00:00'), 5, 5, 'Active'),
('2025-06-15 11:30:00', DATEADD(minute, 142, '2025-06-15 11:30:00'), 7, 6, 'Active'),
('2025-06-15 12:00:00', DATEADD(minute, 154, '2025-06-15 12:00:00'), 8, 7, 'Active'),
('2025-06-15 12:30:00', DATEADD(minute, 175, '2025-06-15 12:30:00'), 9, 8, 'Active'),
('2025-06-15 13:00:00', DATEADD(minute, 142, '2025-06-15 13:00:00'), 10, 9, 'Active'),
('2025-06-15 14:00:00', DATEADD(minute, 181, '2025-06-15 14:00:00'), 1, 1, 'Active'),
('2025-06-15 14:30:00', DATEADD(minute, 152, '2025-06-15 14:30:00'), 2, 2, 'Active'),
('2025-06-15 15:00:00', DATEADD(minute, 148, '2025-06-15 15:00:00'), 3, 3, 'Active'),
('2025-06-15 15:30:00', DATEADD(minute, 132, '2025-06-15 15:30:00'), 4, 4, 'Active'),
('2025-06-15 16:00:00', DATEADD(minute, 148, '2025-06-15 16:00:00'), 5, 5, 'Active'),
('2025-06-15 16:30:00', DATEADD(minute, 142, '2025-06-15 16:30:00'), 7, 6, 'Active'),
('2025-06-15 17:00:00', DATEADD(minute, 154, '2025-06-15 17:00:00'), 8, 7, 'Active'),
('2025-06-15 17:30:00', DATEADD(minute, 175, '2025-06-15 17:30:00'), 9, 8, 'Active'),
('2025-06-15 18:00:00', DATEADD(minute, 142, '2025-06-15 18:00:00'), 10, 9, 'Active'),
('2025-06-15 19:00:00', DATEADD(minute, 181, '2025-06-15 19:00:00'), 1, 1, 'Active'),
('2025-06-15 19:30:00', DATEADD(minute, 152, '2025-06-15 19:30:00'), 2, 2, 'Active'),
('2025-06-15 20:00:00', DATEADD(minute, 148, '2025-06-15 20:00:00'), 3, 3, 'Active'),
('2025-06-15 20:30:00', DATEADD(minute, 132, '2025-06-15 20:30:00'), 4, 4, 'Active'),
('2025-06-15 21:00:00', DATEADD(minute, 148, '2025-06-15 21:00:00'), 5, 5, 'Active'),
('2025-06-15 21:30:00', DATEADD(minute, 142, '2025-06-15 21:30:00'), 7, 6, 'Active'),
('2025-06-15 22:00:00', DATEADD(minute, 154, '2025-06-15 22:00:00'), 8, 7, 'Active'),

-- Thứ Hai, 16/06/2025 (Phim: 11, 12, 14, 15, 1, 2, 3, 4, 5)
('2025-06-16 09:00:00', DATEADD(minute, 136, '2025-06-16 09:00:00'), 11, 1, 'Active'),
('2025-06-16 09:30:00', DATEADD(minute, 169, '2025-06-16 09:30:00'), 12, 2, 'Active'),
('2025-06-16 10:00:00', DATEADD(minute, 146, '2025-06-16 10:00:00'), 14, 3, 'Active'),
('2025-06-16 10:30:00', DATEADD(minute, 201, '2025-06-16 10:30:00'), 15, 4, 'Active'),
('2025-06-16 11:00:00', DATEADD(minute, 181, '2025-06-16 11:00:00'), 1, 5, 'Active'),
('2025-06-16 11:30:00', DATEADD(minute, 152, '2025-06-16 11:30:00'), 2, 6, 'Active'),
('2025-06-16 12:00:00', DATEADD(minute, 148, '2025-06-16 12:00:00'), 3, 7, 'Active'),
('2025-06-16 12:30:00', DATEADD(minute, 132, '2025-06-16 12:30:00'), 4, 8, 'Active'),
('2025-06-16 13:00:00', DATEADD(minute, 148, '2025-06-16 13:00:00'), 5, 9, 'Active'),
('2025-06-16 14:00:00', DATEADD(minute, 136, '2025-06-16 14:00:00'), 11, 1, 'Active'),
('2025-06-16 14:30:00', DATEADD(minute, 169, '2025-06-16 14:30:00'), 12, 2, 'Active'),
('2025-06-16 15:00:00', DATEADD(minute, 146, '2025-06-16 15:00:00'), 14, 3, 'Active'),
('2025-06-16 15:30:00', DATEADD(minute, 201, '2025-06-16 15:30:00'), 15, 4, 'Active'),
('2025-06-16 16:00:00', DATEADD(minute, 181, '2025-06-16 16:00:00'), 1, 5, 'Active'),
('2025-06-16 16:30:00', DATEADD(minute, 152, '2025-06-16 16:30:00'), 2, 6, 'Active'),
('2025-06-16 17:00:00', DATEADD(minute, 148, '2025-06-16 17:00:00'), 3, 7, 'Active'),
('2025-06-16 17:30:00', DATEADD(minute, 132, '2025-06-16 17:30:00'), 4, 8, 'Active'),
('2025-06-16 18:00:00', DATEADD(minute, 148, '2025-06-16 18:00:00'), 5, 9, 'Active'),
('2025-06-16 19:00:00', DATEADD(minute, 136, '2025-06-16 19:00:00'), 11, 1, 'Active'),
('2025-06-16 19:30:00', DATEADD(minute, 169, '2025-06-16 19:30:00'), 12, 2, 'Active'),
('2025-06-16 20:00:00', DATEADD(minute, 146, '2025-06-16 20:00:00'), 14, 3, 'Active'),
('2025-06-16 20:30:00', DATEADD(minute, 201, '2025-06-16 20:30:00'), 15, 4, 'Active'),
('2025-06-16 21:00:00', DATEADD(minute, 181, '2025-06-16 21:00:00'), 1, 5, 'Active'),
('2025-06-16 21:30:00', DATEADD(minute, 152, '2025-06-16 21:30:00'), 2, 6, 'Active'),
('2025-06-16 22:00:00', DATEADD(minute, 148, '2025-06-16 22:00:00'), 3, 7, 'Active'),

-- Thứ Ba, 17/06/2025 (Phim: 7, 8, 9, 10, 11, 12, 14, 15, 1)
('2025-06-17 09:00:00', DATEADD(minute, 142, '2025-06-17 09:00:00'), 7, 1, 'Active'),
('2025-06-17 09:30:00', DATEADD(minute, 154, '2025-06-17 09:30:00'), 8, 2, 'Active'),
('2025-06-17 10:00:00', DATEADD(minute, 175, '2025-06-17 10:00:00'), 9, 3, 'Active'),
('2025-06-17 10:30:00', DATEADD(minute, 142, '2025-06-17 10:30:00'), 10, 4, 'Active'),
('2025-06-17 11:00:00', DATEADD(minute, 136, '2025-06-17 11:00:00'), 11, 5, 'Active'),
('2025-06-17 11:30:00', DATEADD(minute, 169, '2025-06-17 11:30:00'), 12, 6, 'Active'),
('2025-06-17 12:00:00', DATEADD(minute, 146, '2025-06-17 12:00:00'), 14, 7, 'Active'),
('2025-06-17 12:30:00', DATEADD(minute, 201, '2025-06-17 12:30:00'), 15, 8, 'Active'),
('2025-06-17 13:00:00', DATEADD(minute, 181, '2025-06-17 13:00:00'), 1, 9, 'Active'),
('2025-06-17 14:00:00', DATEADD(minute, 142, '2025-06-17 14:00:00'), 7, 1, 'Active'),
('2025-06-17 14:30:00', DATEADD(minute, 154, '2025-06-17 14:30:00'), 8, 2, 'Active'),
('2025-06-17 15:00:00', DATEADD(minute, 175, '2025-06-17 15:00:00'), 9, 3, 'Active'),
('2025-06-17 15:30:00', DATEADD(minute, 142, '2025-06-17 15:30:00'), 10, 4, 'Active'),
('2025-06-17 16:00:00', DATEADD(minute, 136, '2025-06-17 16:00:00'), 11, 5, 'Active'),
('2025-06-17 16:30:00', DATEADD(minute, 169, '2025-06-17 16:30:00'), 12, 6, 'Active'),
('2025-06-17 17:00:00', DATEADD(minute, 146, '2025-06-17 17:00:00'), 14, 7, 'Active'),
('2025-06-17 17:30:00', DATEADD(minute, 201, '2025-06-17 17:30:00'), 15, 8, 'Active'),
('2025-06-17 18:00:00', DATEADD(minute, 181, '2025-06-17 18:00:00'), 1, 9, 'Active'),
('2025-06-17 19:00:00', DATEADD(minute, 142, '2025-06-17 19:00:00'), 7, 1, 'Active'),
('2025-06-17 19:30:00', DATEADD(minute, 154, '2025-06-17 19:30:00'), 8, 2, 'Active'),
('2025-06-17 20:00:00', DATEADD(minute, 175, '2025-06-17 20:00:00'), 9, 3, 'Active'),
('2025-06-17 20:30:00', DATEADD(minute, 142, '2025-06-17 20:30:00'), 10, 4, 'Active'),
('2025-06-17 21:00:00', DATEADD(minute, 136, '2025-06-17 21:00:00'), 11, 5, 'Active'),
('2025-06-17 21:30:00', DATEADD(minute, 169, '2025-06-17 21:30:00'), 12, 6, 'Active'),
('2025-06-17 22:00:00', DATEADD(minute, 146, '2025-06-17 22:00:00'), 14, 7, 'Active'),

-- Thứ Tư, 18/06/2025 (Phim: 2, 3, 4, 5, 7, 8, 9, 10, 11)
('2025-06-18 09:00:00', DATEADD(minute, 152, '2025-06-18 09:00:00'), 2, 1, 'Active'),
('2025-06-18 09:30:00', DATEADD(minute, 148, '2025-06-18 09:30:00'), 3, 2, 'Active'),
('2025-06-18 10:00:00', DATEADD(minute, 132, '2025-06-18 10:00:00'), 4, 3, 'Active'),
('2025-06-18 10:30:00', DATEADD(minute, 148, '2025-06-18 10:30:00'), 5, 4, 'Active'),
('2025-06-18 11:00:00', DATEADD(minute, 142, '2025-06-18 11:00:00'), 7, 5, 'Active'),
('2025-06-18 11:30:00', DATEADD(minute, 154, '2025-06-18 11:30:00'), 8, 6, 'Active'),
('2025-06-18 12:00:00', DATEADD(minute, 175, '2025-06-18 12:00:00'), 9, 7, 'Active'),
('2025-06-18 12:30:00', DATEADD(minute, 142, '2025-06-18 12:30:00'), 10, 8, 'Active'),
('2025-06-18 13:00:00', DATEADD(minute, 136, '2025-06-18 13:00:00'), 11, 9, 'Active'),
('2025-06-18 14:00:00', DATEADD(minute, 152, '2025-06-18 14:00:00'), 2, 1, 'Active'),
('2025-06-18 14:30:00', DATEADD(minute, 148, '2025-06-18 14:30:00'), 3, 2, 'Active'),
('2025-06-18 15:00:00', DATEADD(minute, 132, '2025-06-18 15:00:00'), 4, 3, 'Active'),
('2025-06-18 15:30:00', DATEADD(minute, 148, '2025-06-18 15:30:00'), 5, 4, 'Active'),
('2025-06-18 16:00:00', DATEADD(minute, 142, '2025-06-18 16:00:00'), 7, 5, 'Active'),
('2025-06-18 16:30:00', DATEADD(minute, 154, '2025-06-18 16:30:00'), 8, 6, 'Active'),
('2025-06-18 17:00:00', DATEADD(minute, 175, '2025-06-18 17:00:00'), 9, 7, 'Active'),
('2025-06-18 17:30:00', DATEADD(minute, 142, '2025-06-18 17:30:00'), 10, 8, 'Active'),
('2025-06-18 18:00:00', DATEADD(minute, 136, '2025-06-18 18:00:00'), 11, 9, 'Active'),
('2025-06-18 19:00:00', DATEADD(minute, 152, '2025-06-18 19:00:00'), 2, 1, 'Active'),
('2025-06-18 19:30:00', DATEADD(minute, 148, '2025-06-18 19:30:00'), 3, 2, 'Active'),
('2025-06-18 20:00:00', DATEADD(minute, 132, '2025-06-18 20:00:00'), 4, 3, 'Active'),
('2025-06-18 20:30:00', DATEADD(minute, 148, '2025-06-18 20:30:00'), 5, 4, 'Active'),
('2025-06-18 21:00:00', DATEADD(minute, 142, '2025-06-18 21:00:00'), 7, 5, 'Active'),
('2025-06-18 21:30:00', DATEADD(minute, 154, '2025-06-18 21:30:00'), 8, 6, 'Active'),
('2025-06-18 22:00:00', DATEADD(minute, 175, '2025-06-18 22:00:00'), 9, 7, 'Active'),

-- Thứ Năm, 19/06/2025 (Phim: 12, 14, 15, 1, 2, 3, 4, 5, 7)
('2025-06-19 09:00:00', DATEADD(minute, 169, '2025-06-19 09:00:00'), 12, 1, 'Active'),
('2025-06-19 09:30:00', DATEADD(minute, 146, '2025-06-19 09:30:00'), 14, 2, 'Active'),
('2025-06-19 10:00:00', DATEADD(minute, 201, '2025-06-19 10:00:00'), 15, 3, 'Active'),
('2025-06-19 10:30:00', DATEADD(minute, 181, '2025-06-19 10:30:00'), 1, 4, 'Active'),
('2025-06-19 11:00:00', DATEADD(minute, 152, '2025-06-19 11:00:00'), 2, 5, 'Active'),
('2025-06-19 11:30:00', DATEADD(minute, 148, '2025-06-19 11:30:00'), 3, 6, 'Active'),
('2025-06-19 12:00:00', DATEADD(minute, 132, '2025-06-19 12:00:00'), 4, 7, 'Active'),
('2025-06-19 12:30:00', DATEADD(minute, 148, '2025-06-19 12:30:00'), 5, 8, 'Active'),
('2025-06-19 13:00:00', DATEADD(minute, 142, '2025-06-19 13:00:00'), 7, 9, 'Active'),
('2025-06-19 14:00:00', DATEADD(minute, 169, '2025-06-19 14:00:00'), 12, 1, 'Active'),
('2025-06-19 14:30:00', DATEADD(minute, 146, '2025-06-19 14:30:00'), 14, 2, 'Active'),
('2025-06-19 15:00:00', DATEADD(minute, 201, '2025-06-19 15:00:00'), 15, 3, 'Active'),
('2025-06-19 15:30:00', DATEADD(minute, 181, '2025-06-19 15:30:00'), 1, 4, 'Active'),
('2025-06-19 16:00:00', DATEADD(minute, 152, '2025-06-19 16:00:00'), 2, 5, 'Active'),
('2025-06-19 16:30:00', DATEADD(minute, 148, '2025-06-19 16:30:00'), 3, 6, 'Active'),
('2025-06-19 17:00:00', DATEADD(minute, 132, '2025-06-19 17:00:00'), 4, 7, 'Active'),
('2025-06-19 17:30:00', DATEADD(minute, 148, '2025-06-19 17:30:00'), 5, 8, 'Active'),
('2025-06-19 18:00:00', DATEADD(minute, 142, '2025-06-19 18:00:00'), 7, 9, 'Active'),
('2025-06-19 19:00:00', DATEADD(minute, 169, '2025-06-19 19:00:00'), 12, 1, 'Active'),
('2025-06-19 19:30:00', DATEADD(minute, 146, '2025-06-19 19:30:00'), 14, 2, 'Active'),
('2025-06-19 20:00:00', DATEADD(minute, 201, '2025-06-19 20:00:00'), 15, 3, 'Active'),
('2025-06-19 20:30:00', DATEADD(minute, 181, '2025-06-19 20:30:00'), 1, 4, 'Active'),
('2025-06-19 21:00:00', DATEADD(minute, 152, '2025-06-19 21:00:00'), 2, 5, 'Active'),
('2025-06-19 21:30:00', DATEADD(minute, 148, '2025-06-19 21:30:00'), 3, 6, 'Active'),
('2025-06-19 22:00:00', DATEADD(minute, 132, '2025-06-19 22:00:00'), 4, 7, 'Active'),

-- Thứ Sáu, 20/06/2025 (Phim: 8, 9, 10, 11, 12, 14, 15, 1, 2)
('2025-06-20 09:00:00', DATEADD(minute, 154, '2025-06-20 09:00:00'), 8, 1, 'Active'),
('2025-06-20 09:30:00', DATEADD(minute, 175, '2025-06-20 09:30:00'), 9, 2, 'Active'),
('2025-06-20 10:00:00', DATEADD(minute, 142, '2025-06-20 10:00:00'), 10, 3, 'Active'),
('2025-06-20 10:30:00', DATEADD(minute, 136, '2025-06-20 10:30:00'), 11, 4, 'Active'),
('2025-06-20 11:00:00', DATEADD(minute, 169, '2025-06-20 11:00:00'), 12, 5, 'Active'),
('2025-06-20 11:30:00', DATEADD(minute, 146, '2025-06-20 11:30:00'), 14, 6, 'Active'),
('2025-06-20 12:00:00', DATEADD(minute, 201, '2025-06-20 12:00:00'), 15, 7, 'Active'),
('2025-06-20 12:30:00', DATEADD(minute, 181, '2025-06-20 12:30:00'), 1, 8, 'Active'),
('2025-06-20 13:00:00', DATEADD(minute, 152, '2025-06-20 13:00:00'), 2, 9, 'Active'),
('2025-06-20 14:00:00', DATEADD(minute, 154, '2025-06-20 14:00:00'), 8, 1, 'Active'),
('2025-06-20 14:30:00', DATEADD(minute, 175, '2025-06-20 14:30:00'), 9, 2, 'Active'),
('2025-06-20 15:00:00', DATEADD(minute, 142, '2025-06-20 15:00:00'), 10, 3, 'Active'),
('2025-06-20 15:30:00', DATEADD(minute, 136, '2025-06-20 15:30:00'), 11, 4, 'Active'),
('2025-06-20 16:00:00', DATEADD(minute, 169, '2025-06-20 16:00:00'), 12, 5, 'Active'),
('2025-06-20 16:30:00', DATEADD(minute, 146, '2025-06-20 16:30:00'), 14, 6, 'Active'),
('2025-06-20 17:00:00', DATEADD(minute, 201, '2025-06-20 17:00:00'), 15, 7, 'Active'),
('2025-06-20 17:30:00', DATEADD(minute, 181, '2025-06-20 17:30:00'), 1, 8, 'Active'),
('2025-06-20 18:00:00', DATEADD(minute, 152, '2025-06-20 18:00:00'), 2, 9, 'Active'),
('2025-06-20 19:00:00', DATEADD(minute, 154, '2025-06-20 19:00:00'), 8, 1, 'Active'),
('2025-06-20 19:30:00', DATEADD(minute, 175, '2025-06-20 19:30:00'), 9, 2, 'Active'),
('2025-06-20 20:00:00', DATEADD(minute, 142, '2025-06-20 20:00:00'), 10, 3, 'Active'),
('2025-06-20 20:30:00', DATEADD(minute, 136, '2025-06-20 20:30:00'), 11, 4, 'Active'),
('2025-06-20 21:00:00', DATEADD(minute, 169, '2025-06-20 21:00:00'), 12, 5, 'Active'),
('2025-06-20 21:30:00', DATEADD(minute, 146, '2025-06-20 21:30:00'), 14, 6, 'Active'),
('2025-06-20 22:00:00', DATEADD(minute, 201, '2025-06-20 22:00:00'), 15, 7, 'Active'),

-- Thứ Bảy, 21/06/2025 (Phim: 3, 4, 5, 7, 8, 9, 10, 11, 12)
('2025-06-21 09:00:00', DATEADD(minute, 148, '2025-06-21 09:00:00'), 3, 1, 'Active'),
('2025-06-21 09:30:00', DATEADD(minute, 132, '2025-06-21 09:30:00'), 4, 2, 'Active'),
('2025-06-21 10:00:00', DATEADD(minute, 148, '2025-06-21 10:00:00'), 5, 3, 'Active'),
('2025-06-21 10:30:00', DATEADD(minute, 142, '2025-06-21 10:30:00'), 7, 4, 'Active'),
('2025-06-21 11:00:00', DATEADD(minute, 154, '2025-06-21 11:00:00'), 8, 5, 'Active');

-- Invoice table data
INSERT INTO Invoice (CustomerID, EmployeeID, PromotionID, Discount, BookingDate, Totalprice) VALUES
(1, 2, 1, 1.25, '2025-06-07 10:30:00', 11.25),
(2, 3, 2, 3.00, '2025-06-07 11:15:00', 12.00),
(3, NULL, NULL, 0.00, '2025-06-07 14:20:00', 13.75),
(5, 4, 3, 2.77, '2025-06-07 16:45:00', 15.73),
(1, 2, 4, 4.62, '2025-06-07 19:10:00', 13.88),
(6, NULL, NULL, 0.00, '2025-06-06 20:30:00', 32.00),
(7, 5, 5, 8.55, '2025-06-06 21:15:00', 19.95),
(8, 3, NULL, 0.00, '2025-06-06 15:45:00', 25.00),
(3, NULL, 6, 0.62, '2025-06-05 18:20:00', 11.88),
(8, 7, 7, 5.13, '2025-06-05 12:30:00', 23.37);

-- Chèn dữ liệu vào bảng Detail_Seat
INSERT INTO Detail_Seat (InvoiceID, SeatID, ScheduleID) VALUES

-- Hóa đơn 1: Xem phim ScheduleID=1 (Room 1), ghế F1, F2 (VIP) - 2 ghế
(1, 41, 1),  -- F1 VIP Room 1
(1, 42, 1),  -- F2 VIP Room 1

-- Hóa đơn 2: Xem phim ScheduleID=2 (Room 1), ghế A1, A2 - 2 ghế thường
(2, 1, 2),   -- A1 Room 1
(2, 2, 2),   -- A2 Room 1

-- Hóa đơn 3: Xem phim ScheduleID=4 (Room 2), ghế F1, F2, F3 (VIP) - 3 ghế
(3, 109, 4), -- F1 VIP Room 2
(3, 110, 4), -- F2 VIP Room 2
(3, 111, 4), -- F3 VIP Room 2

-- Hóa đơn 4: Xem phim ScheduleID=3 (Room 1), ghế E1, E2 - 2 ghế thường
(4, 33, 3),  -- E1 Room 1
(4, 34, 3),  -- E2 Room 1

-- Hóa đơn 5: Xem phim ScheduleID=6 (Room 3), ghế E1, E2 (VIP) + C1, C2 - 2 VIP + 2 thường
(5, 133, 6), -- E1 VIP Room 3
(5, 134, 6), -- E2 VIP Room 3
(5, 97, 6),  -- C1 Room 3
(5, 98, 6),  -- C2 Room 3

-- Hóa đơn 6: Xem phim ScheduleID=7 (Room 4 - Couple), ghế A1, A2 - 2 ghế couple
(6, 169, 7), -- A1 Couple Room 4
(6, 170, 7), -- A2 Couple Room 4

-- Hóa đơn 7: Xem phim ScheduleID=8 (Room 4 - Couple), ghế F1 (VIP Couple) - 1 ghế
(7, 199, 8), -- F1 VIP Couple Room 4

-- Hóa đơn 8: Xem phim ScheduleID=5 (Room 2), ghế B1 - 1 ghế thường
(8, 59, 5),  -- B1 Room 2

-- Hóa đơn 9: Xem phim ScheduleID=9 (Room 5), ghế G1, G2, H1, H2 (VIP) - 4 ghế VIP
(9, 217, 9), -- G1 VIP Room 5
(9, 218, 9), -- G2 VIP Room 5
(9, 225, 9), -- H1 VIP Room 5
(9, 226, 9), -- H2 VIP Room 5

-- Hóa đơn 10: Xem phim ScheduleID=1 (Room 1), ghế D1, D2 - 2 ghế thường
(10, 25, 1), -- D1 Room 1
(10, 26, 1); -- D2 Room 1

-- Theater_Stock table data
INSERT INTO Theater_Stock (TheaterID, FoodName, Quantity, UnitPrice, Image, Status) VALUES
(1, 'Large Popcorn', 50, 7.50, 'popcorn_large.jpg', 'Active'),
(1, 'Coca Cola', 100, 4.25, 'coca_cola.jpg', 'Active'),
(1, 'Gummy Bears', 30, 2.75, 'gummy_bears.jpg', 'Active'),
(1, 'Medium Popcorn', 80, 5.50, 'popcorn_medium.jpg', 'Active'),
(1, 'Pepsi', 120, 3.95, 'pepsi.jpg', 'Active'),
(2, 'Nachos with Cheese', 25, 8.75, 'nachos_cheese.jpg', 'Active'),
(2, 'Orange Juice', 60, 4.85, 'orange_juice.jpg', 'Active'),
(2, 'Hot Dog', 40, 6.50, 'hot_dog.jpg', 'Active'),
(2, 'Ice Cream Bar', 70, 3.25, 'ice_cream.jpg', 'Inactive'),
(3, 'Potato Chips', 90, 2.95, 'potato_chips.jpg', 'Active'),
(3, 'Coca Cola', 100, 4.25, 'coca_cola.jpg', 'Active'),
(3, 'Potato Chips', 90, 2.95, 'potato_chips.jpg', 'Active'),
(3, 'Ice Cream Bar', 70, 3.25, 'ice_cream.jpg', 'Inactive');

-- Detail_FD table data
INSERT INTO Detail_FD (InvoiceID, Theater_StockID, Quantity, TotalPrice) VALUES
(1, 1, 1, 7.50),
(1, 2, 2, 8.50),
(2, 4, 1, 5.50),
(3, 3, 2, 5.50),
(4, 6, 1, 8.75),
(5, 7, 1, 4.85),
(6, 8, 2, 13.00),
(7, 2, 3, 12.75),
(8, 10, 1, 2.95),
(9, 1, 1, 7.50);

-- MovieFeedback table data
INSERT INTO MovieFeedback (CustomerID, MovieID, Content, MovieRate) VALUES
(1, 1, 'Amazing movie with great emotional depth!', 5),
(2, 1, 'Great effects but a bit too long', 4),
(3, 2, 'Spider-Man was outstanding!', 5),
(5, 3, 'Dark and gritty Batman version', 4),
(1, 4, 'Tom Cruise still delivers amazing action', 5),
(6, 5, 'Visually stunning underwater scenes', 4),
(7, 6, 'Touching tribute to Chadwick Boseman', 4),
(8, 7, 'Minions are always adorable', 3),
(4, 8, 'Multiverse concept was confusing', 3),
(4, 9, 'Dinosaurs still impressive after all these years', 4);

-- Insert data into MovieFeedback table
INSERT INTO MovieFeedback (CustomerID, MovieID, Content, MovieRate) VALUES
-- Avengers: Endgame (MovieID: 1)
(1, 1, 'Absolutely phenomenal conclusion to the Marvel saga!', 5),
(3, 1, 'Epic battle sequences, emotional ending', 5),
(5, 1, 'Great movie but too long for my taste', 4),
(8, 1, 'Perfect blend of action and emotion', 5),

-- The Dark Knight (MovieID: 2)
(2, 2, 'Heath Ledger performance was incredible!', 5),
(4, 2, 'Dark and intense, masterpiece of cinema', 5),
(7, 2, 'Best Batman movie ever made', 5),
(1, 2, 'Joker was terrifyingly good', 4),

-- Inception (MovieID: 3)
(1, 3, 'Mind-bending plot, loved every minute', 5),
(6, 3, 'Complex but brilliant storytelling', 4),
(2, 3, 'Had to watch twice to understand fully', 4),

-- Parasite (MovieID: 4)
(2, 4, 'Brilliant social commentary, deserved the Oscar', 5),
(5, 4, 'Intense thriller with deep meaning', 4),
(8, 4, 'Unexpected twists throughout', 4),

-- Spider-Man: No Way Home (MovieID: 5)
(3, 5, 'Amazing to see all three Spider-Men together!', 5),
(6, 5, 'Great fan service and emotional moments', 4),
(7, 5, 'Action-packed and nostalgic', 4),
(1, 5, 'Best Spider-Man movie in years', 5),

-- Titanic (MovieID: 6) - Status: Removed
(4, 6, 'Classic love story, never gets old', 4),
(8, 6, 'Heartbreaking and beautiful', 5),

-- The Shawshank Redemption (MovieID: 7)
(1, 7, 'Most inspiring movie I have ever seen', 5),
(2, 7, 'Perfect story about hope and friendship', 5),
(5, 7, 'Morgan Freeman narration is perfect', 5),
(8, 7, 'Slow start but incredible ending', 4),

-- Pulp Fiction (MovieID: 8)
(3, 8, 'Unique storytelling style by Tarantino', 4),
(6, 8, 'Dialogues are memorable and quotable', 4),
(7, 8, 'Not for everyone but I loved it', 4),

-- The Godfather (MovieID: 9)
(4, 9, 'Classic mafia film, still holds up today', 5),
(3, 9, 'Marlon Brando was phenomenal', 5),
(5, 9, 'Slow paced but worth every minute', 4),

-- Forrest Gump (MovieID: 10)
(1, 10, 'Tom Hanks delivers another masterpiece', 5),
(2, 10, 'Emotional journey through American history', 4),
(5, 10, 'Heartwarming and funny at the same time', 4),

-- The Matrix (MovieID: 11)
(3, 11, 'Revolutionary special effects for its time', 4),
(6, 11, 'Philosophical themes mixed with great action', 4),
(8, 11, 'Keanu Reeves at his best', 4),

-- Interstellar (MovieID: 12)
(4, 12, 'Visually stunning space epic', 5),
(7, 12, 'Matthew McConaughey emotional performance', 4),
(1, 12, 'Complex science but beautifully told', 4),

-- Fight Club (MovieID: 13) - Status: Removed
(1, 13, 'Dark psychological thriller, very thought-provoking', 4),
(6, 13, 'Brad Pitt and Edward Norton great chemistry', 4),

-- Goodfellas (MovieID: 14)
(2, 14, 'Authentic portrayal of mafia life', 4),
(5, 14, 'Fast-paced and engaging throughout', 4),
(7, 14, 'Joe Pesci was terrifying and funny', 4),

-- The Lord of the Rings: The Return of the King (MovieID: 15)
(3, 15, 'Epic conclusion to the trilogy', 5),
(6, 15, 'Long but every minute was worth it', 5),
(7, 15, 'Perfect fantasy adventure', 4);

-- Schindler List (MovieID: 16)
--(4, 16, 'Powerful and important historical film', 5),
--(8, 16, 'Difficult to watch but necessary', 5),
--(2, 16, 'Liam Neeson outstanding performance', 4),

-- 12 Angry Men (MovieID: 17)
--(1, 17, 'Brilliant courtroom drama with great acting', 4),
--(2, 17, 'Tense and engaging despite simple setting', 4),
--(5, 17, 'Classic film that still feels relevant', 4);


-- ServiceFeedback table data
INSERT INTO ServiceFeedback (CustomerID, Content, Status) VALUES
(1, 'Excellent customer service from staff', 'Suported'),
(2, 'Sound system in theater was too quiet', 'Suported'),
(3, 'Popcorn was too salty', 'Not_Suported'),
(5, 'Very comfortable seating', 'Suported'),
(1, 'Restrooms need better maintenance', 'Suported'),
(6, 'Ticket prices are getting expensive', 'Not_Suported'),
(7, 'Long waiting time at concession stand', 'Suported'),
(8, 'Need more discount combo deals', 'Not_Suported'),
(1, 'Website booking system is user-friendly', 'Suported'),
(3, 'Would like more late night showtimes', 'Suported');

-- History table data
INSERT INTO History (TableName, RecordID, OldValue, NewValue, Action, AccountID, UpdatedAt) VALUES
('Movie', 10, 'Status: Active', 'Status: Removed', 'Update', 1, '2025-06-07 10:00:00'),
('Promotion', 9, 'Status: Available', 'Status: Expired', 'Update', 1, '2025-06-06 23:59:59'),
('Theater', 9, 'Status: Active', 'Status: Inactive', 'Update', 1, '2025-06-05 15:30:00'),
('Room', 9, 'Status: Active', 'Status: Inactive', 'Update', 2, '2025-06-04 14:20:00'),
('Seat', 9, 'Status: Active', 'Status: Inactive', 'Update', 2, '2025-06-04 14:25:00'),
('Schedule', 10, 'Status: Active', 'Status: Inactive', 'Update', 2, '2025-06-03 16:45:00'),
('Employee', 9, 'Status: Active', 'Status: Inactive', 'Update', 1, '2025-06-02 11:10:00'),
('Theater_Stock', 9, 'Status: Active', 'Status: Inactive', 'Update', 2, '2025-06-01 09:30:00'),
('Account', 8, 'Status: Active', 'Status: Banned', 'Update', 1, '2025-05-30 13:45:00'),
('Customer', 4, 'Point: 50', 'Point: 0', 'Update', 4, '2025-05-29 17:20:00');

