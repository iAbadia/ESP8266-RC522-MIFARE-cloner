#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>
#include <ArduinoJson.h>
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
#define SECTOR_SIZE 4
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

typedef struct Sector {
  byte number;
  String key;
  String blocks[4];
};

/* WIFI */
const char* ssid     = "OpenWRT";
const char* password = "9484113580";

/* WebServer */
ESP8266WebServer server(80);

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
  Serial.println(F("Filesystem..."));
  //SPIFFS.format();
  bool fs = SPIFFS.begin();
  Serial.println(fs ? " [+] Filesystem ready!" : " [-] Filesystem failed!");
  //delete_all_cards();
  erase_fs();

  // Update cards list
  Serial.println(F("Local cards..."));
  update_cards_list();
  Serial.print(F(" [+] Found "));
  Serial.print(cards_count);
  Serial.println(F(" cards!"));

  // SPI init
  Serial.println(F("SPI..."));
  SPI.begin();
  Serial.println(F(" [+] SPI ready!"));

  delay(50);
  // RC522 init
  Serial.println(F("RC522..."));
  mfrc522.PCD_Init();
  mfrc522.PCD_Init();
  Serial.print(" [+] ");
  mfrc522.PCD_DumpVersionToSerial();
  Serial.println(F(" [+] RC522 ready!"));

  // WIFI
  Serial.println(F("Wifi..."));
  Serial.print(F(" [+] Connecting to "));
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(250);
  }
  Serial.print(F(" [+] Connected to "));
  Serial.println(ssid);
  Serial.print(" [+] IP address: ");
  Serial.println(WiFi.localIP());

  // WebServer
  Serial.println("WebServer... ");
  if (MDNS.begin("esp8266")) {
    Serial.println(" [+] MDNS responder ready (esp8266)");
  } else {
    Serial.println(" [-] MDNS responder failed.");
  }

  server.on("/", handle_root);
  server.onNotFound(handle_not_found);
  server.on("/listcards", handle_list_cards);
  server.on("/card", HTTP_GET, handle_get_card);
  server.on("/card", HTTP_PUT, handle_update_card);
  //server.on("/card", HTTP_PUT, [](){ server.send(200, "text/plain", ""); }, handle_update_card);
  server.on("/card", HTTP_DELETE, handle_delete_card);
  server.on("/readcard", HTTP_POST, handle_read_card);
  server.on("/writecard", HTTP_POST, handle_write_card);
  server.begin();
  Serial.println(" [+] WebServer ready!");

  // Print header
  Serial.println(F("======================================================"));
  Serial.print(F("Scanning for cards... "));

}

void loop() {
  // Serve clients
  yield();
  server.handleClient();
  // Look for new cards
  yield();
  if (! mfrc522.PICC_IsNewCardPresent()) {
    delay(50);
    return;
  }
  Serial.println(F("Card found!"));

  // Select one of the cards
  if (!mfrc522.PICC_ReadCardSerial()) {
    delay(50);
    return;
  }

  yield();
  read_card(&mfrc522);
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
    Serial.print(" - Size: "); Serial.println(f.size());
  }
  Serial.println(F("======================"));
}

bool erase_fs() {
  Dir dir = SPIFFS.openDir("/");
  bool allgood = true;
  while (dir.next()) {
    allgood &= SPIFFS.remove(dir.fileName());
  }
  update_cards_list();
  return allgood;
}

/*###########################################################*/
/*#################  CARDS IN FILESYSTEM  ###################*/
/*###########################################################*/

/* Save card to filesystem */
/*void save_card(String uid) {
  // Create file for storing card
  File card_file = SPIFFS.open(CARDS_DIR + uid, "w+");
  // Write first line, UID
  card_file.println("+UID: " + uid);

  // Show some details of the PICC
  Serial.println("Card UID: " + uid);
  Serial.print(F("PICC type: "));
  MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
  String picc_type = mfrc522.PICC_GetTypeName(piccType);
  Serial.println(picc_type);

  // Iterate over all blocks
  MFRC522::MIFARE_Key key;
  int picc_sectors = picc_blocks(picc_type) / 4;
  for (byte sector = 0; sector < picc_sectors; sector++) {
    // Try all keys until found the right one
    for (byte k = 0; k < NR_KNOWN_KEYS; k++) {
      // Copy the known key into the MIFARE_Key structure
      for (byte i = 0; i < MFRC522::MF_KEY_SIZE; i++) {
        key.keyByte[i] = knownKeys[k][i];
      }
      // Try key
      if (try_key_and_save_sector(&key, sector, &card_file)) break;
    }
  }
  update_cards_list();
  mfrc522.PICC_HaltA();       // Halt PICC
  mfrc522.PCD_StopCrypto1();  // Stop encryption on PCD

  // Check saved card
  card_file.close();
  Serial.println("SAVED CARD " + uid);
  //print_card(uid);
  }*/

void save_card_json(String uid) {
  // Create JSON for storing card
  String name = uid;
  while(check_card_name(name)) {
    name = uid + "_" + random(0, 999);
  }
  DynamicJsonBuffer json_buffer(2500);
  JsonObject& json_card = json_buffer.createObject();

  // Show some details of the PICC
  Serial.println("Card UID: " + uid);
  Serial.print(F("PICC type: "));
  MFRC522::PICC_Type piccType = mfrc522.PICC_GetType(mfrc522.uid.sak);
  String picc_type = mfrc522.PICC_GetTypeName(piccType);
  Serial.println(picc_type);

  // UID, PICC TYPE
  json_card["uid"] = uid;
  json_card["name"] = name;
  json_card["picc"] = picc_type;

  // Sectors
  JsonArray& json_sectors = json_card.createNestedArray("sectors");

  // Iterate over all sectors
  MFRC522::MIFARE_Key key;
  int picc_sectors = picc_blocks(picc_type) / 4;
  for (byte sector = 0; sector < picc_sectors; sector++) {

    // Create JsonObject for each sector
    JsonObject& json_sector = json_sectors.createNestedObject();
    // Try all keys until found the right one
    for (byte k = 0; k < NR_KNOWN_KEYS; k++) {
      // Copy the known key into the MIFARE_Key structure
      for (byte i = 0; i < MFRC522::MF_KEY_SIZE; i++) {
        key.keyByte[i] = knownKeys[k][i];
      }
      // Try key
      if (try_key_and_save_sector_json(&key, sector, &json_sector)) break;
    }
  }

  mfrc522.PICC_HaltA();       // Halt PICC
  mfrc522.PCD_StopCrypto1();  // Stop encryption on PCD

  save_card_json_to_file(&json_card);
  Serial.println("SAVED CARD " + name);
  update_cards_list();
  //print_card(uid);
}

/* Delete single card given UID
     Return: TRUE  if succesfully removed
             FALSE if not removed
*/
/*bool delete_card(String uid) {
  return SPIFFS.remove(CARDS_DIR + uid);
}*/

bool delete_card_name(String name) {
  Serial.println(" [+] Deleting card: " + name);
  bool res = SPIFFS.remove(CARDS_DIR + name);
  update_cards_list();
  return res;
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
  update_cards_list();
  return allgood;
}

/* Check if card exists in filesystem
     Return: TRUE  if Card exists.
             False if Card doesn't exist
*/
/*bool check_card_uid(String uid) {
  return SPIFFS.exists(CARDS_DIR + uid);
  }*/

bool check_card_name(String name) {
  bool ret = false;
  for (int i = 0; i < cards_count && !ret; i++) {
    ret = name.equals(cards_list[i]);
  }
  return ret;
}

/* Updates global variable cards_list */
void update_cards_list() {
  Dir dir = SPIFFS.openDir(CARDS_DIR);
  cards_count = 0;
  for (int i = 0; dir.next(); i++) {
    cards_list[i] = dir.fileName().substring(3);
    cards_count++;
    if (i >= MAX_CARDS_N) break;
  }

  print_fs();
}

/*###########################################################*/
/*#########################  CARDS ##########################*/
/*###########################################################*/

/* Reads card, decides wether to save or skip it
     Return: TRUE  if Card saved
             FALSE if Card not saved
*/
bool read_card(MFRC522* mfrc) {
  String uid = get_card_uid(mfrc522.uid.uidByte, mfrc522.uid.size);
  save_card_json(uid);
  return check_card_name(uid);
}

/* Read card content block by block */
/*boolean try_key_and_save_sector(MFRC522::MIFARE_Key *key, byte sector, File* file_ptr) {
  boolean result = true;
  struct Sector sector_str; // 0: Key, [1-3]: Content
  sector_str.number = sector;

  // Read sector
  byte block = sector * 4;
  for (int i = 0; i < 4; i++) {
    // Authenticate with A key
    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, block, key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) {
      Serial.print(F("PCD_Authenticate() failed: "));
      Serial.println(mfrc522.GetStatusCodeName(status));
      return false;
    }
    // Read block
    byte byteCount = sizeof(buffer);
    status = mfrc522.MIFARE_Read(block, buffer, &byteCount);
    if (status == MFRC522::STATUS_OK) {
      // Successful read
      sector_str.key = byte_array_to_hex_string((*key).keyByte, MFRC522::MF_KEY_SIZE);
      sector_str.blocks[i] = byte_array_to_hex_string(buffer, BLOCK_SIZE);
      result &= true;
    } else {
      // Error reading block
      Serial.print(F("MIFARE_Read() failed: "));
      Serial.println(mfrc522.GetStatusCodeName(status));
      result &= false;
    }
    block++;
  }
  //print_sector(&sector_str);
  save_sector(&sector_str, file_ptr);

  return result;
  }*/

boolean try_key_and_save_sector_json(MFRC522::MIFARE_Key *key, byte sector, JsonObject* sector_ptr) {
  boolean result = true;
  struct Sector sector_str; // 0: Key, [1-3]: Content
  sector_str.number = sector;

  // Create block array and add it to json
  JsonArray& json_blocks = sector_ptr->createNestedArray("blocks");

  // Read sector
  byte block = sector * 4;
  for (int i = 0; i < 4; i++) {
    // Authenticate with A key
    status = mfrc522.PCD_Authenticate(MFRC522::PICC_CMD_MF_AUTH_KEY_A, block, key, &(mfrc522.uid));
    if (status != MFRC522::STATUS_OK) {
      Serial.print(F("PCD_Authenticate() failed: "));
      Serial.println(mfrc522.GetStatusCodeName(status));
      return false;
    }
    // Read block
    byte byteCount = sizeof(buffer);
    status = mfrc522.MIFARE_Read(block, buffer, &byteCount);
    if (status == MFRC522::STATUS_OK) {
      // Successful read
      json_blocks.add(byte_array_to_hex_string(buffer, BLOCK_SIZE));
      result &= true;
    } else {
      // Error reading block
      Serial.print(F("MIFARE_Read() failed: "));
      Serial.println(mfrc522.GetStatusCodeName(status));
      result &= false;
    }
    block++;
  }

  // Save key if successful
  if (result) {
    (*sector_ptr)["key"] = byte_array_to_hex_string((*key).keyByte, MFRC522::MF_KEY_SIZE);
  }

  return result;
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
int picc_blocks(String picc) {
  if (picc.equals(MIFARE_1K)) {
    return 64;
  }
  else if (picc.equals(MIFARE_4K)) {
    return 256;
  }
  else {
    return 64;
  }
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

/* Dump byte array to String */
String byte_array_to_hex_string(byte *buffer, byte bufferSize) {
  String ret = "";
  for (byte i = 0; i < bufferSize; i++) {
    ret.concat(buffer[i] < 0x10 ? "0" : "");
    ret.concat(String(buffer[i], HEX));
  }
  return ret;
}

/* Dump byte array to File - FALSEEEEEEEEEEEEEE, yet */
void dump_byte_array_file(byte *buffer, byte bufferSize) {
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? "0" : "");
    Serial.print(buffer[i], HEX);
  }
}

/* Print Sector struct to Serial */
void print_sector(Sector* sector) {
  // Info
  Serial.print("+Sector ");
  Serial.print(sector->number, DEC);
  Serial.println(": " + sector->key);

  // Blocks
  for (int i = 0; i < SECTOR_SIZE; i++) {
    Serial.println(sector->blocks[i]);
  }
}

/* Save sector to file. Appends sector. */
/*void save_sector(Sector* sector, File* f_ptr) {
  // Info
  f_ptr->print("+Sector ");
  f_ptr->println(sector->number, DEC);
  //f_ptr->println(": " + sector->key);
  fix_a_key(sector);

  // Blocks
  for (int i = 0; i < SECTOR_SIZE; i++) {
    f_ptr->println(sector->blocks[i]);
  }
  }*/

void save_card_json_to_file(JsonObject* json_card) {
  int sectors = (*json_card)["sectors"].size();
  String name = (*json_card)["name"];
  File json_card_file = SPIFFS.open(CARDS_DIR + name, "w+");
  //File json_card_file = SPIFFS.open(CARDS_DIR+uid, "w+");
  String json_card_string;
  json_card->printTo(json_card_string);

  Serial.println(json_card_string);
  json_card_file.print(json_card_string);
  json_card_file.close();
}

void update_card_json_file(JsonObject* json_card, String old_name) {
  String name = (*json_card)["name"];
  if (old_name.equals("")) {
    SPIFFS.remove(CARDS_DIR + old_name);
  }
  save_card_json_to_file(json_card);  
}

/* Places A key into 4th block. Condition flags
    might not allow you to read A key so you get
    zeroes.
*/
void fix_a_key(Sector* sector) {
  String control_block = sector->blocks[3];
  if (!control_block.startsWith(sector->key)) {
    sector->blocks[3] = sector->key + sector->blocks[3].substring(12);
  }
}

void print_card(String uid) {
  File card = SPIFFS.open(CARDS_DIR + uid, "r");
  print_file_lines(&card);
}

void print_file_lines(File* file) {
  while (file->available()) {
    //Lets read line by line from the file
    String line = file->readStringUntil('n');
    Serial.println(line);
  }
}

