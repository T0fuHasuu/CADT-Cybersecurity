package crack

import (
	"bufio"
	"crypto/md5"
	"encoding/hex"
	"os"
	"strings"
)

// CrackMD5 opens wordlistPath, computes MD5 for each line (no newline),
// compares to target (case-insensitive hex). Returns the matching plain
// string or "" if not found.
func CrackMD5(target, wordlistPath string) (string, error) {
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
		sum := md5.Sum([]byte(line))
		if hex.EncodeToString(sum[:]) == target {
			return line, nil
		}
	}
	if err := sc.Err(); err != nil {
		return "", err
	}
	return "", nil
}
