#include <WiFi.h>
#include <WebServer.h>
#include <Firebase_ESP_Client.h>
#include "mbedtls/sha256.h"
#include <esp_system.h> // esp_fill_random

// ---------- CONFIG ----------
#define LED_PIN   25
#define SSID      "T0fu"
#define PASSWORD  "logic123"
#define HOSTNAME  "abuga"

#define API_KEY   "YOUR_FIREBASE_API_KEY"
#define DB_URL    "https://your-project-id.firebaseio.com/"  // trailing slash required

const char* MASTER_PASSWORD_HASH = "39b57dae2ed0bc654c15f9d36a1bd4ac280d059340b29a0a4fc0d87768e3e21b";

// ---------- Firebase objects ----------
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig configFirebase;

// ---------- Web server ----------
WebServer server(80);

// ---------- Utilities ----------
String bytesToHex(const uint8_t *buf, size_t len) {
  String s; s.reserve(len*2);
  const char hex[] = "0123456789abcdef";
  for (size_t i = 0; i < len; ++i) {
    s += hex[(buf[i] >> 4) & 0xF];
    s += hex[buf[i] & 0xF];
  }
  return s;
}

bool hexToBytes(const String &hex, uint8_t *out, size_t outLen) {
  if (hex.length() != (int)outLen * 2) return false;
  auto val = [](char c)->int {
    if (c >= '0' && c <= '9') return c - '0';
    if (c >= 'a' && c <= 'f') return 10 + (c - 'a');
    if (c >= 'A' && c <= 'F') return 10 + (c - 'A');
    return -1;
  };
  for (size_t i = 0; i < outLen; ++i) {
    int hi = val(hex[2*i]);
    int lo = val(hex[2*i+1]);
    if (hi < 0 || lo < 0) return false;
    out[i] = (uint8_t)((hi << 4) | lo);
  }
  return true;
}

String sha256Hex(const String &input) {
  unsigned char out[32];
  mbedtls_sha256_context ctx;
  mbedtls_sha256_init(&ctx);
  mbedtls_sha256_starts_ret(&ctx, 0);
  mbedtls_sha256_update_ret(&ctx, (const unsigned char*)input.c_str(), input.length());
  mbedtls_sha256_finish_ret(&ctx, out);
  mbedtls_sha256_free(&ctx);
  String h = bytesToHex(out, 32);
  memset(out, 0, sizeof(out));
  return h;
}

String genSaltHex(size_t lenBytes = 16) {
  uint8_t s[lenBytes];
  esp_fill_random(s, lenBytes);
  String hex = bytesToHex(s, lenBytes);
  memset(s, 0, lenBytes);
  return hex;
}

// Compute SHA256(saltBytes || password) -> hex
bool hashWithSaltHex(const String &saltHex, const String &password, String &outHashHex) {
  size_t saltLen = saltHex.length() / 2;
  uint8_t *salt = (uint8_t*)malloc(saltLen);
  if (!salt) return false;
  if (!hexToBytes(saltHex, salt, saltLen)) { free(salt); return false; }
  size_t pwdLen = password.length();
  size_t bufLen = saltLen + pwdLen;
  uint8_t *buf = (uint8_t*)malloc(bufLen);
  if (!buf) { free(salt); return false; }
  memcpy(buf, salt, saltLen);
  memcpy(buf + saltLen, password.c_str(), pwdLen);

  unsigned char out[32];
  mbedtls_sha256_context ctx;
  mbedtls_sha256_init(&ctx);
  mbedtls_sha256_starts_ret(&ctx, 0);
  mbedtls_sha256_update_ret(&ctx, buf, bufLen);
  mbedtls_sha256_finish_ret(&ctx, out);
  mbedtls_sha256_free(&ctx);

  outHashHex = bytesToHex(out, 32);

  memset(out, 0, sizeof(out));
  memset(buf, 0, bufLen); free(buf);
  memset(salt, 0, saltLen); free(salt);
  return true;
}

// ---------- Firebase helpers ----------
void firebaseInit() {
  configFirebase.api_key = API_KEY;
  configFirebase.database_url = DB_URL;
  Firebase.begin(&configFirebase, &auth);
  Firebase.reconnectWiFi(true);
}

bool storePasswordToFirebase(const String &name, const String &saltHex, const String &hashHex) {
  String pBase = "/passwords/" + name;
  String pSalt = pBase + "/salt";
  String pHash = pBase + "/hash";
  if (!Firebase.RTDB.setString(&fbdo, pSalt.c_str(), saltHex)) {
    Serial.println("Firebase write salt error: " + fbdo.errorReason());
    return false;
  }
  if (!Firebase.RTDB.setString(&fbdo, pHash.c_str(), hashHex)) {
    Serial.println("Firebase write hash error: " + fbdo.errorReason());
    return false;
  }
  return true;
}

bool getPasswordFromFirebase(const String &name, String &outSaltHex, String &outHashHex) {
  String pBase = "/passwords/" + name;
  String pSalt = pBase + "/salt";
  String pHash = pBase + "/hash";
  if (!Firebase.RTDB.getString(&fbdo, pSalt.c_str())) {
    Serial.println("Firebase read salt error: " + fbdo.errorReason());
    return false;
  }
  outSaltHex = fbdo.stringData();
  if (!Firebase.RTDB.getString(&fbdo, pHash.c_str())) {
    Serial.println("Firebase read hash error: " + fbdo.errorReason());
    return false;
  }
  outHashHex = fbdo.stringData();
  return true;
}

// ---------- WiFi ----------
void connectWiFi() {
  Serial.print("Connecting to WiFi");
  WiFi.setHostname(HOSTNAME);
  WiFi.begin(SSID, PASSWORD);
  int tries = 0;
  while (WiFi.status() != WL_CONNECTED && tries < 60) {
    delay(500);
    Serial.print(".");
    tries++;
  }
  Serial.println();
  if (WiFi.status() == WL_CONNECTED) {
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
    digitalWrite(LED_PIN, HIGH);
  } else {
    Serial.println("WiFi connect failed");
  }
}

// ---------- Web UI pages ----------
const char indexPage[] PROGMEM = R"rawliteral(
<!doctype html>
<html>
<head><meta charset="utf-8"><title>ESP Password Manager</title></head>
<body>
<h2>ESP Password Manager (Web UI)</h2>
<ul>
<li><a href="/create">Create Password</a></li>
<li><a href="/verify">Verify Password</a></li>
<li><a href="/quit">Quit (reboot)</a></li>
</ul>
</body>
</html>
)rawliteral";

const char createPage[] PROGMEM = R"rawliteral(
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Create</title></head>
<body>
<h3>Create Password</h3>
<form method="POST" action="/create">
<label>Entry Name (id): <input name="name" required></label><br>
<label>Password: <input name="password" required></label><br>
<button type="submit">Store</button>
</form>
<p><a href="/">Home</a></p>
</body>
</html>
)rawliteral";

const char verifyPage[] PROGMEM = R"rawliteral(
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Verify</title></head>
<body>
<h3>Verify Password</h3>
<form method="POST" action="/verify">
<label>Entry Name (id): <input name="name" required></label><br>
<label>Auth SHA256 of "YUTH": <input name="authHex" required></label><br>
<label>Password to verify: <input name="password" required></label><br>
<button type="submit">Verify</button>
</form>
<p><a href="/">Home</a></p>
</body>
</html>
)rawliteral";

const char quitPage[] PROGMEM = R"rawliteral(
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Quit</title></head>
<body>
<h3>Rebooting ESP...</h3>
<p>If the device does not reboot, unplug and replug it.</p>
</body>
</html>
)rawliteral";

// ---------- Web handlers ----------
void handleRoot() { server.send_P(200, "text/html", indexPage); }

void handleCreateGet() { server.send_P(200, "text/html", createPage); }

void handleCreatePost() {
  // read form values
  String name = server.arg("name");
  String pwd  = server.arg("password");
  if (name.length() == 0 || pwd.length() == 0) {
    server.send(400, "text/plain", "Missing fields");
    return;
  }
  String salt = genSaltHex(16);
  String hash;
  if (!hashWithSaltHex(salt, pwd, hash)) {
    server.send(500, "text/plain", "Hash error");
    return;
  }
  bool ok = storePasswordToFirebase(name, salt, hash);
  if (ok) server.send(200, "text/html", "<p>Stored successfully.</p><p><a href='/'>Home</a></p>");
  else server.send(500, "text/plain", "Firebase store failed");
}

void handleVerifyGet() { server.send_P(200, "text/html", verifyPage); }

void handleVerifyPost() {
  String name = server.arg("name");
  String authHex = server.arg("authHex");
  String pwd = server.arg("password");
  if (name.length() == 0 || authHex.length() == 0 || pwd.length() == 0) {
    server.send(400, "text/plain", "Missing fields");
    return;
  }
  // normalize and compare auth hash
  String expected = String(MASTER_PASSWORD_HASH);
  if (!authHex.equalsIgnoreCase(expected)) {
    server.send(403, "text/plain", "Authorization hash invalid");
    return;
  }
  String saltHex, storedHash;
  if (!getPasswordFromFirebase(name, saltHex, storedHash)) {
    server.send(404, "text/plain", "Entry not found");
    return;
  }
  String tryHash;
  if (!hashWithSaltHex(saltHex, pwd, tryHash)) {
    server.send(500, "text/plain", "Hash error");
    return;
  }
  if (tryHash.equalsIgnoreCase(storedHash)) {
    server.send(200, "text/html", "<p>Password VERIFIED.</p><p><a href='/'>Home</a></p>");
  } else {
    server.send(200, "text/html", "<p>Password INVALID.</p><p><a href='/'>Home</a></p>");
  }
}

void handleQuit() {
  server.send_P(200, "text/html", quitPage);
  delay(500);
  ESP.restart();
}

void startWebServer() {
  server.on("/", HTTP_GET, handleRoot);
  server.on("/create", HTTP_GET, handleCreateGet);
  server.on("/create", HTTP_POST, handleCreatePost);
  server.on("/verify", HTTP_GET, handleVerifyGet);
  server.on("/verify", HTTP_POST, handleVerifyPost);
  server.on("/quit", HTTP_GET, handleQuit);
  server.begin();
  Serial.println("Web server started. Visit http://" + WiFi.localIP().toString() + "/");
}

// ---------- Setup / Loop ----------
void setup() {
  Serial.begin(115200);
  delay(200);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);

  connectWiFi();
  firebaseInit();

  // optional: also print master hash for convenience
  Serial.println("Master SHA256(\"YUTH\") = " + String(MASTER_PASSWORD_HASH));
  startWebServer();
}

void loop() {
  server.handleClient();
}
