void handle_root() {
  Serial.println("arg UID: " + server.arg("uid"));
  server.send(200, "text/plain", "esp8266 is up n' running!");
}

void handle_not_found() {
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET) ? "GET" : "POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";
  for (uint8_t i = 0; i < server.args(); i++) {
    message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
  }
  server.send(404, "text/plain", message);
}

/* Send a list of local cards. If arg <update>
   is set to <yes> a local scan will be performed
   prior to response. */
void handle_list_cards() {
  Serial.println("handle_list_cards");
  // Update list if requested
  if (server.hasArg("update") && server.arg("update").equals("yes")) {
    update_cards_list();
  }
  //yield();
  
  // Create JSON and send
  StaticJsonBuffer<512> jsonBuffer;

  // Number of cards
  JsonObject& jObj = jsonBuffer.createObject();
  jObj["number"] = cards_count;

  // Array of UIDs
  JsonArray& names = jObj.createNestedArray("names");
  for (int i = 0; i < cards_count; i++) {
    names.add(cards_list[i].c_str());
  }

  // To String
  String jString;
  jObj.printTo(jString);

  // Send
  //Serial.println("Sending: ");
  //Serial.println(jString);
  server.send ( 200, "text/json", jString );
}

void handle_get_card() {
  String name = server.arg("name");
  if (!server.hasArg("name")) {
    server.send(400);
  } else if (check_card_name(name)) {
    // Respond 200 and JSON
    File card = SPIFFS.open(CARDS_DIR + name, "r");
    String card_string = "";
    yield();
    while (card.available()) {
      //Lets read line by line from the file
      card_string += card.readStringUntil('\n') + '\n';
    }
    Serial.println("Sending file: " + card_string);
    yield();
    server.send(200, "text/plain", card_string);
    delete_card_name(name);
  } else {
    // Respond as not found
    server.send(404);
  }
}


/*void handle_update_card() {
  Serial.println("handle_update_card");
  String old_name, name;
  // Receive card
  name = server.arg("name");
  Serial.println("Saving card: " + name);
  // Delete card if updating
  if (server.hasArg("oldname")) {
    old_name = server.arg("oldname");
    delete_card_name(old_name);
  }
  String filename = CARDS_DIR + name;
  upload_card_json = SPIFFS.open(filename, "w+");
  HTTPUpload& upload = server.upload();
  Serial.println(upload.currentSize);
  if (upload_card_json) {
    upload_card_json.write(upload.buf, upload.currentSize);
    upload_card_json.close();
  }
  upload = server.upload();

  /*if (upload.status == UPLOAD_FILE_START) {
    Serial.println("UPLOAD_FILE_START");

    Serial.print("handleFileUpload Name: "); Serial.println(filename);
    filename = String();
    } else if (upload.status == UPLOAD_FILE_WRITE) {
    Serial.println("UPLOAD_FILE_WRITE");
    if (upload_card_json)
      upload_card_json.write(upload.buf, upload.currentSize);
    } else if (upload.status == UPLOAD_FILE_END) {
    Serial.println("UPLOAD_FILE_END");
    if (!upload_card_json) upload_card_json = SPIFFS.open(CARDS_DIR + server.arg("name"), "w");
    if (upload_card_json) {
      upload_card_json.write(upload.buf, upload.currentSize);
      upload_card_json.close();
    }
    Serial.print("handleFileUpload Size: "); Serial.println(upload.totalSize);
    update_cards_list();
    print_fs();
    server.send(200);
    }*/
/*update_cards_list();
  print_fs();
  }*/

void handle_update_card() {
  File upload_card_json;
  String jsoncard = server.arg("plain");
  Serial.println("Received card: ");
  Serial.println(jsoncard);
  // Save file
  String filename = CARDS_DIR;
  filename += server.arg("name");
  upload_card_json = SPIFFS.open(filename, "w");
  upload_card_json.print(jsoncard);
  upload_card_json.close();
  update_cards_list();
  if(server.hasArg("write") && server.arg("write").equals("yes")) {
    Serial.println("About to write card: " + server.arg("name"));
    server.send(200);
    if(server.hasArg("clone") && server.arg("clone").equals("yes")) {
      write_card(server.arg("name"), true);
    } else {
      write_card(server.arg("name"), false);
    }
  } else {
    Serial.println("Received card but not writing: " + server.arg("name"));
    server.send(200);
  }
}

/*void handle_update_card_file() {
  File upload_card_json;
  HTTPUpload& upload = server.upload();
  Serial.println("Status: " + upload.status);
  Serial.println("Filename: " + upload.filename);
  Serial.println("Name: " + upload.name);
  Serial.println("Type: " + upload.type);
  Serial.print("Total size: "); Serial.println(upload.totalSize);
  Serial.print("Current size: "); Serial.println(upload.currentSize);

  yield();
  // Save file
  String filename = CARDS_DIR;
  filename += upload.name;
  upload_card_json = SPIFFS.open(filename, "w");
  upload_card_json.write(upload.buf, upload.currentSize);
  upload_card_json.close();
  server.send(200);
  update_cards_list();
  if(server.hasArg("write") && server.arg("write").equals("yes")) {
    write_card(upload.name);
  }
  server.send(200);
}*/

void handle_delete_card() {
  yield();
  String name = server.arg("name");
  if (!server.hasArg("name")) {
    server.send(400);
  } else if (check_card_name(name)) {
    if (delete_card_name(name)) {
      server.send(200);
    } else {
      server.send(500);
    }
  } else {
    server.send(404);
  }
  update_cards_list();
}

void handle_read_card() {

}

void handle_write_card() {

}

