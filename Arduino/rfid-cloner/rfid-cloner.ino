#include <ESP8266WiFi.h>
#include <SPI.h>
#include <MFRC522.h>
#include <FS.h>

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

/* STORAGE */
#define CARDS_DIR "/c/"
#define MAX_CARDS_N 30
String cards_list[MAX_CARDS_N];
int cards_count = 0;

void setup() {
  // Serial init
  Serial.begin(115200);    // Initialize serial communications
  Serial.println(F("\nBooting...."));

  // Filesystem
  delay(50);
  Serial.print(F("Filesystem... "));
  bool fs = SPIFFS.begin();
  Serial.println(fs ? "ready!" : "failed!");

  // Update cards list
  Serial.print(F("Checking local cards... "));
  update_cards_list();
  Serial.print(F("found "));
  Serial.print(cards_count);
  Serial.println(F(" cards!"));

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
  yield();  
  if (! mfrc522.PICC_IsNewCardPresent()) {
    delay(50);
    return;
  }
  Serial.println(F("Card found!"));
  
  // Select one of the cards
  yield();
  if (!mfrc522.PICC_ReadCardSerial()) {
    delay(50);
    return;
  }

  save_card(&mfrc522);
  // Show some details of the PICC (that is: the tag/card)
  /*Serial.print(F("[+] Card UID: "));
  dump_byte_array(mfrc522.uid.uidByte, mfrc522.uid.size);
  Serial.println();*/
}

// Helper routine to dump a byte array as hex values to Serial
void dump_byte_array(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}

void save_card(MFRC522* mfrc) {
  String uid = get_card_uid(mfrc522.uid.uidByte, mfrc522.uid.size);
  if (!check_card(uid)) {
    // Card not in flash, save it
    File card_f = SPIFFS.open(CARDS_DIR+uid, "w+");
    card_f.print(uid);
    card_f.seek(0, SeekSet");
    Serial.write(card_f.read());
    Serial.print(F("Card "));
    Serial.print(uid);
    Serial.println(F(" stored!"));
  } else {
    // Card already in flash, skip it
    Serial.print(F("Card "));
    Serial.print(uid);
    Serial.println(F(" already stored, skipped."));
  }
}

bool check_card(String uid) {
  return SPIFFS.exists(uid);
}

String get_card_uid(byte *uid_buffer, byte bufferSize) {
  String uid = "";
  for (byte i = 0; i < bufferSize; i++) {
    uid += uid_buffer[i] < 0x10 ? " 0" : "";
    uid += uid_buffer[i];
  }
  return uid;
}

/* Updates global variable cards_list */
void update_cards_list() {
  Dir dir = SPIFFS.openDir(CARDS_DIR);
  cards_count = 0;
  for(int i = 0; dir.next(); i++) {
    cards_list[i] = dir.fileName();
    cards_count++;
    if (i >= MAX_CARDS_N) break;
  }
}

void print_fs() {
  Serial.println(F("ALL FILES IN FILESYTEM"));
  Serial.println(F("======================"));
  Dir dir = SPIFFS.openDir("/");
  while (dir.next()) {
      Serial.print(dir.fileName());
      File f = dir.openFile("r");
      Serial.println(f.size());
  }
  Serial.println(F("======================"));
}

