package main

import "fmt"

func divide(a, b int) (int, error) {
	if b == 0 {
		return 0, fmt.Errorf("cannot divide by zero")
	}
	return a / b, nil
}

func modulo(a, b int) (int, error) {
	if b == 0 {
		return 0, fmt.Errorf("cannot modulo by zero")
	}
	return a % b, nil
}

func Task1(a, b int) {
	fmt.Printf("c = a = %d\n", a)
	fmt.Printf("a += b → %d\n", a+b)
	fmt.Printf("a -= b → %d\n", a-b)
	fmt.Printf("a *= b → %d\n", a*b)

	if res, err := divide(a, b); err == nil {
		fmt.Printf("a /= b → %d\n", res)
	} else {
		fmt.Println("a /= b →", err)
	}

	if res, err := modulo(a, b); err == nil {
		fmt.Printf("a %%= b → %d\n", res)
	} else {
		fmt.Println("a %%= b →", err)
	}
}

func main() {
	var a, b int

	fmt.Print("Enter A: ")
	fmt.Scan(&a)
	fmt.Print("Enter B: ")
	fmt.Scan(&b)

	fmt.Println("\n--- Assignment Operators Demo ---")
	Task1(a, b)
}
