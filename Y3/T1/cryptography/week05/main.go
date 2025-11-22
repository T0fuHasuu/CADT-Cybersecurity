package main

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"log"

	"work/tools"
)

func aesECB(plaintext []byte, block cipher.Block) error {
	bs := block.BlockSize()

	// Pad
	plainPadded := tools.Pkcs7Pad(plaintext, bs)

	// Encrypt (ECB)
	ciphertext := make([]byte, len(plainPadded))
	for start := 0; start < len(plainPadded); start += bs {
		end := start + bs
		block.Encrypt(ciphertext[start:end], plainPadded[start:end])
	}
	fmt.Println("ECB CipherText (Hex):", hex.EncodeToString(ciphertext))

	// Decrypt (ECB)
	decryptedPadded := make([]byte, len(ciphertext))
	for start := 0; start < len(ciphertext); start += bs {
		end := start + bs
		block.Decrypt(decryptedPadded[start:end], ciphertext[start:end])
	}

	decrypted, err := tools.Pkcs7Unpad(decryptedPadded)
	if err != nil {
		return fmt.Errorf("ECB unpad error: %w", err)
	}
	fmt.Println("ECB Decrypted Text:", string(decrypted))

	return nil
}

func aesCBC(plaintext []byte, block cipher.Block, iv []byte) error {
	bs := block.BlockSize()

	// Pad plaintext
	plainPadded := tools.Pkcs7Pad(plaintext, bs)

	// Encrypt (CBC)
	ciphertext := make([]byte, len(plainPadded))
	modeEnc := cipher.NewCBCEncrypter(block, iv)
	modeEnc.CryptBlocks(ciphertext, plainPadded)
	fmt.Println("CBC CipherText (Hex):", hex.EncodeToString(ciphertext))

	// Decrypt (CBC)
	decryptedPadded := make([]byte, len(ciphertext))
	modeDec := cipher.NewCBCDecrypter(block, iv)
	modeDec.CryptBlocks(decryptedPadded, ciphertext)

	decrypted, err := tools.Pkcs7Unpad(decryptedPadded)
	if err != nil {
		return fmt.Errorf("CBC unpad error: %w", err)
	}
	fmt.Println("CBC Decrypted Text:", string(decrypted))
	return nil
}


func aesCFB(plaintext []byte, block cipher.Block, iv []byte) error {
	ciphertext := make([]byte, len(plaintext))
	// Create a new CFB encrypter stream
	streamEnc := cipher.NewCFBEncrypter(block, iv)
	// Apply the keystream to the plaintext
	streamEnc.XORKeyStream(ciphertext, plaintext)
	fmt.Println("Ciphertext (hex):", hex.EncodeToString(ciphertext))
	
	// Decrypt
	decrypted := make([]byte, len(ciphertext))
	// Create a new CFB decrypter stream
	streamDec := cipher.NewCFBDecrypter(block, iv)
	// Apply the *same* keystream to the *ciphertext*Practical AES encryption in Go12
	streamDec.XORKeyStream(decrypted, ciphertext)
	fmt.Println("Decrypted:", string(decrypted))
	return nil
}

func OFB(plaintext []byte, block cipher.Block, iv []byte) error {

	ciphertext := make([]byte, len(plaintext))
	// Create the OFB stream
	streamEnc := cipher.NewOFB(block, iv)
	// XOR the plaintext with the keystream
	streamEnc.XORKeyStream(ciphertext, plaintext)
	fmt.Println("Ciphertext (hex):", hex.EncodeToString(ciphertext))

	decrypted := make([]byte, len(ciphertext))
	// Create a new OFB stream with the same IV
	streamDec := cipher.NewOFB(block, iv)
	// XOR the ciphertext with the keystream
	streamDec.XORKeyStream(decrypted, ciphertext)
	fmt.Println("Decrypted:", string(decrypted))
	return nil
}

func aesCTR(plaintext []byte, block cipher.Block, nonce []byte) error {
	ciphertext := make([]byte, len(plaintext))
	// Create a new CTR stream
	streamEnc := cipher.NewCTR(block, nonce)
	// XOR the keystream with the plaintext
	streamEnc.XORKeyStream(ciphertext, plaintext)
	fmt.Println("Ciphertext (hex):", hex.EncodeToString(ciphertext))

	decrypted := make([]byte, len(ciphertext))
	// Create a new CTR stream with the *same* nonce
	streamDec := cipher.NewCTR(block, nonce)
	// XOR the keystream with the ciphertext
	streamDec.XORKeyStream(decrypted, ciphertext)
	fmt.Println("Decrypted:", string(decrypted))
	return nil
}

func main() {
	key := []byte("thisis32bitlongpassphraseimusing") // 32 bytes => AES-256
	plaintext := []byte("YELLOW SUBMARINE")
	fmt.Println("PlainText:", string(plaintext))
	
	block, err := aes.NewCipher(key)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("----- ECB Encryption/Decryption -----")
	if err := aesECB(plaintext, block); err != nil {
		log.Fatal(err)
	}

	bs := block.BlockSize()
	fmt.Println("----- CBC Encryption/Decryption -----")
	iv := make([]byte, bs)
	if _, err := rand.Read(iv); err != nil {
		log.Fatal(err)
	}
	fmt.Println("CBC IV (Hex):", hex.EncodeToString(iv))

	if err := aesCBC(plaintext, block, iv); err != nil {
		log.Fatal(err)
	}

	fmt.Println("----- CFB Encryption/Decryption -----")
	if err := aesCFB(plaintext, block, iv); err != nil {
		log.Fatal(err)
	}

	fmt.Println("----- OFB Encryption/Decryption -----")
	if err := OFB(plaintext, block, iv); err != nil {
		log.Fatal(err)
	}

	nonce := make([]byte, aes.BlockSize)
	if _, err := rand.Read(nonce); err != nil {
	log.Fatal(err)
	}
	fmt.Println("Nonce (hex):", hex.EncodeToString(nonce))
	fmt.Println("----- CTR Encryption/Decryption -----")
	if err := aesCTR(plaintext, block, nonce); err != nil {
		log.Fatal(err)
	}


	gcm, err := cipher.NewGCM(block)
	if err != nil {
		log.Fatal("cipher.NewGCM:", err)
	}

	// Nonce size (usually 12 bytes for GCM)
	nonceSize := gcm.NonceSize()
	nonce = make([]byte, nonceSize)
	if _, err := rand.Read(nonce); err != nil {
		log.Fatal("rand.Read nonce:", err)
	}
	fmt.Println("Nonce (hex):", hex.EncodeToString(nonce))

	// Encrypt (Seal) — output = ciphertext || authTag
	ciphertext := gcm.Seal(nil, nonce, plaintext, nil)
	fmt.Println("Ciphertext+Tag (hex):", hex.EncodeToString(ciphertext))

	// Decrypt (Open) — will verify auth tag
	decrypted, err := gcm.Open(nil, nonce, ciphertext, nil)
	if err != nil {
		log.Fatal("Decryption failed (tag mismatch or other):", err)
	}
	fmt.Println("Decrypted:", string(decrypted))
}
