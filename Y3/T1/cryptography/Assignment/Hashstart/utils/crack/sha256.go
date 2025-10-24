package crack

import (
	"bufio"
	"crypto/sha512"
	"encoding/hex"
	"os"
	"strings"
)

func CrackSHA512(target, wordlistPath string) (string, error) {
	target = strings.ToLower(strings.TrimSpace(target))
	f, err := os.Open(wordlistPath)
	if err != nil {
		return "", err
	}
	defer f.Close()

	sc := bufio.NewScanner(f)
	for sc.Scan() {
		line := strings.TrimSpace(sc.Text())
		if line == "" {
			continue
		}
		sum := sha512.Sum512([]byte(line))
		if hex.EncodeToString(sum[:]) == target {
			return line, nil
		}
	}
	if err := sc.Err(); err != nil {
		return "", err
	}
	return "", nil
}
