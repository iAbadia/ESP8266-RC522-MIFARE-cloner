# RFID Reader / Writer / Cloner

Iñaki Abadia's project for Embedded Systems Laboratory 2016/17, EINA (UNIZAR). This is a prototype, developed with academic purposes ONLY.

<p align="center">
  <img src="https://raw.githubusercontent.com/iAbadia/Laboratorio-Empotrados-2016-17/master/Media/rfid-cloner.jpg"/>
</p>

## Structure

Folder [ESP8266](https://github.com/iAbadia/Laboratorio-Empotrados-2016-17/tree/master/ESP8266) contains Arduino IDE project for ESP8266.

Folder [Android](https://github.com/iAbadia/Laboratorio-Empotrados-2016-17/tree/master/Android) contains Android Studio project.

Folder [Docs](https://github.com/iAbadia/Laboratorio-Empotrados-2016-17/tree/master/Docs) contains the project slides in PDF. English and Spanish versions available at the moment.

Folder [Media](https://github.com/iAbadia/Laboratorio-Empotrados-2016-17/tree/master/Media) contains pictures about this project.

## Dependencies

### Android
Android project dependencies are properly registered in gradle.build, they'll be automatically downloaded at Gradle sync.

### ESP8266
ESP8266 project dependencies:

* ESP8266: Community library for Arduino developement evironment, Arduino IDE. Available on [GitHub](https://github.com/esp8266/Arduino).
* RC522: Miguel Balboa's library for MFRC522 reader/writer. Available at his [GitHub](https://github.com/miguelbalboa/rfid).
