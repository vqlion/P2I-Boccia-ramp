# Boccia ramp project
An android & arduino project. The android app is used to control motors via Arduino.

## Description

This project is made in the context of INSA Lyon 2nd year project, the P2I.
The goal of the project is to create an automated boccia ramp. This archive contains android code as well as arduino code. 
The android app is made to send information via internet to an arduino ESP8266 on a NodeMCU, which is then connected via I2C to an arduino MEGA2560 with a motor shield rev3 on top of it to control motors.

## Wiring

The arduino boards are setup like so :
![Diagram of the arduino circuits](https://user-images.githubusercontent.com/104720049/167480165-e51c1c9a-a689-44f1-9a7d-eee097607fe5.jpg)

I used EMG30 motors from Robot Electronics. The arduino code is able to support the 2 motors in direction and PWN, as well as get the encoder position from each motor.
