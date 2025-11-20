package mycrypto

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"errors"
	"io"
)

// Encrypt takes a key and plaintext and returns the encrypted data (nonce + ciphertext).
// It implements the GCM logic found in the PDF [cite: 292-301].
func Encrypt(key, plaintext []byte) ([]byte, error) {
	// 1. Create the AES cipher block [cite: 292]
	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}

	// 2. Create the GCM AEAD cipher [cite: 294]
	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}

	// 3. Create a unique nonce (standard size is 12 bytes) [cite: 295]
	nonce := make([]byte, gcm.NonceSize())
	if _, err := io.ReadFull(rand.Reader, nonce); err != nil {
		return nil, err
	}

	// 4. Encrypt and "Seal". We prepend the nonce to the ciphertext so we can use it for decryption later.
	// The PDF explains Seal takes (dst, nonce, plaintext, additionalData) [cite: 298]
	ciphertext := gcm.Seal(nonce, nonce, plaintext, nil)
	return ciphertext, nil
}

// Decrypt takes a key and ciphertext (which includes the nonce) and returns plaintext.
func Decrypt(key, ciphertext []byte) ([]byte, error) {
	// 1. Create the AES cipher block
	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}

	// 2. Create GCM mode
	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return nil, err
	}

	// 3. Separate the Nonce and the actual Ciphertext
	nonceSize := gcm.NonceSize()
	if len(ciphertext) < nonceSize {
		return nil, errors.New("ciphertext too short")
	}
	nonce, actualCiphertext := ciphertext[:nonceSize], ciphertext[nonceSize:]

	// 4. Decrypt and "Open" [cite: 303]
	// If the tag is invalid (tampering), this returns an error [cite: 304]
	plaintext, err := gcm.Open(nil, nonce, actualCiphertext, nil)
	if err != nil {
		return nil, err
	}

	return plaintext, nil
}