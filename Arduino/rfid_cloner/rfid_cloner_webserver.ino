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
  // Update list if requested
  if (server.hasArg("update") && server.arg("update").equals("yes")) {
    update_cards_list();
  }

  // Create JSON and send
  StaticJsonBuffer<200> jsonBuffer;

  // Number of cards
  JsonObject& jObj = jsonBuffer.createObject();
  jObj["number"] = cards_count;

  // Array of UIDs
  JsonArray& uids = jObj.createNestedArray("uids");
  for (int i = 0; i < cards_count; i++) {
    uids.add(cards_list[i].c_str());
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
    while (card.available()) {
      //Lets read line by line from the file
      card_string += card.readStringUntil('\n') + '\n';
    }
    server.send(200, "text/plain", card_string);
  } else {
    // Respond as not found
    server.send(404);
  }
}

void handle_update_card() {
  String name = server.arg("name");
  String old_name = server.arg("oldname");
  if(server.hasArg("name")) {
    DynamicJsonBuffer buffer(2500);
    JsonObject& json_card = buffer.parseObject(server.arg("card"));
    update_card_json_file(&json_card, server.hasArg("oldname") ? server.arg("oldname") : "");
    server.send(200);
  } else {
    // Malformed
    server.send(400);
  }
}

void handle_delete_card() {
  String name = server.arg("name");
  if (!server.hasArg("name")) {
     server.send(400);
  } else if (check_card_name(name)) {
    if(delete_card_name(name)) {
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

