/*안드로이드 제어코드
1001:워터펌프 활성화  | 1000:워터펌프 비활성화
2001:환풍기 활성화    | 2000:환풍기 비활성화
3001:LED 활성화       | 3000:LED 비활성화
4***:자동모드 토양수분량 설정
5***:자동모드 습도 조절
9001:자동모드 ON      | 9000:자동모드 OFF
*/


//라이브러리 호출부
#include <SoftwareSerial.h>   //블루투스통신을 위한 라이브러리(Software로 시리얼통신을 하기 위함)
#include "DHT.h"              //온습도센서를 위한 라이브러리
#include <Adafruit_NeoPixel.h>//NeoPixel-8 LED를 위한 라이브러리


//PinMapping부
#define CONTROL_PUMP 2  //펌프상태변환(릴레이 In1)
#define CONTROL_FAN 3   //환풍기상태변환(릴레이 In2)
#define NEO_LED 4       //NeoPixel-8 LED
#define TEMP_HUM 5      //온습도센서
#define SW_LED 6        //8-LED제어 Switch
#define SW_FAN 7        //환풍기제어 Switch
#define SW_PUMP 8       //펌프제어 Switch
#define BLUETOOTH_TX 9  //블루투스 송신
#define BLUETOOTH_RX 10 //블루투스 수신
///////////////////////////////////////////////////////////////////////////////////////#define SOIL_MOISTURE A0//토양수분센서


//그 밖의 가시성을 위한 상수부
#define DHT_TYPE DHT11        //DHT의 Type(온도오차:2, 습도오차:5%) | DHT22(온도오차:0.5, 습도오차:2%) | Type는 하드웨어의 스펙을 보고 결정
#define NUMBER_OF_LED_PIXEL 8 //NeoPixel LED에 연결되어 있는 LED 갯수
#define DELAY_LOOP 3000       //루프의 지연시간


//전역변수부
int pumpStatus  = 0;  //워터펌프의 상태
int fanStatus   = 0;  //환풍기의 상태
int LEDStatus   = 0;  //LED의 상태

///////////////////////////////////////////////////////////////////////////////////////int soilValue         = 0;    //토양수분량의 아날로그 값
///////////////////////////////////////////////////////////////////////////////////////int convertSoilValue  = 0;    //토양수분량을 백분율로 변환한 값
int humidity          = 0;    //습도값
int temperature       = 0;    //온도값

String data = "";             //온습도, 토양수분량을 블루투스송신하기 전에 담는 변수
char sendMessage[100];        //블루투스송신을 위한 문자열(블루투스 송신 시 꼭 char배열에 담아서 보내야 함)
char receiveData = 0;         //블루투스 수신 데이터 변수(데이터가 바이트단위로 들어오기 때문에 char로 선언)
String receiveMessage = "";   //바이트단위로 받은 데이터를 저장하는 변수
int checkStatus = 1;
int inputControlCode      = 0;//안드로이드가 보낸 제어코드의 제어종류부분
int inputControlBehavior  = 0;//안드로이드가 보낸 제어코드의 제어량부분

///////////////////////////////////////////////////////////////////////////////////////int autoModeSoilMoistureValue = 0;  //자동모드에서의 기대 토양수분량 설정 값
int autoModeHumidityValue     = 90; //자동모드에서의 기대 습도 설정 값
int autoMode                  = 0;  //자동모드의 상태(1:ON, 0:OFF)


//사용자 정의 함수부
void changePumpStatusWithSW();                //Switch를 누를 때 워터펌프의 상태를 변경
void changeFanStatusWithSW();                 //Switch를 누를 때 환풍기의 상태를 변경
void changeLEDStatusWithSW();                 //Switch를 누를 때 NeoPixel LED의 상태를 변경
void changeAutoMode(int input);               //사용자가 자동모드, 수동모드 설정
void androidControlsPump(int input);          //수동모드일 때 사용자가 워터펌프를 제어
void androidControlsFan(int input);           //수동모드일 때 사용자가 환풍기를 제어
void androidControlsLED(int input);           //수동모드일 때 사용자가 NeoPixel LED를 제어
void fireDetect();
///////////////////////////////////////////////////////////////////////////////////////void autoMode_checkSoilMoisture(int input);   //자동모드일 때 토양수분량을 측정하여 설정된 기대 토양수분량에 따라 워터펌프 제어
void autoMode_checkHumidity(int input);       //자동모드일 때 습도를 측정하여 설정된 기대 습도에 따라 환풍기 제어
///////////////////////////////////////////////////////////////////////////////////////void setAutoModeSoilMoistureValue(int input); //사용자가 자동모드에서의 기대 토양수분량 설정
void setAutoModeHumidityValue(int input);     //사용자가 자동모드에서의 기대 습도 설정


//객체 선언부
DHT dht(TEMP_HUM, DHT_TYPE);  //온습도 객체 선언
SoftwareSerial BLUETOOTH(BLUETOOTH_TX, BLUETOOTH_RX); //블루투스통신을 위한 소프트웨어시리얼 객체 선언
Adafruit_NeoPixel neoLED = Adafruit_NeoPixel(NUMBER_OF_LED_PIXEL, NEO_LED, NEO_GRB + NEO_KHZ800);
/*
neoPixelLED를 위한 Adafruit_NeoPixel객체 선언

Parameter 1 = 링에 연결되어 있는 neoPixelLED LED 갯수
Parameter 2 = neoPixelLED에 연결하는데 사용하는 pin 번호
Parameter 3 = pixel type flags, add together as needed:
        - NEO_KHZ800 800 KHz bitstream (most NeoPixel products w/WS2812 LEDs)
        - NEO_KHZ400 400 KHz (classic 'v1' (not v2) FLORA pixels, WS2811 drivers)
        - NEO_GRB Pixels are wired for GRB bitstream (most NeoPixel products)
        - NEO_RGB Pixels are wired for RGB bitstream (v1 FLORA pixels, not v2)
*/


//아두이노 setUp부
void setup() {
  Serial.begin(9600);     //시리얼모니터 시작
  BLUETOOTH.begin(9600);  //블루투스 시작
  neoLED.begin();         //LED 시작
  dht.begin();

  for(int i=0;i<NUMBER_OF_LED_PIXEL;i++) {  //LED 활성화
    neoLED.setPixelColor(i, 0, 0, 0); //각 핀의 RGB값 설정
      ///////////////////////////////////////////////////////////////////////////////////////neoLED.show();                          //LED에 설정값 적용
  }

  pinMode(CONTROL_PUMP, OUTPUT);
  pinMode(CONTROL_FAN, OUTPUT);
  pinMode(SW_PUMP, INPUT);
  pinMode(SW_FAN, INPUT);
  pinMode(SW_LED, INPUT);

  digitalWrite(CONTROL_PUMP, !pumpStatus);
  digitalWrite(CONTROL_FAN, !fanStatus);
}


//아두이노 loop부
void loop() {
  ///////////////////////////////////////////////////////////////////////////////////////soilValue         = analogRead(SOIL_MOISTURE);          //토양수분값 가져오기
  ///////////////////////////////////////////////////////////////////////////////////////convertSoilValue  = map(soilValue, 220, 1023, 100, 0);  //토양수분값 비율로 전환
  humidity          = dht.readHumidity();                 //습도값 가져오기
  temperature       = dht.readTemperature();              //온도값 가져오기
  
  ///////////////////////////////////////////////////////////////////////////////////////data = "  "+(String)convertSoilValue + "," + (String)humidity + "," + (String)temperature + "," + (String)autoMode + "," + (String)pumpStatus + "," + (String)fanStatus + "," + (String)LEDStatus;

    //온습도, 토양수분량을 String에 저장
    //+ ","     //앞뒤로 한칸씩 주는 이유는 라즈베리파이에서 블루투스를 수신할 때
    //앞DP 한칸씩을 띄워야 온전한 데이터가 나오기 때문임. 나중에 더 디버깅할 필요 있음
  if((humidity != 0)&&(temperature != 0)) {
    data = " " + (String)humidity + "," + (String)temperature + ",";

    data.toCharArray(sendMessage, data.length()+1); //String에 저장된 데이터를 char배열로 옮김
    BLUETOOTH.write(sendMessage);                   //블루투스 송신
    delay(100);
    data = " " + (String)checkStatus+ "," + (String)autoMode + "," + (String)pumpStatus + "," + (String)fanStatus + "," + (String)LEDStatus;
    data.toCharArray(sendMessage, data.length()+1);
    BLUETOOTH.write(sendMessage);
  
  }
  
  changePumpStatusWithSW(); //스위치가 눌리면 워터펌프 상태 변경
  changeFanStatusWithSW();  //스위치가 눌리면 환풍기 상태 변경
  changeLEDStatusWithSW();  //스위치가 눌리면 LED 상태 변경

  ///////////////////////////////////////////////////////////////////////////////////////autoMode_checkSoilMoisture(convertSoilValue); //자동모드일 때 설정값 check
  autoMode_checkHumidity(humidity);             //자동모드일 때 설정값 check

  while(BLUETOOTH.available()) {    //라즈베리파이로부터 수신된 블루투스 값이 있을 때
    receiveData = BLUETOOTH.read(); //수신데이터를 받음(바이트단위로 받음)
    if((receiveData >= '0' && receiveData <= '9') || receiveData == '\n') { //수신데이터를 String에 저장(수신할 때 이상한 값을 필터링)
      receiveMessage.concat(receiveData);
    }
    if(receiveData == '\n') {         //수신데이터가 끝났을 때
      Serial.println(receiveMessage); /////////////////////////////////////////////////디버깅 코드
      inputControlCode = receiveMessage.toInt()/1000;    //제어코드의 제어종류부분을 분리
      inputControlBehavior = receiveMessage.toInt()%1000;//제어코드의 제어량부분을 분리

      /*안드로이드 제어코드
      1001:워터펌프 활성화  | 1000:워터펌프 비활성화
      2001:환풍기 활성화    | 2000:환풍기 비활성화
      3001:LED 활성화       | 3000:LED 비활성화
      4***:자동모드 토양수분량 설정(10 ~ 99)
      5***:자동모드 습도 조절(10 ~ 99)
      6000:화재감지시 워터펌프 활성화
      9001:자동모드 ON      | 9000:자동모드 OFF
      */
      switch(inputControlCode) {
        case 1:
          androidControlsPump(inputControlBehavior);  //수동모드일 때 사용자가 워터펌프를 제어
          break;
        case 2:
          androidControlsFan(inputControlBehavior);   //수동모드일 때 사용자가 환풍기를 제어
          break;
        case 3:
          androidControlsLED(inputControlBehavior);   //수동모드일 때 사용자가 NeoPixel LED를 제어
          break;
          ///////////////////////////////////////////////////////////////////////////////////////case 4:
            ///////////////////////////////////////////////////////////////////////////////////////setAutoModeSoilMoistureValue(inputControlBehavior); //자동모드일 때 기대 토양수분량 설정
            ///////////////////////////////////////////////////////////////////////////////////////break;          
        case 5:
          setAutoModeHumidityValue(inputControlBehavior);     //자동모드일 때 기대 습도 설정
          break;
        case 6:
          fireDetect();
          break;
        case 9:
          changeAutoMode(inputControlBehavior);       //사용자가 자동모드, 수동모드 설정
        default:
          break;
      }
      receiveMessage = "";  //String 초기화
    }
  }
  
  delay(DELAY_LOOP);  //아두이노의 과부화를 줄이기 위해 딜레이 설정
}


//사용자 함수 구현부
//Switch를 누를 때 워터펌프의 상태를 변경
void changePumpStatusWithSW() {
  if(digitalRead(SW_PUMP) == HIGH) {  //스위치가 눌렸을 때
    pumpStatus = !pumpStatus;         //현재 워터펌프의 상태를 변경
  }
  digitalWrite(CONTROL_PUMP, !pumpStatus);
}

//Switch를 누를 때 환풍기의 상태를 변경
void changeFanStatusWithSW() {
  if(digitalRead(SW_FAN) == HIGH) {
    fanStatus = !fanStatus;
  }
  digitalWrite(CONTROL_FAN, !fanStatus);
}

//Switch를 누를 때 NeoPixel LED의 상태를 변경
void changeLEDStatusWithSW() {
  if(digitalRead(SW_LED) == HIGH) {
    LEDStatus = !LEDStatus;
    if(LEDStatus == false) {
       for(int i=0;i<NUMBER_OF_LED_PIXEL;i++) {
        neoLED.setPixelColor(i, 0, 0, 0);
        neoLED.show();
       }
    } else {
      for(int i=0;i<NUMBER_OF_LED_PIXEL;i++) {
        neoLED.setPixelColor(i, 255, 255, 255);
        neoLED.show();
      }
    }
  }
}

//사용자가 자동모드, 수동모드 설정
void changeAutoMode(int input) {
  if(input == 1) {
    autoMode = 1;
  } else {
    autoMode = 0;
  }
}

//수동모드일 때 사용자가 워터펌프를 제어
void androidControlsPump(int input) {
  if(input == 1) {
    pumpStatus = true;
  } else {
    pumpStatus = false;
  }
  digitalWrite(CONTROL_PUMP, !pumpStatus);
}

//화재감지시 워터펌프 ON
void fireDetect() {
    pumpStatus = true;    
    digitalWrite(CONTROL_PUMP, !pumpStatus);
    delay(4000);
    pumpStatus = false;
    digitalWrite(CONTROL_PUMP, !pumpStatus);
}

//수동모드일 때 사용자가 환풍기를 제어
void androidControlsFan(int input) {
  if(input == 1){
    fanStatus = true;
  } else {
    fanStatus = false;
  }
  digitalWrite(CONTROL_FAN, !fanStatus);
}

//수동모드일 때 사용자가 NeoPixel LED를 제어
void androidControlsLED(int input) {
  if(input == 1) {
    for(int i=0;i<NUMBER_OF_LED_PIXEL;i++) {
      neoLED.setPixelColor(i, 255, 255, 255);
      neoLED.show();
    }
  } else {
    for(int i=0;i<NUMBER_OF_LED_PIXEL;i++) {
      neoLED.setPixelColor(i, 0, 0, 0);
      neoLED.show();
    }
  }
}

  /////////////////////////////////////////////////////////////////////////////////////////자동모드일 때 토양수분량을 측정하여 설정된 기대 토양수분량에 따라 워터펌프 제어
  /*
void autoMode_checkSoilMoisture(int input) {
  if(autoMode == 1) {
    if(input < autoModeSoilMoistureValue) {
      digitalWrite(CONTROL_PUMP, LOW);
    } else {
      digitalWrite(CONTROL_PUMP, HIGH);
    } 
  }
}
*/

//자동모드일 때 습도를 측정하여 설정된 기대 습도에 따라 환풍기 제어
void autoMode_checkHumidity(int input) {
  if(autoMode == 1) {
    if(input > autoModeHumidityValue) {
      digitalWrite(CONTROL_FAN, LOW);
    } else {
      digitalWrite(CONTROL_FAN, HIGH);
    }
  }
}

  /////////////////////////////////////////////////////////////////////////////////////////사용자가 자동모드에서의 기대 토양수분량 설정
  /*
void setAutoModeSoilMoistureValue(int input) {
  autoModeSoilMoistureValue = input;
}
*/

//사용자가 자동모드에서의 기대 습도 설정
void setAutoModeHumidityValue(int input) {
  autoModeHumidityValue = input;
}
