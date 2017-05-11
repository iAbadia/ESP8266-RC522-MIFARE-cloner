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

/* MFRC522 */

#define RST_PIN D4
#define SS_PIN  D8

MFRC522 mfrc522(SS_PIN, RST_PIN); // Create MFRC522 instance

byte buffer[18];
byte block;
byte card_dump[64][16];
#define BLOCK_SIZE 16
#define MIFARE_1K "MIFARE 1KB"
#define MIFARE_4K "MIFARE_4KB"

MFRC522::StatusCode status;

MFRC522::MIFARE_Key key;

#define NR_KNOWN_KEYS   8
byte knownKeys[NR_KNOWN_KEYS][MFRC522::MF_KEY_SIZE] =  {
  {0xff, 0xff, 0xff, 0xff, 0xff, 0xff}, // FF FF FF FF FF FF = factory default
  {0xa0, 0xa1, 0xa2, 0xa3, 0xa4, 0xa5}, // A0 A1 A2 A3 A4 A5
  {0xb0, 0xb1, 0xb2, 0xb3, 0xb4, 0xb5}, // B0 B1 B2 B3 B4 B5
  {0x4d, 0x3a, 0x99, 0xc3, 0x51, 0xdd}, // 4D 3A 99 C3 51 DD
  {0x1a, 0x98, 0x2c, 0x7e, 0x45, 0x9a}, // 1A 98 2C 7E 45 9A
  {0xd3, 0xf7, 0xd3, 0xf7, 0xd3, 0xf7}, // D3 F7 D3 F7 D3 F7
  {0xaa, 0xbb, 0xcc, 0xdd, 0xee, 0xff}, // AA BB CC DD EE FF
  {0x00, 0x00, 0x00, 0x00, 0x00, 0x00}  // 00 00 00 00 00 00
};

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
  delete_all_cards();

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
    delay(150);
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
}


/*###########################################################*/
/*######################  FILESYSTEM  #######################*/
/*###########################################################*/

/* Print all files in filesystem */
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

/*###########################################################*/
/*#################  CARDS IN FILESYSTEM  ###################*/
/*###########################################################*/

/* Reads card, decides wether to save or skip it 
     Return: TRUE  if Card saved
             FALSE if Card not saved
*/
bool save_card(MFRC522* mfrc) {
  String uid = get_card_uid(mfrc522.uid.uidByte, mfrc522.uid.size);
  if (!check_card(uid)) {
    // Card not in flash, save it
    read_card();
    return check_card(uid);
  } else {
    // Card already in flash, skip it
    Serial.print(F("Card "));
    Serial.print(uid);
    Serial.println(F(" already stored, skipped."));
    return false;
  }
}

/* Delete single card given UID
     Return: TRUE  if succesfully removed
             FALSE if not removed
*/
bool delete_card(String uid) {
  return SPIFFS.remove(CARDS_DIR + uid);
}

/* Delete all cards in filesystem
     Return: TRUE  if All cards successfully removed
             FALSE if Not all cards removed.
*/
bool delete_all_cards() {
  Dir dir = SPIFFS.openDir(CARDS_DIR);
  bool allgood = true;
  while (dir.next()) {
    allgood &= SPIFFS.remove(dir.fileName());
  }
  return allgood;
}

/* Check if card exists in filesystem
     Return: TRUE  if Card exists.
             False if Card doesn't exist
*/
bool check_card(String uid) {
  return SPIFFS.exists(CARDS_DIR + uid);
}

/* Updates global variable cards_list */
void update_cards_list() {
  Dir dir = SPIFFS.openDir(CARDS_DIR);
  cards_count = 0;
  for (int i = 0; dir.next(); i++) {
    cards_list[i] = dir.fileName();
    cards_count++;
    if (i >= MAX_CARDS_N) break;
  }
}

/*###########################################################*/
/*#########################  CARDS ##########################*/
/*###########################################################*/

/* Read card content block by block */
boolean try_key(MFRC522::MIFARE_Key *key) {
  boolean result = false;

  int picc_blocks_n = picc_blocks();

  for (byte block = 0; block < picc_blocks_n; block++) {

    // Serial.println(F("Authenticating using key A..."));
    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, block, key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) {
      Serial.print(F("PCD_Authenticate() failed: "));
      Serial.println(mfrc522.GetStatusCodeName(status));
      return false;
    }

    // Read block
    byte byteCount = sizeof(buffer);
    status = mfrc522.MIFARE_Read(block, buffer, &byteCount);
    if (status != MFRC522::STATUS_OK) {
      Serial.print(F("MIFARE_Read() failed: "));
      Serial.println(mfrc522.GetStatusCodeName(status));
    }
    else {
      // Successful read
      result = true;

      // Dump block data
      Serial.print(F("\nBlock ")); Serial.print(block); Serial.print (F(": "));
      // Print successful key
      dump_byte_array((*key).keyByte, MFRC522::MF_KEY_SIZE);
      Serial.println();
      dump_byte_array_file(buffer, BLOCK_SIZE);
      Serial.println();
    }
  }
  Serial.println();
  mfrc522.PICC_HaltA();       // Halt PICC
  mfrc522.PCD_StopCrypto1();  // Stop encryption on PCD
  return result;
}

/* Save card to filesystem */
void read_card() {
  // Show some details of the PICC (that is: the tag/card)
  Serial.print(F("Card UID:"));
  dump_byte_array(mfrc522.uid.uidByte, mfrc522.uid.size);
  Serial.println();
  Serial.print(F("PICC type: "));
  MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
  Serial.println(mfrc522.PICC_GetTypeName(piccType));

  // Try the known default keys
  MFRC522::MIFARE_Key key;
  for (byte k = 0; k < NR_KNOWN_KEYS; k++) {
    // Copy the known key into the MIFARE_Key structure
    for (byte i = 0; i < MFRC522::MF_KEY_SIZE; i++) {
      key.keyByte[i] = knownKeys[k][i];
    }
    // Try the key
    if (try_key(&key)) {
      // Found and reported on the key and block,
      // no need to try other keys for this PICC
      break;
    }
  }
}

/* Get active card's UID
     Return: Sting as Active card UID
*/
String get_card_uid(byte *uid_buffer, byte bufferSize) {
  String uid = "";
  for (byte i = 0; i < bufferSize; i++) {
    uid += uid_buffer[i] < 0x10 ? "0" : "";
    uid += uid_buffer[i];
  }
  return uid;
}

/* Get number of blocks given PICC type
     Return: Int as Active PICC's number of blocks
*/
int picc_blocks() {
  MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
  String picc = mfrc522.PICC_GetTypeName(piccType);
  if (picc.equals(MIFARE_1K)) {return 64;}
  else if(picc.equals(MIFARE_4K)) {return 256;}
  else {return 64;}
}

/*###########################################################*/
/*####################  HELPER ROUTINES #####################*/
/*###########################################################*/

/* Dump byte array to Serial */
void dump_byte_array(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? " 0" : " ");
    Serial.print(buffer[i], HEX);
  }
}

/* Dump byte array to File - FALSEEEEEEEEEEEEEE, yet */
void dump_byte_array_file(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? "0" : "");
    Serial.print(buffer[i], HEX);
  }
}

