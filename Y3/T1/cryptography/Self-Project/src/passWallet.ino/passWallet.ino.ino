#include <WiFi.h>
#include <WebServer.h>

// WIFI Configuration 
const char* ssid = "T0fu";
const char* password = "logic1234";
const char* hostname = "obuwga"; 
const int CONNECTED_LED = 26; 

WebServer server(80);

void handleRoot() {
  String html = "<html><head><meta http-equiv='refresh' content='5'>";
  html += "<title>T0fu</title></head><body>";
  html += "<h1>I AM " + String(hostname) + "</h1>";
  html += "</body></html>";

  server.send(200, "text/html", html);
}

// Connect Wifi
void wifiConnect() {
  pinMode(CONNECTED_LED, OUTPUT);
  digitalWrite(CONNECTED_LED, LOW);
  WiFi.setHostname(hostname); 
  WiFi.mode(WIFI_STA);
  
  WiFi.begin(ssid, password); 
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.print("\nWiFi Connected IP : ");
  Serial.println(WiFi.localIP());
  digitalWrite(CONNECTED_LED, HIGH);
}

void setup() {
  Serial.begin(115200);
  randomSeed(analogRead(0));

  wifiConnect(); 

  server.on("/", handleRoot); 
  server.begin();
  Serial.println("HTTP Server started. Access it at:");
  Serial.print("http://");
  Serial.println(WiFi.localIP());
}

void loop() {

  server.handleClient(); 

  if (WiFi.status() != WL_CONNECTED) {
      Serial.println("Connection lost. Resetting.");
      ESP.restart(); 
  }
}