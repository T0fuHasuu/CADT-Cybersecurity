package main

import (
	"bufio"
	"encoding/base64"
	"fmt"
	"os"
	"strings"
)

func inputInt() (int, int) {
	var a, b int
	fmt.Print("Enter A (int) :")
	fmt.Scan(&a)
	fmt.Print("Enter B (int) :")
	fmt.Scan(&b)
	return a, b
}

func task1(a, b int) {
	c:=a
	fmt.Printf("c = a = %d\n", c)
	fmt.Printf("%d += %d = %d\n", a, b, a + b)
	fmt.Printf("%d -= %d = %d\n", a, b, a - b)
	fmt.Printf("%d *= %d = %d\n", a, b, a * b)
	fmt.Printf("%d /= %d = %d\n", a, b, a / b)
	fmt.Printf("%d %%= %d = %d\n", a, b, a % b)
}

func task2(a, b int) {
	bothPositive := (a > 0) && (b > 0)
	fmt.Printf("(%d > 0) && (%d > 0) : %v\n", a, b, bothPositive)
	oneGreater := (a > b) || (b > a)
	fmt.Printf("(%d > %d) || (%d > %d) : %v\n", a, b, b, a, oneGreater)
	notEqual := !(a == b)
	fmt.Printf("!(%d == %d) : %v\n", a, b, notEqual)
}

func task4(loop bool) {
	if !loop {
		return
	}
	for {
		fmt.Println("===== Mini Calculator =====")
		fmt.Println("1) Add 2) Sub 3) Mul 4) Div 5) Mod 6) Exit")
		fmt.Print("Choose: ")
		var choice int
		if _, err := fmt.Scan(&choice); err != nil {
			fmt.Println("Invalid input, exiting.")
			return
		}

		switch choice {
		case 1, 2, 3, 4, 5:
			var a, b int
			fmt.Print("Enter a: ")
			if _, err := fmt.Scan(&a); err != nil {
				fmt.Println("Invalid input, exiting.")
				return
			}
			fmt.Print("Enter b: ")
			if _, err := fmt.Scan(&b); err != nil {
				fmt.Println("Invalid input, exiting.")
				return
			}

			switch choice {
			case 1:
				fmt.Printf("%d + %d = %d\n", a, b, a+b)
			case 2:
				fmt.Printf("%d - %d = %d\n", a, b, a-b)
			case 3:
				fmt.Printf("%d * %d = %d\n", a, b, a*b)
			case 4:
				if b == 0 {
					fmt.Println("Error: division by zero")
				} else {
					fmt.Printf("%d / %d = %d\n", a, b, a/b)
				}
			case 5:
				if b == 0 {
					fmt.Println("Error: division by zero")
				} else {
					fmt.Printf("%d %% %d = %d\n", a, b, a%b)
				}
			}

		case 6:
			fmt.Println("Exiting.")
			return
		default:
			fmt.Println("Invalid choice, exiting.")
			return
		}
	}
}

// Requires adding to imports: "bufio", "os", "encoding/base64", "strings"
func task5() {
	fmt.Println("Lab #5: Binary, Hex, and Base64 Encoding")
	fmt.Println("• Enter a line of text and press Enter.")
	fmt.Print("Input: ")

	reader := bufio.NewReader(os.Stdin)
	input, err := reader.ReadString('\n')
	if err != nil {
		fmt.Println("Error reading input:", err)
		return
	}
	// Trim trailing newline characters
	input = strings.TrimRight(input, "\r\n")

	data := []byte(input)

	// Binary representation (each byte as 8 bits)
	var bld strings.Builder
	for i, by := range data {
		if i > 0 {
			bld.WriteByte(' ')
		}
		bld.WriteString(fmt.Sprintf("%08b", by))
	}
	fmt.Println("Binary:  ", bld.String())

	// Hexadecimal representation
	fmt.Println("Hex:     ", fmt.Sprintf("%x", data))

	// Base64 representation
	fmt.Println("Base64:  ", base64.StdEncoding.EncodeToString(data))
}

func main() {
	// var a, b int
	// a, b = inputInt()
	// task4(true)
	task5()
	// task1(a, b)
	// task2(a, b)
}