#include <WiFi.h>               // kept only in case you re-enable Firebase later
#include "mbedtls/base64.h"
#include "mbedtls/gcm.h"
#include "esp_system.h"         // for esp_fill_random

// Crypto constants
#define KEY_SIZE_MAX 32        // max key bytes we'll allow for buffer
#define IV_SIZE 12
#define TAG_SIZE 16

// LED pins: GPIO25 = success light, GPIO32 = fail light
const int PIN_OK = 25;
const int PIN_FAIL = 32;

// Simple message to test
const char *TEST_MESSAGE = "Hello, this is a secret message from Arduino!";

// Helper: Secure Random Generator
void get_random_bytes(uint8_t *buf, size_t len) {
  esp_fill_random(buf, len);
}

// Performs full encrypt+decrypt test using a key of key_len bytes.
// Returns true if decrypted plaintext exactly matches original.
bool encrypt_decrypt_test(size_t key_len) {
  Serial.print("Test with key length (bytes): ");
  Serial.println(key_len);

  if (key_len == 0 || key_len > KEY_SIZE_MAX) {
    Serial.println("Invalid key length requested.");
    return false;
  }

  const size_t plain_len = strlen(TEST_MESSAGE);
  const uint8_t *plain_in = (const uint8_t*) TEST_MESSAGE;

  // allocate key/iv
  uint8_t key[KEY_SIZE_MAX] = {0};
  uint8_t iv[IV_SIZE] = {0};

  get_random_bytes(key, key_len);   // fill only key_len bytes
  get_random_bytes(iv, IV_SIZE);

  // prepare packet buffer: IV + TAG + CIPHERTEXT
  size_t total_len = IV_SIZE + TAG_SIZE + plain_len;
  uint8_t *packet = (uint8_t*) malloc(total_len);
  if (!packet) {
    Serial.println("malloc packet failed");
    return false;
  }
  uint8_t *iv_ptr = packet;
  uint8_t *tag_ptr = packet + IV_SIZE;
  uint8_t *cipher_ptr = packet + IV_SIZE + TAG_SIZE;

  // copy IV into packet
  memcpy(iv_ptr, iv, IV_SIZE);

  // --- ENCRYPT ---
  mbedtls_gcm_context gcm;
  mbedtls_gcm_init(&gcm);

  int ret = mbedtls_gcm_setkey(&gcm, MBEDTLS_CIPHER_ID_AES, key, key_len * 8);
  if (ret != 0) {
    Serial.print("mbedtls_gcm_setkey failed, ret=");
    Serial.println(ret);
    mbedtls_gcm_free(&gcm);
    free(packet);
    return false;
  }

  ret = mbedtls_gcm_crypt_and_tag(&gcm, MBEDTLS_GCM_ENCRYPT,
                                  plain_len,
                                  iv, IV_SIZE,
                                  NULL, 0,
                                  plain_in, cipher_ptr,
                                  TAG_SIZE, tag_ptr);
  if (ret != 0) {
    Serial.print("mbedtls_gcm_crypt_and_tag failed, ret=");
    Serial.println(ret);
    mbedtls_gcm_free(&gcm);
    free(packet);
    return false;
  }
  mbedtls_gcm_free(&gcm);

  // --- DECRYPT ---
  // slices from packet
  uint8_t *iv_from_packet = iv_ptr;
  uint8_t *tag_from_packet = tag_ptr;
  uint8_t *cipher_from_packet = cipher_ptr;
  size_t cipher_len = plain_len;

  uint8_t *plain_out = (uint8_t*) malloc(cipher_len + 1);
  if (!plain_out) {
    Serial.println("malloc plain_out failed");
    free(packet);
    return false;
  }

  mbedtls_gcm_init(&gcm);
  ret = mbedtls_gcm_setkey(&gcm, MBEDTLS_CIPHER_ID_AES, key, key_len * 8);
  if (ret != 0) {
    Serial.print("mbedtls_gcm_setkey (decrypt) failed, ret=");
    Serial.println(ret);
    mbedtls_gcm_free(&gcm);
    free(packet);
    free(plain_out);
    return false;
  }

  ret = mbedtls_gcm_auth_decrypt(&gcm, cipher_len,
                                iv_from_packet, IV_SIZE,
                                NULL, 0,
                                tag_from_packet, TAG_SIZE,
                                cipher_from_packet, plain_out);
  mbedtls_gcm_free(&gcm);

  bool ok = false;
  if (ret == 0) {
    // Null terminate and compare
    plain_out[cipher_len] = '\0';
    Serial.print("Decrypted: ");
    Serial.println((char*)plain_out);
    if (strcmp((char*)plain_out, TEST_MESSAGE) == 0) {
      ok = true;
      Serial.println("Plaintext matches original -> OK");
    } else {
      Serial.println("Plaintext does NOT match original -> FAIL");
    }
  } else {
    Serial.print("Decryption failed (auth check), ret=");
    Serial.println(ret);
  }

  free(packet);
  free(plain_out);
  return ok;
}

void indicate_result(bool ok) {
  if (ok) {
    digitalWrite(PIN_OK, HIGH);
    digitalWrite(PIN_FAIL, LOW);
  } else {
    digitalWrite(PIN_OK, LOW);
    digitalWrite(PIN_FAIL, HIGH);
  }
}

void setup() {
  Serial.begin(115200);
  while (!Serial) { delay(10); } // wait for Serial on some setups

  // Initialize indicator pins
  pinMode(PIN_OK, OUTPUT);
  pinMode(PIN_FAIL, OUTPUT);
  digitalWrite(PIN_OK, LOW);
  digitalWrite(PIN_FAIL, LOW);

  Serial.println("=== Local encryption self-test (no Firebase) ===");

  // Run two example tests (you can change or remove as needed)
  // 1) Test with 25 bytes (likely invalid AES key size -> usually FAIL)
  bool ok25 = encrypt_decrypt_test(25);
  Serial.print("Result for key 25: ");
  Serial.println(ok25 ? "SUCCESS" : "FAIL");
  indicate_result(ok25);
  delay(4000); // leave indicator on for a bit

  // Clear indicators
  digitalWrite(PIN_OK, LOW);
  digitalWrite(PIN_FAIL, LOW);
  delay(500);

  // 2) Test with 32 bytes (256-bit AES, expected to SUCCEED)
  bool ok32 = encrypt_decrypt_test(32);
  Serial.print("Result for key 32: ");
  Serial.println(ok32 ? "SUCCESS" : "FAIL");
  indicate_result(ok32);
  // Keep final result visible
}

void loop() {
  // Nothing else to do — tests already run in setup.
  delay(1000);
}
