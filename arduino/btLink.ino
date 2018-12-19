#include <SoftwareSerial.h>

#define bad 57600
 
SoftwareSerial BTSerial(12, 13);   //bluetooth module Tx:Digital 12 Rx:Digital 13 rx->tx,tx->rx

int temp[8];
int theta;
int alpha;
int result;
char buf[10];

void setup() {
  
  Serial.begin(bad);
  Serial2.begin(115200);
  BTSerial.begin(bad);
}
 
void loop() {
   Data();
   for(int k =0;k<8;k++){ 
   Serial.print(temp[k]); Serial.print("  ,");
  }
  Serial.println();
  delay(1000);

  result = alpha - theta;
  if(result<0) result=-result;
  sprintf(buf,"%d",result);
  Serial.println(buf);

  Serial2.write(buf);
  Serial2.print("\r\n");
  initTemp();

}
  void Data(){

    for(int i =0;i<20;i++){
      for(int j=0;j<8;j++){
        if (BTSerial.available()){
          int read;
          read=BTSerial.read();

          if(read==224)       temp[j]+=20;
          else if(read==192)  temp[j]+=10;
          else if(read==0)    temp[j]-=10;
          delay(10);
        }
      }
    }
    theta = temp[1];
    alpha = temp[7];
    return;
  }
  void initTemp(){
       for(int c =0;c<8;c++){
    temp[c]=0;
  }
  return;
  }
