/*
   This sketch is uploaded to a nodeMCU, which is then supposed to control
   an another arduino board via I2C.
   The code for the I2C part is mainly inspired by cunchem https://github.com/cunchem/I2C_esp8266ToArduinoUno
*/

#include <Wire.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <WiFiUdp.h>
#include <ESP8266WebServer.h>


WiFiUDP Udp;
const char* ssid = "P2I6";
const char* password = "";
int PORT = 5000;
String message;

boolean match = true;

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(115200); /* begin serial for debug */
  Wire.begin(D1, D2); /* join i2c bus with SDA=D1 and SCL=D2 of NodeMCU */
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("WiFi connected");
  Serial.println(WiFi.localIP());
  Udp.begin(PORT);
  Serial.println("NODE connected");

}

void loop() {
  char* reply = "";
  int packetSize = Udp.parsePacket();
  if (packetSize > 0) {
    message = receiveUDP();

    char msgCode[] = {message.charAt(0), message.charAt(5), 'a' };
    if (message.length() > 9) {
      msgCode[2] = message.charAt(10);
    }
    Serial.println(" | coded as : " + String(msgCode[0]) + "   " + String(msgCode[1]) + "   " + String(msgCode[2]));

    reply = "once again we meet";
    sendUDP(reply);

    String messageNode = "";
    messageNode += msgCode[0];
    messageNode += msgCode[1];
    messageNode += msgCode[2];
    sendI2C(messageNode);

    Wire.requestFrom(8, 13); /* request & read data of size 13 from slave */
    while (Wire.available()) {
      char c = Wire.read();
      Serial.print(c);
    }
    Serial.println();
  }
}

String receiveUDP() {
  char incomingPacket[255];
  int len = Udp.read(incomingPacket, 255);
  String msg = String(incomingPacket);
  Serial.print("message recu : " + msg.substring(0, len));
  return msg.substring(0, len);
}

void sendUDP(char* msg) {
  Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
  Udp.write(msg);
  Serial.println(msg);
  Udp.endPacket();
}

void sendI2C(String msg) {
  char buffer[32];
  msg.toCharArray(buffer, 32);

  Wire.beginTransmission(8); /* begin with device address 8 */
  Wire.write(buffer);  /* sends string message */
  Wire.endTransmission();    /* stop transmitting */
}
