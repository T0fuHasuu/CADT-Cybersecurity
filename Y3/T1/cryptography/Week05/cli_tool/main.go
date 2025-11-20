package main

import ( 
	"Week05/mycryto"
	"encoding/hex"
	"fmt"
	"os"
)

func main() {
	// Hardcoded key as used in the PDF examples (32 bytes for AES-256) [cite: 135]
	// In a real production tool, you would load this securely, not hardcode it.
	key := []byte("example key 1234example key 1234")

	if len(os.Args) < 3 {
		fmt.Println("Usage:")
		fmt.Println("  Encrypt: go run main.go enc \"Your Message\"")
		fmt.Println("  Decrypt: go run main.go dec \"HexCiphertext\"")
		return
	}

	mode := os.Args[1]
	input := os.Args[2]

	if mode == "enc" {
		// Perform Encryption
		encryptedBytes, err := mycrypto.Encrypt(key, []byte(input))
		if err != nil {
			panic(err)
		}
		// Print as Hex so it is readable/copyable [cite: 84]
		fmt.Printf("Encrypted (Hex): %s\n", hex.EncodeToString(encryptedBytes))

	} else if mode == "dec" {
		// Perform Decryption
		// First, decode the hex string back to bytes
		data, err := hex.DecodeString(input)
		if err != nil {
			panic("Invalid hex string")
		}

		decryptedBytes, err := mycrypto.Decrypt(key, data)
		if err != nil {
			// This error happens if authentication fails (wrong key or tampered data) [cite: 304]
			fmt.Println("Decryption failed! The message may have been tampered with.")
			return
		}
		fmt.Printf("Decrypted Message: %s\n", string(decryptedBytes))
	}
}