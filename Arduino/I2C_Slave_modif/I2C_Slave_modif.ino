/*
   This sketch is uploaded to an arduino board, which is then supposed to be controlled
   by another arduino board (in this case a NodeMCU) via I2C.
   The code for the I2C part is mainly inspired by cunchem https://github.com/cunchem/I2C_esp8266ToArduinoUno
*/


#include <Wire.h>
#include <Encoder.h>

const byte BLACK = 0b111;
const byte RED = 0b010;

int valA, valB;
Encoder encA(2, 5);
Encoder encB(6, 7);

//motors pins
const int PWN_A = 3;
const int PWN_B = 11;
const int DIR_A = 12;
const int DIR_B = 13;

//RGB led pins
const byte PIN_LED_R = 35;
const byte PIN_LED_G = 33;
const byte PIN_LED_B = 31;

char msgCode[3];

String messageNode;
boolean match;

void setup() {
  Wire.begin(8);                /* join i2c bus with address 8 */
  Wire.onReceive(receiveEvent); /* register receive event */
  Wire.onRequest(requestEvent); /* register request event */
  Serial.begin(115200);           /* start serial for debug */
  encA.write(0);
  encB.write(0);
  pinMode(12, OUTPUT);    //pin Direction A
  pinMode(3, OUTPUT);    //pin PWM A
  pinMode(13, OUTPUT);  //pin Direction B
  pinMode(11, OUTPUT);  //pin PWN B
  pinMode(PIN_LED_R, OUTPUT);
  pinMode(PIN_LED_G, OUTPUT);
  pinMode(PIN_LED_B, OUTPUT);
  led(BLACK);
  analogWrite(11, 0);
  analogWrite(3, 0);
}

void loop() {
  valA = encA.read();          // read position
  valB = encB.read();
  /* Serial.print(millis()); // print the position
  Serial.print(" ; ");
  Serial.print(valA);
  Serial.print(" ; ");
  Serial.println(valB); */


  if (messageNode.length() == 3) {
    messageNode = decodeMessage(messageNode);
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
      motors(PWN_A, 0, HIGH);
      motors(PWN_B, 0, HIGH);
      switch (msgCode[1]) {
        case 'c':
          led(RED);
          break;
      }
      break;
    case 'c':
      led(BLACK);
      break;
    case 'm':  //command to move the motors
      switch (msgCode[1]) {
        case 's':
          switch (msgCode[2]) {
            case 'l' : case 'r' :
              motors(PWN_A, 0, HIGH);
              break;
            case 'u': case 'd':
              motors(PWN_B, 0, HIGH);
              break;
          }
          break;
        case 'l':
          motors(PWN_A, 100, HIGH);
          break;
        case 'r':
          motors(PWN_A, 100, LOW);
          break;
        case 'u':
          motors(PWN_B, 100, HIGH);
          break;
        case 'd':
          motors(PWN_B, 100, LOW);
          break;
      }
      break;
  }

}

void motors(int motor, int value, int dir) {
  switch (motor) {
    case PWN_A:
      analogWrite(PWN_A, value);
      digitalWrite(DIR_A, dir);
      break;
    case PWN_B:
      analogWrite(PWN_B, value);
      digitalWrite(DIR_B, dir);
      break;
  }
}

String decodeMessage(String msgNode) {
  msgCode[0] = msgNode.charAt(0);
  msgCode[1] = msgNode.charAt(1);
  msgCode[2] = msgNode.charAt(2);
  Serial.println(" | coded as : " + String(msgCode[0]) + "   " + String(msgCode[1]) + "   " + String(msgCode[2]));
  return "";
}

void led(byte color) {
  digitalWrite(PIN_LED_R, !bitRead(color, 2));
  digitalWrite(PIN_LED_G, !bitRead(color, 1));
  digitalWrite(PIN_LED_B, !bitRead(color, 0));
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
