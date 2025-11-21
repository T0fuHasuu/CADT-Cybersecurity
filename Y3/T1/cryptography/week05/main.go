package main

import (
	"bytes"
	"crypto/aes"
	"crypto/rand"
	"crypto/cipher"
	"encoding/hex"
	"fmt"
	"log"
)

func pkcs7Pad(data []byte, blockSize int) []byte{

	// Calc How Much Pad Needed
	padding := blockSize - len(data)%blockSize

	// Create Pad Bytes
	padText := bytes.Repeat([]byte{byte(padding)}, padding)

	// Append Pad to Data
	return append(data, padText...)
}

func pkcs7Unpad(data []byte) ([]byte, error) {
	
	if len(data) == 0 {
		return nil, fmt.Errorf("data is empty")
	}

	// Get Padding Length With Last Byte
	padding := int(data[len(data)-1])

	if padding == 0 || padding > len(data) {
		return nil, fmt.Errorf("Invalid Padding Size")
	}

	// Return Slice Without Padding
	return data[:len(data)-padding], nil
}



func main() {
	key := []byte("thisis32bitlongpassphraseimusing")
	plaintext := []byte("YELLOW SUBMARINE")
	fmt.Println("PlainText", string(plaintext))

	// Initialize AES Cipher
	block, err := aes.NewCipher(key)
	if err != nil {
		log.Fatal(err)
	}
	blockSize := block.BlockSize()

	// Pad Encrypt 	
	plainPadded := pkcs7Pad(plaintext, blockSize)
	ciphertext := make([]byte, len(plainPadded))

	for start := 0; start < len(plainPadded); start += blockSize {
		end := start + blockSize
		block.Encrypt(ciphertext[start:end], plainPadded[start:end])
	}
	fmt.Println("CipherText (Hex):", hex.EncodeToString(ciphertext))

	// Decrypt
	decryptedPadded := make([]byte, len(ciphertext))
	for start := 0; start < len(ciphertext); start += blockSize {
		end := start + blockSize
		block.Decrypt(decryptedPadded[start:end], ciphertext[start:end])
	}
	decrypted, err := pkcs7Unpad(decryptedPadded)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("Decrypted Text:", string(decrypted))


	iv := make([]byte, blockSize)
	if _, err := rand.Read(iv); err != nil {
		log.Fatal(err)
	}

	fmt.Println("IV (Hex):", hex.EncodeToString(iv))

	modeEnc := cipher.NewCBCEncrypter(block, iv)
	modeEnc.CryptBlocks(ciphertext, plainPadded)
	fmt.Println("CipherText (Hex):", hex.EncodeToString(ciphertext))

	// decryptedPadded := make([]byte, len(ciphertext))
	modeEnc = cipher.NewCBCDecrypter(block, iv)
	modeEnc.CryptBlocks(decryptedPadded, ciphertext)
	decrypted, err = pkcs7Unpad(decryptedPadded)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("Decrypted Text:", string(decrypted))
} 