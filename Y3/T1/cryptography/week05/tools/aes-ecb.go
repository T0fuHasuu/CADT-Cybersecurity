package tools

import (
	"fmt"
	"bytes"
)

func Pkcs7Pad(data []byte, blockSize int) []byte {
	padding := blockSize - len(data)%blockSize
	padText := bytes.Repeat([]byte{byte(padding)}, padding)
	return append(data, padText...)
}

func Pkcs7Unpad(data []byte) ([]byte, error) {
	if len(data) == 0 {
		return nil, fmt.Errorf("data is empty")
	}

	padding := int(data[len(data)-1])

	if padding == 0 || padding > len(data) {
		return nil, fmt.Errorf("invalid Padding Size")
	}

	return data[:len(data)-padding], nil
}
