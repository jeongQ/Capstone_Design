#include <SoftwareSerial.h>

SoftwareSerial esp8266(10, 11);
#define LED 4//indicator, Grove - LED is connected with D4 of Arduino
boolean led_state = LOW;//state of LED, each time an external interrupt 

unsigned char counter;
unsigned long temp[21];
unsigned long sub;
bool data_effect=true;
unsigned int heart_rate;//the measurement result of heart rate
const int max_heartpluse_duty = 2000;//you can change it follow your
int value;
char buffer1[20];               //통신을 할때 buffer배열에 전송받은 데이터 입력
char bufferIndex = 0; 

void receiveBrain(){
  int c;
  while(Serial2.available()) {
    //c =Serial2.read();
    buffer1[bufferIndex] = Serial2.read();   //시리얼 통신으로 버퍼배열에 데이터 수신
    bufferIndex++;                          //데이터 수신 후 버퍼 인덱스 1 증가
  }         
      int pos = atoi(buffer1);    
      esp8266.write("AT+CIPSEND=5\r\n");
      delay(100);
      Serial.print("BR:");
      Serial.println(pos);
      esp8266.write("Br:");
      esp8266.print(pos);
      esp8266.print("\r\n");
     
  for(int a=0;a<21;a++) {
    buffer1[a] = NULL;
  }
  bufferIndex = 0;
    delay(1000);
}
void sendCO2(){
  esp8266.write("AT+CIPSEND=7\r\n");
  delay(100);
  esp8266.write("CO2:");
  esp8266.print(value);
  esp8266.print("\r\n");
  delay(1000);
  
}
void setup() {
  // Open serial communications and wait for port to open:
  Serial.begin(115200);
  Serial2.begin(115200);
  while (!Serial && !esp8266) {
    ; // wait for serial port to connect. Needed for native USB port only
  }

  Serial.println("Started");

  // set the data rate for the SoftwareSerial port
  esp8266.begin(115200);
  esp8266.write("AT+CIPSTART=\"UDP\",\"13.209.8.155\",2222\r\n");
  delay(100);
  arrayInit();
  Serial.println("Heart rate test begin.");
  attachInterrupt(0, interrupt, RISING);//set interrupt 0,digital port
  

}

void loop() {
  //Serial.print("Sample value:");
  //Serial.println(analogRead(0));
  value =analogRead(0);
  //Serial.println(value);
  delay(1000);
  if(value >= 300){
    sendCO2();
  }
  receiveBrain();
  if (esp8266.available()) {
    Serial.write(esp8266.read());
  }
  if (Serial.available()) {
    esp8266.write(Serial.read());
  }
  
}
void sum()
{
 if(data_effect)
 {
 heart_rate=1200000/(temp[20]-temp[0]);//60*20*1000/20_total_time
 Serial.print("Heart_rate_is:\t");
 Serial.println(heart_rate);
  esp8266.write("AT+CIPSEND=5\r\n");
  delay(100);
  esp8266.write("HT:");
  esp8266.print(heart_rate);
  esp8266.print("\r\n");
  delay(1000);
 
 }
 data_effect=1;//sign bit
}
/*Function: Interrupt service routine.Get the sigal from the external
interrupt*/
void interrupt()
{
 temp[counter]=millis();
 //Serial.println(counter,DEC);
 //Serial.println(temp[counter]);

 switch(counter)
 {
 case 0:
 sub=temp[counter]-temp[20];
 //Serial.println(sub);
 break;
 default:
 sub=temp[counter]-temp[counter-1];
 //Serial.println(sub);
 break;
 }
 if(sub>max_heartpluse_duty)//set 2 seconds as max heart pluse duty
 {
 data_effect=0;//sign bit
 counter=0;
 Serial.println("Heart rate measure error,test will restart!" );
 arrayInit();
 }
 if (counter==20&&data_effect)
 {
 counter=0;
 sum();
 }
 else if(counter!=20&&data_effect)
 counter++;
 else
 {
 counter=0;
 data_effect=1;
 }

}
/*Function: Initialization for the array(temp)*/
void arrayInit()
{
 for(unsigned char i=0;i < 20;i ++)
 {
 temp[i]=0;
 }
 temp[20]=millis();
}

