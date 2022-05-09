/*
 * This sketch is uploaded to an arduino board, which is then supposed to be controlled 
 * by another arduino board (in this case a NodeMCU) via I2C.
 * The code for the I2C part is mainly inspired by cunchem https://github.com/cunchem/I2C_esp8266ToArduinoUno
*/


#include <Wire.h>
#include <Encoder.h>

int valA, valB;
Encoder encA(2, 5);
Encoder encB(6, 7);

const int PWN_A = 3;
const int PWN_B = 11;
const int DIR_A = 12;
const int DIR_B = 13;

String messageNode;
boolean match;

void setup() {
  Wire.begin(8);                /* join i2c bus with address 8 */
  Wire.onReceive(receiveEvent); /* register receive event */
  Wire.onRequest(requestEvent); /* register request event */
  Serial.begin(115200);           /* start serial for debug */
  encA.write(0);
  encB.write(0);
  pinMode(12, OUTPUT);    //pin Direction
  pinMode(3, OUTPUT);    //pin PWM
  pinMode(13, OUTPUT);
  pinMode(11, OUTPUT);
  analogWrite(11, 0);
  analogWrite(3, 0);
}

void loop() {
  valA = encA.read();          // read position
  valB = encB.read();
  Serial.print(millis()); // print the position
  Serial.print(" ; ");
  Serial.print(valA);
  Serial.print(" ; ");
  Serial.println(valB);

  char msgCode[] = {'a', 'a', 'a'};

  if (messageNode.length() == 3) {
    msgCode[0] = messageNode.charAt(0);
    msgCode[1] = messageNode.charAt(1);
    msgCode[2] = messageNode.charAt(2);
    Serial.println(" | coded as : " + String(msgCode[0]) + "   " + String(msgCode[1]) + "   " + String(msgCode[2]));
    messageNode = "";
  }

  switch (msgCode[0]) {

    case 'o':
      switch (msgCode[1]) {
        case 'm':
          match = true;
          break;
        case 't':
          match = false;
          break;
      }
      break;
    case 's':  //stops the robot (emergency stop or call)
      analogWrite(3, 0);
      break;
    case 'm':  //command to move the motors
      switch (msgCode[1]) {
        case 's':
          analogWrite(3, 0);
          analogWrite(PWN_B, 0);
          break;
        case 'l':
          analogWrite(3, 100);
          digitalWrite(12, HIGH);
          break;
        case 'r':
          analogWrite(3, 100);
          digitalWrite(12, LOW);
          break;
        case 'u':
          analogWrite(PWN_B, 100);
          digitalWrite(DIR_B, HIGH);
          break;
        case 'd':
          analogWrite(PWN_B, 100);
          digitalWrite(DIR_B, LOW);
          break;
      }
      break;
  }

}

// function that executes whenever data is received from master
void receiveEvent(int howMany) {
  messageNode = "";
  while (0 < Wire.available()) {
    char c = Wire.read();      /* receive byte as a character */
    Serial.print(c);  /* print the character */
    messageNode += c;
  }
  Serial.println();             /* to newline */
  delay(50);
}

// function that executes whenever data is requested from master
void requestEvent() {
  Wire.write("Thanks master");  /*send string on request */
}
