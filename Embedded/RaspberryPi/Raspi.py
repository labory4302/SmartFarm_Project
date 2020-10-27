
from bluetooth import *
from socket import *
import threading
import time
import pymysql
import datetime

#RDS에 올릴 RasberryPi ID
RASPIID = '1'

#아두이노 블루투스와 connect
bluetoothArduino = BluetoothSocket(RFCOMM)
bluetoothArduino.connect(("98:D3:37:90:AF:07",1))
#bluetoothArduino.connect(("98:D3:91:FD:C5:5A",1))
#bluetoothArduino.baud = 38400
print('블루투스 접속이 확인되었습니다.')
#RDS와 connect하기 위해 필요한 코드, host(IPv4주소), RDS Username, password , db이름 설정
conn = pymysql.connect(host = "dbinstance3.cjytw5i33eqd.us-west-2.rds.amazonaws.com", user = "luck0707", passwd = "disorder2848", db = "example")


#센서값이 RDS에 올라간 시간 표시
now = datetime.datetime.now()

#안드로이드에서 전달받은 값을 아두이노로 send
def sendArduino(data):
    try:
        bluetoothArduino.send(data)
        print("send to arduino {}\n".format(data))
    except KeyboardInterrupt:
        print("\nFinished\n")
        exit()

#아두에노에서 받은 센서값을 RDS로 send
def sendRDS(humi,temp):
    try:
#with를 사용해서 connect 종료시 자동 close
        with conn.cursor() as cur:
            sql = "INSERT INTO RaspiData VALUES(%s, %s, %s, %s)"
            cur.execute(sql,(RASPIID, now, humi, temp))
            conn.commit()
    except KeyboardInterrupt:
        conn.close()
        exit()

#아두이노의 현재상태(on,off 여부) 를 RDS에 올리는 함수
def arduinoStatus(automode,pump,fan,led):
    try:
        with conn.cursor() as cur:
            sql = "UPDATE arduinoStatus SET automode  = %s , pump = %s, fan = %s, led = %s WHERE userNo = %s;"
            #sql = "insert into arduinoStatus values(%s, %s, %s, %s, %s)"
            cur.execute(sql,(automode, pump, fan, led, RASPIID))
            conn.commit()
    except KeyboardInterrupt:
        conn.close()
        exit()

#안드로이드 세팅값을 RDS에 저장하는 함수
def saveData(saveHumi):
    try:
        with conn.cursor() as cur:
            sql = "UPDATE saveRaspiData SET recvDate = %s, Humi = %s WHERE userNo = %s;"
          #  sql = "INSERT INTO saveRaspiData VALUES(%s, %s, %s)"
            cur.execute(sql,(now, saveHumi, RASPIID))
            conn.commit()
    except KeyboardInterrupt:
        conn.close()
        exit()


#아두이노에서 센서값을 받아 파싱 후 sendRDS함수를 이용해 데이터 전송 (쓰레드형식)
def receiverArduinoData(sock):
    try:
        while True:
#아두이노에서 값을 수신한뒤 UTP-8로 디코딩 , 디코딩 하지 않을 시 센서값이 제대로 수신되지 않음
            recvArduinoData = sock.recv(4096)
            arduinoData = recvArduinoData.decode('utf-8').strip()
#수신한 데이터가 없을 경우 pass  
            if(arduinoData == ""):
                pass
            else:
                print("recived Data : {}\n".format(arduinoData))           
# , 를 기준으로 데이터 파싱
                passingArduinoData = arduinoData.split(",")
#파싱된 데이터 RDS에 전송
                if(passingArduinoData[0] == "1"):
                    
                    arduinoStatus(passingArduinoData[1],passingArduinoData[2],passingArduinoData[3],passingArduinoData[4])
               #     print("{} , {}".format(passingArduinoData[1], passingArduinoData[2]))
                else:
                    sendRDS(passingArduinoData[0],passingArduinoData[1])
               #     print("{} , {}".format(passingArduinoData[0],passingArduinoData[1]))

    except KeyboardInterrupt:
        print("\nFinished\n")
        exit()
    finally:
        bluetoothArduino.close()
        

#안드로이드와의 소켓통신을 위한 함수(쓰레드 형식)
def receiverAndroidRequest(sock):
    try:
        while True:
            recvAndroidData = sock.recv(1024)
            decodingData = recvAndroidData.decode('utf-8')#.strip()
            print('받은 데이터 : ', decodingData)
            sendArduino(decodingData)    
            passingAndroidData = decodingData.split(",")
            if(int(int(passingAndroidData[0])/1000) ==5):
                saveData(int(int(passingAndroidData[0])%1000))
            else:
                pass
            sock, addr = androidSock.accept()
    except KeyboardInterrupt:
        print("\nFinished\n")
        exit()
    finally:
        androidSock.close()
        connectionSock.close()


#receiverArduinoData 함수를 쓰레드로 실행
arduinoThread = threading.Thread(target=receiverArduinoData, args=(bluetoothArduino,))
arduinoThread.start()

#안드로이드 소켓 연결, PORT가 9999인 모든 IP 접속 허용
androidSock = socket(AF_INET, SOCK_STREAM)
androidSock.bind(('', 9999))
androidSock.listen(1)
connectionSock, addr = androidSock.accept()

print(str(addr),'에서 접속이 확인되었습니다.')

#receiverAndroidRequest 함수를 쓰레드로 실행
androidThread = threading.Thread(target=receiverAndroidRequest, args=(connectionSock,))
androidThread.start()

while True:
    time.sleep(1)
    pass

