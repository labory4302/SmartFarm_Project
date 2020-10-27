var mysql = require('mysql');
var express = require('express');
var bodyParser = require('body-parser');
var app = express();

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));
app.listen(8080, function () {
    console.log('서버 실행 중...');
});

var connection = mysql.createConnection({
    host: "dbinstance3.cjytw5i33eqd.us-west-2.rds.amazonaws.com",
    user: "luck0707",
    database: "example",
    password: "disorder2848",
    port: 3306
});

// 안드로이드
// 회원
// 접속
app.post('/user/access/in', function (req, res) {

    var userLoginCheck = req.body.userLoginCheck;
    var userNo = req.body.userNo;

    var sql = 'UPDATE Users SET userLoginCheck = ? WHERE userNo = ?;';
    var params = [userLoginCheck, userNo];

    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
        }
        res.json({
            code : resultCode
        });
    });
});

// 접속 해제
app.post('/user/access/out', function (req, res) {

    var userLoginCheck = req.body.userLoginCheck;
    var userNo = req.body.userNo;
    
    var sql = 'UPDATE Users SET userLoginCheck = ? WHERE userNo = ?;';
    var params = [userLoginCheck, userNo];

    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
        }
        res.json({
            code : resultCode
        });
    });
});



// 회원가입
app.post('/user/register', function (req, res) {
    console.log(req.body);

    var userName = req.body.userName;
    var userNickName = req.body.userNickName;
    var userEmail = req.body.userEmail;
    var userID = req.body.userID;
    var userPwd = req.body.userPwd;
    var userLocation = req.body.userLocation;

    // 삽입을 수행하는 sql문.
    var sql = 'INSERT INTO Users(userName, userNickName, userEmail, userID, userPwd, userLocation) VALUES (?, ?, ?, ?, ?, ?);';
    var params = [userName, userNickName, userEmail, userID, userPwd, userLocation];

    // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';

        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = '회원가입에 성공했습니다.';
        }
        res.json({
            code : resultCode,
            message : message
        });
    });
});

//회원가입 시 중복된 아이디 확인
app.post('/user/checkDuplicateId', function (req, res) {
    var userID = req.body.userID;

    var sql = 'SELECT * FROM Users WHERE userID = ?;';

    connection.query(sql, userID, function(err, result) {
        var resultCode = 404;

        if (err) {
            console.log(err);
        } else {
            if (result.length === 0) {
                resultCode = 200;
            } else {
                resultCode = 204;
            }
        }

        res.json({
            code : resultCode
        });
    });
});

//로그인
app.post('/user/login', function (req, res) {
    var userID = req.body.userID;
    var userPwd = req.body.userPwd;

    //mypage에 필요한 데이터
    var userName = req.body.userName;
    var userNickName = req.body.userNickName;
    var userEmail = req.body.userEmail;
    var userLocation = req.body.userLocation;
    var userNo = req.body.userNo;
    var userLoginCheck = req.body.userLoginCheck;

    var sql = 'SELECT * FROM Users WHERE userID = ?;';

    connection.query(sql, userID, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';

        if (err) {
            console.log(err);
        } else {
            if (result.length === 0) {
                resultCode = 204;
                message = '아이디가 틀렸습니다.';
            } else if (userPwd !== result[0].userPwd) {
                resultCode = 204;
                message = '비밀번호가 틀렸습니다!';
            } else {
                resultCode = 200;
                message = result[0].userNickName + '님 환영합니다!';
            }
        }

        if (err) {
            console.log(err);
        } else{
            if (result.length === 0){
                resultcode = 204;
                message = '정보를 불러오지 못했습니다.';
            } else {
                resultcode = 200;
            }
        }
        res.json({
            code : resultCode,
            message : message,
            userName : result[0].userName,
            userNickName : result[0].userNickName,
            userEmail : result[0].userEmail,
            userID : result[0].userID,
            userPwd : result[0].userPwd,
            userLocation : result[0].userLocation,
            userNo : result[0].userNo,
            userLoginCheck : result[0].userLoginCheck
        });
    })
});

// 안드로이드
// 마이페이지
// 개인정보수정
app.post('/mypage/changemyinformation', function (req, res) {

    var userName = req.body.userName;
    var userNickName = req.body.userNickName;
    var userEmail = req.body.userEmail;
    var userID = req.body.userID;
    var userPwd = req.body.userPwd;
    var userLocation = req.body.userLocation;
    var userNo = req.body.userNo;

    // 갱신을 수행하는 sql문.
    var sql = 'UPDATE Users SET userName = ?, userNickName = ?, userEmail = ?, userID = ?, userPwd = ?, userLocation = ? WHERE userNo = ?;';
    var params = [userName, userNickName, userEmail, userID, userPwd, userLocation, userNo];

    // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';
    
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = '개인정보가 수정되었습니다.';
        }
        res.json({
            code : resultCode,
            message : message
        });
    });
});

// 공지사항
app.post('/mypage/notification', function (req, res) {

    var notificationTitle = req.body.notificationTitle;
    var notificationContents = req.body.notificationContents;
    var params = [notificationTitle, notificationContents];

    var sql = 'SELECT notificationTitle, notificationContents FROM Notifications;';

    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            res.json({
                code : resultCode,
                notificationTitle : result[0].notificationTitle,
                notificationContents : result[0].notificationContents
            });
        }
    });
});

// 이벤트
app.post('/mypage/event', function (req, res) {

    var eventTitle = req.body.eventTitle;
    var eventContents = req.body.eventContents;
    var params = [eventTitle, eventContents];

    var sql = 'SELECT eventTitle, eventContents FROM Events;';

    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            res.json({
                code : resultCode,
                eventTitle : result[0].eventTitle,
                eventContents : result[0].eventContents
            });
        }
    });
});

// 버전 정보
app.post('/mypage/version', function (req, res) {

    var version = req.body.version;
    var versionInformation = req.body.versionInformation;
    var params = [version, versionInformation];

    var sql = 'SELECT version, versionInformation FROM Versions;';

    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            res.json({
                code : resultCode,
                version : result[0].version,
                versionInformation : result[0].versionInformation
            });
        }
    });
});

// 임베디드
// 블루투스로 전달 받은 센서값을 안드로이드에 표시
app.post('/embedded/data', function (req, res) {

    var userNo = req.body.userNo;

    var sql = 'SELECT recentHumi, Temp FROM RaspiData WHERE userNo = ?;';

    connection.query(sql, userNo, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            res.json({
                code : resultCode,
                recentHumi : result[0].recentHumi,
                Temp : result[0].Temp
            });
        }
    });
});

// 아두이노의 현재 세팅값을 저장, 표시
app.post('/embedded/recentdata', function (req, res) {

    var userNo = req.body.userNo;
    var Humi = req.body.Humi;

    var sql = 'SELECT userNo, Humi FROM saveRaspiData;';
    var params = [userNo, Humi];

    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            res.json({
                code : resultCode,
                userNo : result[0].userNo,
                Humi : result[0].Humi
            });
        }
    });
});

// 어플 종료해도 실행 했을 시 세팅값을 유지하기 위한 값
app.post('/embedded/status', function (req, res) {

    var userNo = req.body.userNo;

    var sql = 'SELECT userNo, automode, pump, fan, led, fireDetection, objectDetection FROM arduinoStatus WHERE userNo = ?;';

    connection.query(sql, userNo, function (err, result) {
        var resultCode = 404;
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            res.json({
                code : resultCode,
                userNo : result[0].userNo,
                automode : result[0].automode,
                pump : result[0].pump,
                fan : result[0].fan,
                led : result[0].led,
                fireDetection : result[0].fireDetection,
                objectDetection : result[0].objectDetection
            });
        }
    });
});

// 화재감지, 객체감지 상태 변경 시 값 반영
app.post('/embedded/changeDetection', function (req, res) {

    var userNo = req.body.userNo;
    var fireDetection = req.body.fireDetection;
    var objectDetection = req.body.objectDetection;

    var sql = 'UPDATE arduinoStatus SET fireDetection = ?, objectDetection = ? WHERE userNo = ?;';
    var params = [fireDetection, objectDetection, userNo];

    // sql 문의 ?는 두번째 매개변수로 넘겨진 params의 값으로 치환된다.
    connection.query(sql, params, function (err, result) {
        var resultCode = 404;
        var message = '에러가 발생했습니다';
    
        if (err) {
            console.log(err);
        } else {
            resultCode = 200;
            message = '감지기 설정변경이 완료되었습니다.';
        }

        res.json({
            code : resultCode,
            message : message
        });
    });
});


// //모든 기능의 원형
// app.post('/embedded/recentdata', function (req, res) {

//     var params = [];

//     var sql = 'SELECT userNo, Humi FROM saveRaspiData;';

//     connection.query(sql, params, function (err, result) {
//         var resultCode = 404;
//         if (err) {
//             console.log(err);
//         } else {
//             resultCode = 200;
//             res.json({
//                 code : resultCode,
                
//             });
//         }
//     });
// });