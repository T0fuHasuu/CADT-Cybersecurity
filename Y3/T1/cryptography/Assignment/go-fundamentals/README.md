# Function

## Input Function

```go
func inputInt() (int, int) {
 var a, b int
 fmt.Print("Enter A (int) : ")
 fmt.Scan(&a)
 fmt.Print("Enter B (int) : ")
 fmt.Scan(&b)
 return a, b
}
```

### 1. Assignment Operators

```go
func task1() {
 a, b := inputInt()
 c:=a
 fmt.Printf("c = a = %d\n", c)
 fmt.Printf("%d += %d = %d\n", a, b, a + b)
 fmt.Printf("%d -= %d = %d\n", a, b, a - b)
 fmt.Printf("%d *= %d = %d\n", a, b, a * b)
 fmt.Printf("%d /= %d = %d\n", a, b, a / b)
 fmt.Printf("%d %%= %d = %d\n", a, b, a % b)
}
```

> result

```bash
Task 1 : Assignment operators
Enter A (int) : 2
Enter B (int) :5
c = a = 2
2 += 5 = 7
2 -= 5 = -3
2 *= 5 = 10
2 /= 5 = 0
2 %= 5 = 2
> 
```

### 2. Logical Operators

```go
func task2() {
 a, b := inputInt()
 bothPositive := (a > 0) && (b > 0)
 fmt.Printf("(%d > 0) && (%d > 0) : %v\n", a, b, bothPositive)
 oneGreater := (a > b) || (b > a)
 fmt.Printf("(%d > %d) || (%d > %d) : %v\n", a, b, b, a, oneGreater)
 notEqual := !(a == b)
 fmt.Printf("!(%d == %d) : %v\n", a, b, notEqual)
}
```

> result

```bash
Task 2 : Logical operators
Enter A (int) : 5
Enter B (int) : 4
(5 > 0) && (4 > 0) : true
(5 > 4) || (4 > 5) : true
!(5 == 4) : true
```

### 3. Bitwise and Assignment

```go
func myXor(a, b int) {fmt.Printf("%d XOR %d = %d\n", a, b, a^b)}

func myNOT(a, b int) {
 not := func(x int) int {if x == 0 {return 1};return 0}
 fmt.Printf("NOT %d = %d\n", a, not(a))
 fmt.Printf("NOT %d = %d\n", b, not(b))
}

func myOR(a, b int) {fmt.Printf("%d OR %d = %d\n", a, b, a|b)}

func myAND(a, b int) {fmt.Printf("%d AND %d = %d\n", a, b, a&b)}

func myShift(a, b int) {
 if b < 0 {
  fmt.Printf("Invalid shift count: %d (must be >= 0)\n", b)
  return
 }
 s := uint(b)
 fmt.Printf("%d << %d = %d\n", a, b, a<<s)
 fmt.Printf("%d >> %d = %d\n", a, b, a>>s)
}
```

> result

```bash
Task 3 : Bitwise operators
Enter A (int) :1
Enter B (int) :0
1 XOR 0 = 1
NOT 1 = 0
NOT 0 = 1
1 OR 0 = 1
1 AND 0 = 0
1 << 0 = 1
1 >> 0 = 1
```

### 4. Mini Calculator

```go
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
```

> result

```bash
Task 4 : Mini Calculator
===== Mini Calculator =====
1) Add 2) Sub 3) Mul 4) Div 5) Mod 6) Exit
Choose: 2
Enter a: 10
Enter b: 3
10 - 3 = 7
===== Mini Calculator =====
1) Add 2) Sub 3) Mul 4) Div 5) Mod 6) Exit
Choose: 6
Exiting.
```

### 5. Binary, Hex & Base64 Encoding

```go
func task5() {
 fmt.Print("Enter Strings : ")

 reader := bufio.NewReader(os.Stdin)
 input, err := reader.ReadString('\n')
 if err != nil {
  fmt.Println("Error reading input:", err)
  return
 }
 input = strings.TrimRight(input, "\r\n")

 data := []byte(input)
 var bld strings.Builder

 for i, by := range data {
  if i > 0 {
   bld.WriteByte(' ')
  }
  bld.WriteString(fmt.Sprintf("%08b", by))
 }

 fmt.Println("Binary:  ", bld.String())
 fmt.Println("Hex:     ", fmt.Sprintf("%x", data))
 fmt.Println("Base64:  ", base64.StdEncoding.EncodeToString(data))
}
```

> result

```bash
Task 5 : Binary, Hex, and Base64 Encoding
Enter Strings : Hello
Binary:   01001000 01100101 01101100 01101100 01101111
Hex:      48656c6c6f
Base64:   SGVsbG8=
```

### 6. XOR Encryption & Decryption

```go
func xorEncrypt(text string, key byte) string {
 b := []byte(text)
 for i := range b {
  b[i] ^= key
 }
 return string(b)
}

func main() {
 r := bufio.NewReader(os.Stdin)
 fmt.Print("Plaintext: ")
 plain, _ := r.ReadString('\n')
 plain = strings.TrimRight(plain, "\r\n")

 fmt.Print("Key (single char): ")
 keyLine, _ := r.ReadString('\n')
 keyLine = strings.TrimRight(keyLine, "\r\n")
 if len(keyLine) == 0 {
  fmt.Println("No key entered")
  return
 }
 key := keyLine[0]

 cipher := xorEncrypt(plain, key)
 fmt.Printf("Cipher (hex): %x\n", []byte(cipher))
 fmt.Printf("Decrypted: %s\n", xorEncrypt(cipher, key))
}
```

> result

```bash
Task 6 : Simple XOR Encryption/Decryption
Plaintext: Hello
Key (single char): THis
Cipher (hex): 1c3138383b
Decrypted: Hello
```

---

![alt text](https://media.tenor.com/AYMpLIY7OQcAAAAj/cat-meme.gif)