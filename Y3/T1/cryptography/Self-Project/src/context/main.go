package main

import (
	"fmt"
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"encoding/hex"
	"log"
)

func main() {
	key := []byte("ThisIsA16ByteKey") 
	plaintext := []byte("MyNameIsJohnDoeMuehehe:D")

	fmt.Println("Plaintext : ", string(plaintext))

	block, err := aes.NewCipher(key)
	if err != nil {
		log.Fatal(err)
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		log.Fatal(err)
	}

	nonce := make([]byte, gcm.NonceSize())
	if _, err := rand.Read(nonce); err != nil {
		log.Fatal(err)
	}
	fmt.Println("Nonce : ", hex.EncodeToString(nonce))

	ciphertext := gcm.Seal(nil, nonce, plaintext, nil)
	fmt.Println("Ciphertext + Tag ( Hex ) : ", hex.EncodeToString(ciphertext))

	decrypted, err := gcm.Open(nil, nonce, ciphertext, nil)
	if err != nil {
		log.Fatal("Decryption Failed : ", err)
	}

	fmt.Println("Decrypted Text : ", string(decrypted))
}