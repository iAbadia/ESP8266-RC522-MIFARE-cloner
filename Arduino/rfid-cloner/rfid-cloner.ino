#include <ESP8266WiFi.h>
#include <SPI.h>
#include <MFRC522.h>

/* wiring the MFRC522 to ESP8266 (ESP-12)
RST     = GPIO9
SDA(SS) = GPIO10 
MOSI    = GPIO13/D7
MISO    = GPIO12/D6
SCK     = GPIO14/D5
GND     = GND
3.3V    = 3.3V
*/

#define RST_PIN D4
#define SS_PIN  D8

MFRC522 mfrc522(SS_PIN, RST_PIN); // Create MFRC522 instance

/* WIFI */
const char* ssid     = "OpenWRT";
const char* password = "9484113580";

void setup() {
  // Serial init
  Serial.begin(115200);    // Initialize serial communications
  delay(250);
  Serial.println(F("Booting...."));

  // SPI init
  Serial.print(F("SPI... "));
  SPI.begin();
  Serial.println(F("ready!"));

  delay(50);
  // RC522 init
  Serial.println(F("RC522..."));
  mfrc522.PCD_Init();
  mfrc522.PCD_Init();
  Serial.print(" [+] ");
  mfrc522.PCD_DumpVersionToSerial();
  Serial.println(F("RC522... ready!"));

  // WIFI
  Serial.println(F("Wifi..."));
  Serial.print(F(" [+] Connecting to "));
  Serial.print(ssid);
  Serial.println(F("..."));

  WiFi.begin(ssid, password);
  Serial.println(F("Wifi... ready!"));

  // Print header
  Serial.println(F("======================================================")); 
  Serial.print(F("Scanning for cards... "));
}

void loop() { 
  // Look for new cards
  bool newCard = false;

  //yield();
  
  while (!newCard) {
    delay(100);
    yield();
    newCard = mfrc522.PICC_IsNewCardPresent();
  }
  Serial.println(F("Card found!"));
  // Select one of the cards
  yield();
  if ( ! mfrc522.PICC_ReadCardSerial()) {
    delay(50);
    return;
  }
  // Show some details of the PICC (that is: the tag/card)
  Serial.print(F("[+] Card UID: "));
  dump_byte_array(mfrc522.uid.uidByte, mfrc522.uid.size);
  Serial.println();
}

// Helper routine to dump a byte array as hex values to Serial
void dump_byte_array(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}
