package main

import (
		"net/url"
		"time"
		"fmt"
		"log"

        mqtt "github.com/eclipse/paho.mqtt.golang"
)

// will parse string like https://user:password@example.com:8080 - host will contain port also
func createClientOptions(clientId string, uriTxt string) *mqtt.ClientOptions {
	uri, err := url.Parse(uriTxt)
	if err != nil {
		log.Fatal(err)
	}
	opts := mqtt.NewClientOptions()
	opts.AddBroker(fmt.Sprintf("%s://%s", uri.Scheme, uri.Host))
	opts.SetUsername(uri.User.Username())
	password, _ := uri.User.Password()
	opts.SetPassword(password)
	opts.SetClientID(clientId)
	return opts
}

func connect(opts *mqtt.ClientOptions) mqtt.Client {

	client := mqtt.NewClient(opts)
	if token := client.Connect(); token.Wait() && token.Error() != nil {
			log.Fatal(token.Error())
	}
	return client
}

func main() {

	const TOPIC = "my-mqtt-topic/test"
	const URI = "tcp://guest:guest@localhost:1883"

	opts := createClientOptions("testgo",URI)
	client := connect(opts)

	timer := time.NewTicker(2 * time.Second)
	for t := range timer.C {
		log.Println("sent: TOPIC:", TOPIC, " MESSAGE:",t)
		client.Publish(TOPIC, 0, false, t.String())
	}
}