/*
   This sketch is uploaded to an arduino board, which is then supposed to be controlled
   by another arduino board (in this case a NodeMCU) via I2C.
   The code for the I2C part is mainly inspired by cunchem https://github.com/cunchem/I2C_esp8266ToArduinoUno
*/

#include <Wire.h>
#include <Encoder.h>
#include <ezButton.h>
#include <limits.h>

ezButton switchAngle1(27);  // create ezButton object that attach to pin;
ezButton switchAngle2(29);
ezButton switchHeight1(23);
ezButton switchHeight2(25);

ezButton emerButton(33);
int stateEmer;
boolean forceStop;

const int VERY_BIG = INT_MAX;

boolean calibAngleMax, calibAngleMin, calibHeightMax, calibHeightMin, firstCalib;
int encAngleMin, encAngleMax;
int encHeightMin, encHeightMax;
int commandCalAngle, commandCalHeight;
int angle1, angle2, height1, height2;

//possible colors of the LED
const byte BLACK = 0b111;
const byte GREEN = 0b010;
const byte RED = 0b100;

//encoder related
int valA, valH;
Encoder encA(2, 5);
Encoder encH(6, 7);

//PID related
int errorA, errorH;
float kpAngle = 12;
float kpHeight = 12;
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
const byte PIN_LED_R = 49;
const byte PIN_LED_G = 50;
const byte PIN_LED_B = 51;

char msgCode[3];

String messageNode;
boolean match;

void setup() {
  Wire.begin(8);                //join i2c bus with address 8
  Wire.onReceive(receiveEvent); //register receive event
  Wire.onRequest(requestEvent); //register request event
  Serial.begin(115200);           //start serial for debug
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
  emerButton.setDebounceTime(50);
  commandCalAngle = 100;
  commandCalHeight = 75;
  calibAngleMax = false;
  calibAngleMin = false;
  calibHeightMax = false;
  calibHeightMin = false;
  firstCalib = false;
  forceStop = false;
  match = true;
  targetAngle = 50;
  targetHeight = 50;
  Serial.println("MEGA connected");
}

void loop() {
  switchAngle1.loop();
  switchAngle2.loop();
  switchHeight1.loop();
  switchHeight2.loop();
  emerButton.loop();

  stateEmer = emerButton.getState();
  angle1 = switchAngle1.getState();
  angle2 = switchAngle2.getState();
  height1 = switchHeight1.getState();
  height2 = switchHeight2.getState();

  t2 = millis();
  dt = t2 - t1;
  t1 = t2;

  valA = encA.read();  //read positions of the encoders
  valH = encH.read();

  if (messageNode.length() == 3) messageNode = decodeMessage(messageNode);

  if (stateEmer == LOW) forceStop = false;
  checkStop();
  if (msgCode[0] == 'r') calibrate();

  getMode();
  if (match) {
    controlMatch();
  } else if (firstCalib) {
    controlTrain();
    regulateAngle(targetAngle);
    regulateHeight(targetHeight);
  }
}

void checkStop() {
  if (msgCode[0] == 's' || stateEmer == HIGH) {
    msgCode[0] = 's';
    motors(PWN_A, 0);
    motors(PWN_H, 0);
    targetAngle = VERY_BIG;
    targetHeight = VERY_BIG;
    forceStop = true;
    led(RED);
    if (msgCode[1] == 'c') led(GREEN);
  }
  if (msgCode[0] == 'c' && stateEmer == LOW) led(BLACK);
}

void calibrate() {
  targetAngle = VERY_BIG;
  targetHeight = VERY_BIG;
  motors(PWN_A, commandCalAngle);
  motors(PWN_H, commandCalHeight);
  if (switchAngle1.isPressed()) {
    encAngleMin = valA;
    commandCalAngle = -commandCalAngle;
    if (calibAngleMax) commandCalAngle = 0;
    calibAngleMin = true;
  }
  if (switchHeight1.isPressed()) {
    encHeightMin = valH;
    commandCalHeight = -commandCalHeight;
    if (calibHeightMax) commandCalHeight = 0;
    calibHeightMin = true;
  }

  if (switchHeight2.isPressed()) {
    encHeightMax = valH;
    commandCalHeight = -commandCalHeight;
    if (calibHeightMin) commandCalHeight = 0;
    calibHeightMax = true;
  }
  if (switchAngle2.isPressed()) {
    encAngleMax = valA;
    commandCalAngle = -commandCalAngle;
    if (calibAngleMin) commandCalAngle = 0;
    calibAngleMax = true;
  }

  if (calibAngleMax && calibHeightMax && calibAngleMin && calibHeightMin) {
    msgCode[0] = 'z';
    firstCalib = true;
    calibAngleMax = false;
    calibAngleMin = false;
    calibHeightMax = false;
    calibHeightMin = false;
    motors(PWN_A, commandCalAngle);
    motors(PWN_H, commandCalHeight);
    commandCalAngle = 100;
    commandCalHeight = 75;
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
          switch (msgCode[2]) {
            case 'a':
              motors(PWN_A, -100);
              break;
          }
          break;
        case 'r':
          switch (msgCode[2]) {
            case 'a':
              motors(PWN_A, 100);
              break;
          }
          break;
        case 'u':
          switch (msgCode[2]) {
            case 'a':
              motors(PWN_H, 100);
              break;
          }
          break;
        case 'd':
          switch (msgCode[2]) {
            case 'a':
              motors(PWN_H, -50);
              break;
          }
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
      regulateAngle(targetAngle);
      break;
    case 'h':
      strH += msgCode[1];
      strH += msgCode[2];
      targetHeight = strH.toInt();
      regulateHeight(targetHeight);
      break;
  }
}

void regulateAngle(int target) {
  target = map(target, 0, 99, encAngleMin, encAngleMax);
  if (targetAngle < VERY_BIG) {
    errorA = target - valA;
    motors(PWN_A, -kpAngle * errorA);
  }
}

void regulateHeight(int target) {
  target = map(target, 0, 99, encHeightMin, encHeightMax);
  if (targetHeight < VERY_BIG) {
    errorH = target - valH;
    motors(PWN_H, kpHeight * errorH);
  }
}

void motors(int motor, int value) {
  if (!forceStop) {
    int dir;
    dir = value > 0 ? HIGH : LOW;
    value = value > 100 ? 100 : value;
    value = value < -100 ? -100 : value; //keep the command below 100 for safety
    switch (motor) {
      case PWN_A:
        if ((angle1 == HIGH && angle2 == HIGH) || (angle1 == LOW && value > 0) || (angle2 == LOW && value < 0)) {
          analogWrite(PWN_A, abs(value));
          digitalWrite(DIR_A, dir);
        } else {
          analogWrite(PWN_A, 0);
        }
        break;
      case PWN_H:
        if ((height1 == HIGH && height2 == HIGH) || (height1 == LOW && value > 0) || (height2 == LOW && value < 0)) {
          value = value < -50 ? -50 : value;
          analogWrite(PWN_H, abs(value));
          digitalWrite(DIR_H, dir);
        } else {
          analogWrite(PWN_H, 0);
        }
        break;
    }
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
    char c = Wire.read();      //receive byte as a character
    Serial.print(c);  //print the character
    messageNode += c;
  }
  Serial.println(); //to newline
  delay(50);
}

// function that executes whenever data is requested from master
void requestEvent() {
  Wire.write("Thanks master");  //send string on request
}
