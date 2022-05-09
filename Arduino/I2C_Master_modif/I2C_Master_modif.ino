/*
 * This sketch is uploaded to a nodeMCU, which is then supposed to control 
 * an another arduino board via I2C.
 * The code for the I2C part is mainly inspired by cunchem https://github.com/cunchem/I2C_esp8266ToArduinoUno
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

}

void loop() {
  char* reply = "";
  int packetSize = Udp.parsePacket();
  if (packetSize > 0) {
    //delay(500);

    char incomingPacket[255];
    int len = Udp.read(incomingPacket, 255);
    String message = String(incomingPacket);
    message = message.substring(0, len);
    Serial.print("message recu : " + message);

    char msgCode[] = {message.charAt(0), message.charAt(5), 'a' };
    if (message.length() > 9) {
      msgCode[2] = message.charAt(10);
    }

    Serial.println(" | coded as : " + String(msgCode[0]) + "   " + String(msgCode[1]) + "   " + String(msgCode[2]));

    switch (msgCode[0]) {
      case 'o' :
        switch (msgCode[1]) {
          case 'm' :
            match = true;
            reply = "match";
            break;
          case 't' :
            match = false;
            reply = "train";
            break;
        }
      default :
        reply = "once again we meet";
    }

    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    Udp.write(reply);
    Serial.println(reply);
    Udp.endPacket();

    /*
      digitalWrite(LED_BUILTIN, HIGH);
      delay(50);
      digitalWrite(LED_BUILTIN, LOW); */

    String messageNode = "";
    messageNode += msgCode[0];
    messageNode += msgCode[1];
    messageNode += msgCode[2];
    char buffer[32];
    messageNode.toCharArray(buffer, 32);

    Wire.beginTransmission(8); /* begin with device address 8 */
    Wire.write(buffer);  /* sends string message */
    Wire.endTransmission();    /* stop transmitting */

    Wire.requestFrom(8, 13); /* request & read data of size 13 from slave */
    while (Wire.available()) {
      char c = Wire.read();
      Serial.print(c);
    }
    Serial.println();
    //delay(1000);
  }
}
