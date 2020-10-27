SHOW DATABASES;
USE example;
SHOW TABLES;
SHOW CREATE TABLE USers;
SHOW TRIGGERS;

/*유저의 정보*/
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
 );
 
INSERT INTO Users(userName, userNickName, userEmail, userID, userPwd, userLocation, userLoginCheck)VALUES('test', 'test', 'test@test.com', 'test123', 'test123', 'test', 0);
 
/*공지사항 정보*/
CREATE TABLE `Notifications` (
   `notificationNo` int NOT NULL AUTO_INCREMENT,
   `notificationTitle` varchar(30) NOT NULL,
   `notificationContents` varchar(500) NOT NULL,
   PRIMARY KEY (`notificationNo`)
 );
 
/*이벤트 정보*/
CREATE TABLE `Events` (
   `eventNo` int NOT NULL AUTO_INCREMENT,
   `eventTitle` varchar(30) NOT NULL,
   `eventContents` varchar(500) NOT NULL,
   PRIMARY KEY (`eventNo`)
 );
 
/*버전의 정보*/
CREATE TABLE `Versions` (
   `version` int NOT NULL,
   `versionInformation` varchar(100) NOT NULL,
   PRIMARY KEY (`version`)
 );

/*측정한 센서값의 정보*/
CREATE TABLE `RaspiData` (
   `userNo` int NOT NULL,
   `recvDate` date NOT NULL,
   `recentHumi` int NOT NULL,
   `Temp` int NOT NULL,
   FOREIGN KEY (`userNo`) REFERENCES `Users` (`userNo`)
 );

/*사용자가 설정한 설정센서값의 정보*/
CREATE TABLE `saveRaspiData` (
   `userNo` int NOT NULL,
   `recvDate` date NOT NULL,
   `Humi` varchar(30) NOT NULL,
   FOREIGN KEY (`userNo`) REFERENCES `Users` (`userNo`)
 );

/*현재 아두이노의 센서들의 상태 및 감지기의 상태 정보*/
CREATE TABLE `arduinoStatus` (
   `userNo` int NOT NULL,
   `automode` int NOT NULL,
   `pump` int NOT NULL,
   `fan` int NOT NULL,
   `led` int NOT NULL,
   `fireDetection` int NOT NULL,
   `objectDetection` int NOT NULL,
   FOREIGN KEY (`userNo`) REFERENCES `Users` (`userNo`)
 );

/*사용자가 회원가입 시 arduinoStatus와 saveRaspiData와 RaspiData에 자동으로 튜플을 만들어주는 트리거*/
DELIMITER $$
	CREATE TRIGGER autoAddUserNo
    AFTER INSERT ON Users
    FOR EACH ROW
    BEGIN
		INSERT INTO saveRaspiData SET userNo = NEW.userNo, recvDate = now(), Humi = 0;
        INSERT INTO arduinoStatus SET userNo = NEW.userNo, automode = 0, pump = 0, fan = 0, led = 0, fireDetection = 0, objectDetection = 0;
        INSERT INTO RaspiData에 SET userNo = NEW.userNo, recvDate = now(), recentHumi = 0, Temp = 0;
	END
$$ DELIMITER ;

commit;

SELECT * FROM Users;

SELECT * FROM RaspiData;
SELECT * FROM arduinoStatus;
SELECT * FROM saveRaspiData;

INSERT INTO RaspiData(userNo, recvDate, recentHumi, Temp)VALUES(23, now(), 0, 0);

UPDATE Users SET userLoginCheck = 0 WHERE userNo = 1;

UPDATE arduinoStatus SET automode = 0, pump = 0, fan = 0, led = 0, fireDetection = 0, objectDetection = 0 WHERE userNo = 1;