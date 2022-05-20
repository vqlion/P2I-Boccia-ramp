/*
   This sketch is uploaded to an arduino board, which is then supposed to be controlled
   by another arduino board (in this case a NodeMCU) via I2C.
   The code for the I2C part is mainly inspired by cunchem https://github.com/cunchem/I2C_esp8266ToArduinoUno
*/

#include <Wire.h>
#include <Encoder.h>
#include <ezButton.h>

ezButton switchAngle1(27);  // create ezButton object that attach to pin;
ezButton switchAngle2(29);
ezButton switchHeight1(23);
ezButton switchHeight2(25);

const int VERY_BIG = 10000;

boolean calibAngle, calibHeight;
int encAngleMin, encAngleMax;
int encHeightMin, encHeightMax;
int commandCalAngle, commandCalHeight;

const byte BLACK = 0b111;
const byte RED = 0b010;

int valA, valH;
Encoder encA(2, 5);
Encoder encH(6, 7);

int errorA, errorH;
float kpAngle = 25;
float kpHeight = 25;
float t1 = 0;
float t2 = 0;
float dt;

int targetAngle, targetHeight;

//motors pins
const int PWN_A = 3;
const int PWN_H = 11;
const int DIR_A = 12;
const int DIR_H = 13;

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
  encH.write(0);
  pinMode(DIR_A, OUTPUT);    //pin Direction A
  pinMode(PWN_A, OUTPUT);    //pin PWM A
  pinMode(DIR_H, OUTPUT);  //pin Direction H
  pinMode(PWN_H, OUTPUT);  //pin PWN H
  pinMode(PIN_LED_R, OUTPUT);
  pinMode(PIN_LED_G, OUTPUT);
  pinMode(PIN_LED_B, OUTPUT);
  led(BLACK);
  analogWrite(PWN_A, 0);
  analogWrite(PWN_H, 0);
  switchAngle1.setDebounceTime(50);
  switchAngle2.setDebounceTime(50);
  switchHeight1.setDebounceTime(50);
  switchHeight2.setDebounceTime(50);
  commandCalAngle = 100;
  commandCalHeight = 100;
  calibAngle = false;
  calibHeight = false;
  Serial.println("MEGA connected");
}

void loop() {
  switchAngle1.loop();
  switchAngle2.loop();
  switchHeight1.loop();
  switchHeight2.loop();

  t2 = millis();
  dt = t2 - t1;
  t1 = t2;

  valA = encA.read();          // read positions of the encoders
  valH = encH.read();
  /* Serial.print(millis()); // print the position
    Serial.print(" ; ");
    Serial.print(valA);
    Serial.print(" ; ");
    Serial.println(valH); */

  if (messageNode.length() == 3) messageNode = decodeMessage(messageNode);

  checkStop();
  if (msgCode[0] == 'r') calibrate();

  getMode();
  if (match) {
    controlMatch();
  } else {
    controlTrain();
    regulateAngle();
    regulateHeight();
  }
}

void checkStop() {
  switch (msgCode[0]) {
    case 's':  //stops the robot (emergency stop or call)
      motors(PWN_A, 0);
      motors(PWN_H, 0);
      targetAngle = VERY_BIG;
      targetHeight = VERY_BIG;
      switch (msgCode[1]) {
        case 'c':
          led(RED);
          break;
      }
      break;
  }
}

void calibrate() {
  motors(PWN_A, commandCalAngle);
  motors(PWN_H, commandCalHeight);
  if (switchAngle1.isPressed()) {
    encAngleMin = valA;
    commandCalAngle = -100;
  }
  if (switchHeight1.isPressed()) {
    encHeightMin = valH;
    commandCalHeight = -100;
  }

  if (switchHeight2.isPressed()) {
    encHeightMax = valH;
    commandCalHeight = 10;
    motors(PWN_H, commandCalHeight);
    calibHeight = true;
  }
  if (switchAngle2.isPressed()) {
    encAngleMax = valA;
    commandCalAngle = 10;
    motors(PWN_A, commandCalAngle);
    calibAngle = true;
  }

  if (calibAngle && calibHeight) {
    msgCode[0] = 'z';
    motors(PWN_A, 0);
    motors(PWN_H, 0);
  }
}

void getMode() {
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
  }
}

void controlMatch() {
  switch (msgCode[0]) {
    case 'c':
      led(BLACK);
      break;
    case 'm':  //command to move the motors
      switch (msgCode[1]) {
        case 's':
          switch (msgCode[2]) {
            case 'l' : case 'r' :
              motors(PWN_A, 0);
              break;
            case 'u': case 'd':
              motors(PWN_H, 0);
              break;
          }
          break;
        case 'l':
          motors(PWN_A, 255);
          break;
        case 'r':
          motors(PWN_A, -255);
          break;
        case 'u':
          motors(PWN_H, 255);
          break;
        case 'd':
          motors(PWN_H, -255);
          break;
      }
      break;
  }
}

void controlTrain() {
  String strA = "";
  String strH = "";
  switch (msgCode[0]) {
    case 'a' :
      strA += msgCode[1];
      strA += msgCode[2];
      targetAngle = strA.toInt();
      targetAngle = map(targetAngle, 0, 99, encAngleMin, encAngleMax);
      regulateAngle();
      break;
    case 'h':
      strH += msgCode[1];
      strH += msgCode[2];
      targetHeight = strH.toInt();
      targetHeight = map(targetHeight, 0, 99, encHeightMin, encHeightMax);
      regulateHeight();
      break;
  }
}

void regulateAngle() {
  if (targetAngle < VERY_BIG) {
    errorA = targetAngle - valA;
    motors(PWN_A, kpAngle * errorA);
  }
}

void regulateHeight() {
  if (targetHeight < VERY_BIG) {
    errorH = targetHeight - valH;
    motors(PWN_H, kpHeight * errorH);
  }
}

void motors(int motor, int value) {
  int dir;
  dir = value > 0 ? HIGH : LOW;
  value = value > 255 ? 255 : value;
  value = value < -255 ? -255 : value;
  switch (motor) {
    case PWN_A:
      analogWrite(PWN_A, abs(value));
      digitalWrite(DIR_A, dir);
      break;
    case PWN_H:
      analogWrite(PWN_H, abs(value));
      digitalWrite(DIR_H, dir);
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
