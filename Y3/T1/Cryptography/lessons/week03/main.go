package main

import (
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"strings"
)

func task1(a, b int) {
	c := a
	fmt.Println("start:", c)
	c += b; fmt.Println("+= :", c)
	c -= b; fmt.Println("-= :", c)
	c *= b; fmt.Println("*= :", c)
	c /= b; fmt.Println("/= :", c)
	c = a % b; fmt.Println("%  :", c)
}

func task2(a, b int) {
	fmt.Println("a > b:", a > b)
	fmt.Println("a == b:", a == b)
	fmt.Println("a != b:", a != b)
}

func task3(a, b int) {
	fmt.Printf("AND: %d  OR: %d  XOR: %d  NOT(a): %d\n", a&b, a|b, a^b, ^a)
	fmt.Printf("SHL: %d  SHR: %d\n", a<<1, a>>1)
}

func add(a, b int) int      { return a + b }
func sub(a, b int) int      { return a - b }
func mul(a, b int) int      { return a * b }
func div(a, b int) int      { return a / b }
func mod(a, b int) int      { return a % b }

func task5(s string) {
	parts := make([]string, 0, len(s))
	for _, bt := range []byte(s) {
		parts = append(parts, fmt.Sprintf("%08b", bt))
	}
	fmt.Println("Binary :", strings.Join(parts, " "))
	fmt.Println("Hex    :", hex.EncodeToString([]byte(s)))
	fmt.Println("Base64 :", base64.StdEncoding.EncodeToString([]byte(s)))
}

func task6(text string, key byte) string {
	out := make([]byte, len(text))
	for i, bt := range []byte(text) {
		out[i] = bt ^ key
	}
	return string(out)
}

func main() {
	a, b := 10, 5

	task1(a, b)
	fmt.Println()
	task2(a, b)
	fmt.Println()
	task3(a, b)
	fmt.Println()

	fmt.Println("Menu: 1)add 2)sub 3)mul 4)div 5)mod")
	var opt int
	fmt.Scan(&opt)
	switch opt {
	case 1:
		fmt.Println(add(a, b))
	case 2:
		fmt.Println(sub(a, b))
	case 3:
		fmt.Println(mul(a, b))
	case 4:
		fmt.Println(div(a, b))
	case 5:
		fmt.Println(mod(a, b))
	default:
		fmt.Println("no-op")
	}

	fmt.Println()
	task5("Helloworld")

	fmt.Println()
	msg := "This Is Dayuth Thy"
	key := byte(32)
	enc := task6(msg, key)
	fmt.Println("Encrypted (raw):", enc)
	fmt.Println("Decrypted       :", task6(enc, key))
}
