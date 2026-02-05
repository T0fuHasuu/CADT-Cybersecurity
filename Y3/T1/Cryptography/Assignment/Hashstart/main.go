package main

import (
	"crypto/md5"
	"crypto/sha1"
	"crypto/sha256"
	"crypto/sha3"
	"crypto/sha512"
	"encoding/hex"
	"fmt"
	"log"
	"example.com/utils/crack/utils/crack"
)

func computeHashes(text string) map[string]string {
	b := []byte(text)
	out := make(map[string]string)

	sMd5 := md5.Sum(b)
	out["MD5"] = hex.EncodeToString(sMd5[:])
	sSha1 := sha1.Sum(b)
	out["SHA1"] = hex.EncodeToString(sSha1[:])
	sSha256 := sha256.Sum256(b)
	out["SHA256"] = hex.EncodeToString(sSha256[:])
	sSha512 := sha512.Sum512(b)
	out["SHA512"] = hex.EncodeToString(sSha512[:])
	sSha3 := sha3.Sum256(b)
	out["SHA3-256"] = hex.EncodeToString(sSha3[:])

	return out
}

func proofMe(a, b string) {
	hA := computeHashes(a)
	hB := computeHashes(b)

	fmt.Println("========Name + Hashing Program========")
	fmt.Printf("Please input value 1 : %s\n", a)
	fmt.Printf("Please input value 2 : %s\n\n", b)

	order := []string{"MD5", "SHA1", "SHA256", "SHA512", "SHA3-256"}
	for _, name := range order {
		ha := hA[name]
		hb := hB[name]
		match := "No Match!"
		if ha == hb {
			match = "Match!"
		}
		fmt.Printf("Hash (%s) : %s / %s => %s\n", name, ha, hb, match)
	}
}

func main() {
	// Task 0
	var a, b string
	fmt.Print("Please input value 1 : ")
	fmt.Scanln(&a)
	fmt.Print("Please input value 2 : ")
	fmt.Scanln(&b)
	proofMe(a, b)

	// Wordlist
	wordlist := "nord_vpn.txt"
	
	// Task 1
	target1 := "6a85dfd77d9cb35770c9dc6728d73d3f"
	fmt.Print("Enter target MD5 (press Enter to keep default): ")
	var input string
	if _, err := fmt.Scanln(&input); err == nil && input != "" {target1 = input}
	found, err := crack.CrackMD5(target1, wordlist)
	if err != nil { log.Fatal(err) }
	if found != "" { fmt.Println("FOUND : ", found) } else { fmt.Println("Not found in wordlist") }

	// Task 2
	target2 := "aa1c7d931cf140bb35a5a16adeb83a551649c3b9"
	fmt.Print("Enter target SHA1 (press Enter to keep default): ")
	if _, err := fmt.Scanln(&input); err == nil && input != "" {target2 = input}
	found, err = crack.CrackSHA1(target2, wordlist)
	if err != nil { log.Fatal(err) }
	if found != "" { fmt.Println("FOUND : ", found) } else { fmt.Println("Not found in wordlist") }

	// Task 3
	target3 := "485f5c36c6f8474f53a3b0e361369ee3e32ee0de2f368b87b847dd23cb284b316bb0f026ada27df76c31ae8fa8696708d14b4d8fa352dbd8a31991b90ca5dd38"
	fmt.Print("Enter target SHA512 (press Enter to keep default): ")
	if _, err := fmt.Scanln(&input); err == nil && input != "" {target3 = input}
	found, err = crack.CrackSHA512(target3, wordlist)
	if err != nil { log.Fatal(err) }
	if found != "" { fmt.Println("FOUND : ", found) } else { fmt.Println("Not found in wordlist") }

	// Task 4 
	test := "meowmeow"
	sum := md5.Sum([]byte(test))
	digest := hex.EncodeToString(sum[:])
	target4 := "b6ccb4ece5454dcae51778b3e239ebc2"

	if digest == target4 {
		fmt.Printf("Matched!! FLAG: cryptoCTF{%s}\n", test)
	} else {
		fmt.Printf("MD5(%q) = %s (target %s) - not a flag\n", test, digest, target4)
	}
}
