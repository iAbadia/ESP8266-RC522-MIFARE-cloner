# RFID Reader / Writer / Cloner

Proyecto de Iñaki Abadia para la asignatura de Laboratorio de Sistemas Empotrados, EINA (UNIZAR) 2016/17.

## Estructura

En la carpeta [ESP8266](https://github.com/iAbadia/Laboratorio-Empotrados-2016-17/tree/master/ESP8266) se encuentra el proyecto para el ESP8266 en entorno de desarrollo Arduino (Arduino IDE).

En la carpeta [Android](https://github.com/iAbadia/Laboratorio-Empotrados-2016-17/tree/master/Android) se encuentra el proyecto de Android Studio.

En la carpeta [Docs](https://github.com/iAbadia/Laboratorio-Empotrados-2016-17/tree/master/Docs) se encuentran los PDFs de la presentacin del proyecto. Actualmente hay una versin en Ingls y otra en Español.

## Dependencias

### Android
Las dependencias del proyecto Android se encuentran registradas en el gradle.build pertinente, serán descargadas al sincronizar el proyecto

### ESP8266
Las dependencias del proyecto para ESP8266 son las siguientes:

* ESP8266: Libreria de la comunidad para ESP8266 en entorno de desarrollo Arduino. Disponible en su [GitHub](https://github.com/esp8266/Arduino).
* RC522: Libreria de Miguel Balboa para el lector/escritor RFID MFRC522. Disponible en su [GitHub](https://github.com/miguelbalboa/rfid).
