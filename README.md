# Boccia ramp project
An android & arduino project. The android app is used to control motors via Arduino.

## Description

This project is made in the context of INSA Lyon 2nd year project, the P2I.
The goal of the project is to create an automated boccia ramp. This archive contains android code as well as arduino code. 
The android app is made to send information via internet to an arduino ESP8266 on a NodeMCU, which is then connected via I2C to an arduino MEGA2560 with a motor shield rev3 on top of it to control motors.

## Wiring

More details on wiring [here](Figures).
The boards are setup like so :


![Diagram of the arduino circuits](https://user-images.githubusercontent.com/104720049/169611251-0a82dc34-8386-4231-b798-ba25f5f7b2ab.jpg)


I used EMG30 motors from Robot Electronics. The arduino code is able to support the 2 motors in direction and PWN, as well as get the encoder position from each motor.
You can use any DC motor if you don't need the encoder position, just ignore the Hall sensor pins.
