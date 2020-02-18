package main

import (
	"fmt"
	"log"
	"strings"
)

type token struct {
	kind string
	value string
}

func tokenizer(input string) []token {
	input += "\n"

	current := 0

	tokens := []token{}
	runes := []rune(input)

	for current < len(runes) {

		char := string(runes[current])

		if char == "(" || char == ")" {

			tokens = append(tokens, token{
				kind: "paren",
				value: char,
			})

			current++
			continue
		}

		if char == " " {
			current++
			continue
		}

		if isNumber(char) {
			value := ""

			for isNumber(char) {
				value += char
				current++
				char = string(runes[current])
			}

			tokens = append(tokens, token{
				kind: "number",
				value: value,
			})

			continue
		}

		if isLetter(char) {
			value := ""

			for isLetter(char) {
				value += char
				current++
				char = string(runes[current])
			}

			tokens = append(tokens, token{
				kind: "name",
				value: value
			})

			continue
		}
		break
	}

	return tokens
}

func isBetween(char string, start string, end string) bool {
	if char == "" {
		return false
	}
	n := []rune(char)[0]
	if n >= start && n <= end {
		return true
	}
	return false
}

func isNumber(char string) bool {
	return isBetween(char, '0', '9')
}

func isLetter(char string) bool {
	return isBetween(char, 'a', 'z')
}