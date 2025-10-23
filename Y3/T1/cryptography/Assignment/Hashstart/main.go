package main

import (
	"crypto/md5"
	"crypto/sha1"
	"crypto/sha256"
	"crypto/sha3"
	"crypto/sha512"
	"encoding/hex"
	"errors"
	"fmt"
	"log"
	"regexp"
	"strconv"

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


func ExtractFlagFromPattern(pattern string) (string, error) {
	hexRe := regexp.MustCompile(`\\x([0-9A-Fa-f]{2})`)
	matches := hexRe.FindAllStringSubmatch(pattern, -1)
	if len(matches) == 0 {
		return "", errors.New("no \\x.. bytes found")
	}
	var b []byte
	for _, m := range matches {
		v, err := strconv.ParseUint(m[1], 16, 8)
		if err != nil {
			return "", err
		}
		b = append(b, byte(v))
	}
	part := string(b)
	return "cryptoCTF{" + part + part + "}", nil
}

func main() {
	// Task 0
	// var a, b string
	// fmt.Print("Please input value 1 : ")
	// fmt.Scanln(&a)
	// fmt.Print("Please input value 2 : ")
	// fmt.Scanln(&b)
	// proofMe(a, b)
	
	// Task 1
	// target := "6a85dfd77d9cb35770c9dc6728d73d3f"
	// wordlist := "nord_vpn.txt"

	// found, err := crack.CrackMD5(target, wordlist)
	// if err != nil {
	// 	log.Fatal(err)
	// }
	// if found != "" {
	// 	fmt.Println("FOUND:", found)
	// } else {
	// 	fmt.Println("Not found in wordlist")
	// }

	// Task 2
	// target := "aa1c7d931cf140bb35a5a16adeb83a551649c3b9"
	// wordlist := "nord_vpn.txt"

	// found, err := crack.CrackSHA1(target, wordlist)
	// if err != nil {
	// 	log.Fatal(err)
	// }
	// if found != "" {
	// 	fmt.Println("FOUND:", found)
	// } else {
	// 	fmt.Println("Not found in wordlist")
	// }

	

	// Task 3
	// target := "485f5c36c6f8474f53a3b0e361369ee3e32ee0de2f368b87b847dd23cb284b316bb0f026ada27df76c31ae8fa8696708d14b4d8fa352dbd8a31991b90ca5dd38"
	// wordlist := "nord_vpn.txt"

	// found, err := crack.CrackSHA512(target, wordlist)
	// if err != nil {
	// 	log.Fatal(err)
	// }
	// if found != "" {
	// 	fmt.Println("FOUND:", found)
	// } else {
	// 	fmt.Println("Not found in wordlist")
	// }



	// Task 4
	// pattern := `^cryptoCTF\{(?:\x6d\x65\x6f\x77){2}\}$`
	// flag, err := ExtractFlagFromPattern(pattern)
	// if err != nil {
	// 	fmt.Println("error:", err)
	// 	return
	// }
	// fmt.Println(flag)
}
