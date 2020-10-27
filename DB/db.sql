show databases;
use example;
show tables;
select * from Versions;
SELECT version, versionInformation FROM Versions;
SELECT * from Users;
SELECT * from Notifications;
show full columns from Users;
show create table Users;

CREATE TABLE `Users` (
   `userNo` int NOT NULL AUTO_INCREMENT,
   `userName` varchar(30) NOT NULL,
   `userNickName` varchar(30) NOT NULL,
   `userEmail` varchar(30) NOT NULL,
   `userID` varchar(30) NOT NULL,
   `userPwd` varchar(30) NOT NULL,
   `userLocation` varchar(30) NOT NULL,
   `userLoginCheck` int NOT NULL,
   PRIMARY KEY (`userNo`)
 )ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

UPDATE Users SET 
userName = '운영자',
userNickName = '관리자',
userEmail = 'cgs07@naver.com',
userID = 'test123',
userPwd = 'test123',
userLocation = '천안작은도시',
userLoginCheck = 0
WHERE userNo = 1;

UPDATE Users SET 
userName = '운영자2',
userNickName = '관리자2',
userEmail = 'cgs07@naver.com2',
userID = 'test1234',
userPwd = 'test1234',
userLocation = '천안미세먼지도시',
userLoginCheck = 0
WHERE userNo = 2;

SELECT * FROM RaspiData;
SELECT * FROM saveRaspiData;
SELECT * FROM arduinoStatus;

ALTER TABLE RaspiData CHANGE Humi recentHumi int;
ALTER TABLE Users MODIFY userLoginCheck int NOT NULL;
ALTER TABLE Users auto_increment = 1;

SELECT * FROM Notifications;
SELECT * FROM Events;
SELECT notificationTitle, notificationContents FROM Notifications;
SELECT eventTitle, eventContents FROM Events;

commit;

DELETE FROM Users  userLoginCheck;

INSERT INTO Users(userName, userNickName, userEmail, userID, userPwd, userLocation)VALUES('test', 'test', 'test', 'test', 'test', 'test');

ALTER TABLE Users AUTO_INCREMENT=1;
SET @COUNT = 0;
UPDATE Users SET userNo = @COUNT:=@COUNT+1;

SELECT userLoginCheck FROM Users WHERE userNo = '1';

UPDATE Users SET userLoginCheck = "0" WHERE userNo = "1" ;
SELECT * from Users;
commit;