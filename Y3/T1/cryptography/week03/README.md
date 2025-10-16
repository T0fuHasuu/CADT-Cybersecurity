# Instruction

go version
go mod init <project/package>
go run <file.go>
go build <file.go>
go get <add package>
go doc <package information and details>
go fmt <format code>

### Lib for Hacking-Related

package main

import (
	"fmt"
	"encoding/base64"
	"encoding/hex"
)

func task1(a,b int) {
	c := a 
	fmt.Println(c)

	c+=b
	fmt.Println(c)

	c-=b
	fmt.Println(c)
	
	c*=b
	fmt.Println(c)

	c/=b 
	fmt.Println(c)

	a%=b
	fmt.Println(a)
}

func task2(a,b int) {
	fmt.Println(a>b && b!=a)
	if a > b {
		fmt.Printf("A is greater than b : %v\n", a>b)
	} else { fmt.Printf("A is less than b : %v\n", a>b)}

	if a != b {
		fmt.Printf("A is diff from b : %v\n", a!=b)
	} else { fmt.Printf("A is the same as b : %v\n", a!=b)}
}



func myAND(a, b int) { fmt.Println("AND:", a&b) }
func myOR(a, b int)  { fmt.Println("OR:", a|b) }
func myXOR(a, b int) { fmt.Println("XOR:", a^b) }
func myNOT(a int)    { fmt.Println("NOT:", ^a) }
func myShift(a int)  { fmt.Println("Left Shift:", a<<1, "Right Shift:", a>>1) }

func add(a,b int) int {return a+b}
func minus(a,b int) int {return a-b}
func mulitply(a,b int) int {return a*b}
func divide(a,b int) int {return a/b}
func mod(a,b int) int {return a%b}


func task5(s string) {
	fmt.Println("Binary:", fmt.Sprintf("%08b", []byte(s)))
	fmt.Println("Hex:", hex.EncodeToString([]byte(s)))
	fmt.Println("Base64:", base64.StdEncoding.EncodeToString([]byte(s)))
}

func task6(text string, key byte) {
	xor := func(s string, k byte) string {
		out := make([]byte, len(s))
		for i := range s {
			out[i] = s[i] ^ k
		}
		return string(out)
	}

	enc := xor(text, key)
	fmt.Println("Encrypted:", enc)
	fmt.Println("Decrypted:", xor(enc, key))
}


func main () {

	var a int = 10
	var b int = 5
	var option int

	task1(a,b)
	task2(a,b)

	myXOR(a, b)
	myNOT(a)
	myOR(a, b)
	myAND(a, b)

	fmt.Print("Menu\n1. add\n2. Minus\n3. Multiply\n4. Divide\n5. Modulor\n6. Exit")

	fmt.Scan(&option)
	switch option {
	case 1:
		fmt.Println(add(a,b))
	case 2:
		fmt.Println(minus(a,b))
	case 3:
		fmt.Println(mulitply(a,b))
	case 4:
		fmt.Println(divide(a,b))
	case 5:
		fmt.Println(mod(a,b))
	default:
		break
		return
	}

	task5("Helloworld")

	task6("Thisffsaadadasdsad", 32)




}