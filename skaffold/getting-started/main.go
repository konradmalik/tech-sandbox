package main

import (
	"fmt"
	"time"
)

func main() {
	for {
		fmt.Println("Hello world!")
		fmt.Println("skaffold is cool!")
		fmt.Println("very cool!")

		time.Sleep(time.Second * 1)
	}
}
