 
package main

import (
  "log"
  "os"
  "os/signal"
  "syscall"
  "bufio"
  "encoding/csv"
  "encoding/json"
  "io"
  "github.com/Shopify/sarama"
  "strconv"
  "time"
)


const relativePathToData = "ipo-data/"
var (
    kafkaConn string
    topic string
    sensorName string
    intervalMillis int32 = 1000
    clientId string
)

type Location struct {
    Lat float32   `json:"lat"`
    Lng  float32 `json:"lng"`
}

type Sensor struct {
    Name string   `json:"name"`
    Timestamp  int64   `json:"timestamp"`
    Value  float32 `json:"value"`
    Location Location `json:"location"`
}

func main() {
    // read args
    // args[0] is the path to the go executable!
    args := os.Args
    if len(args) < 2 {
        log.Fatal("Provide topic as 1st argument, brokers as 2nd argument, " + 
        "sensor name as 3rd argument and interval in millis as 4th argument " + 
        "(4th is optional, default is 1000 millis)")
    } else {
        topic = args[1]
        kafkaConn = args[2]
        sensorName = args[3]
        clientId = topic+"Producer"
        intervalFromString, errInterval := strconv.ParseInt(args[4], 10, 32)
        if errInterval != nil {
            log.Fatal("cannot parse provided interval value, exiting...", errInterval)
        }
        intervalMillis = int32(intervalFromString)
    }


    // create producer
    producer, err := initProducer()
    if err != nil {
        log.Println("Error producer: ", err.Error())
        os.Exit(1)
	}
	
	// read file
    csvFile, _ := os.Open(makeCsvPath(sensorName))
	reader := csv.NewReader(bufio.NewReader(csvFile))
	
	// if program comes to an end, execute cleanup
	defer cleanup(producer, csvFile)

	// if interrupted (most likely) cleanup
	c := make(chan os.Signal)
	signal.Notify(c, os.Interrupt, syscall.SIGTERM)
	go func() {
		<-c
		cleanup(producer, csvFile)
		os.Exit(1)
	}()

    // dummy location
    location := Location{Lng: 21.0122, Lat: 52.2297}

    // read and parse file line by line
    for {
        line, err := reader.Read()
        if err == io.EOF {
            log.Println("reached EOF, reader reset...")
            csvFile.Seek(0, 0)
            continue
        } else if err != nil {
            log.Fatal(err)
        }

        value, errVal := strconv.ParseFloat(line[1], 32)
        if errVal != nil {
            log.Println("cannot parse value line, continuing...", errVal)
            continue
        }

        sensor := Sensor{
            Name: sensorName,
            Timestamp:  currentUnixTimestamp(),
            Value: float32(value),
            Location: location}

        sensorJson, _ := json.Marshal(sensor)
        msg := string(sensorJson)
        log.Println("sending message:",msg)
        // publish
        go publish(msg, producer)
    
        // wait for interval
        time.Sleep(time.Duration(intervalMillis) * time.Millisecond)
    }

}

func initProducer()(sarama.SyncProducer, error) {
  // setup sarama log to stdout
  sarama.Logger = log.New(os.Stdout, "", log.Ltime)

  // producer config
  config := sarama.NewConfig()
  config.Producer.Retry.Max = 5
  config.Producer.RequiredAcks = sarama.WaitForAll
  config.Producer.Return.Successes = true
  config.ClientID = clientId

  // sync producer
  prd, err := sarama.NewSyncProducer([]string{kafkaConn}, config)

  return prd, err
}

func publish(message string, producer sarama.SyncProducer) {
    // publish sync
    msg := &sarama.ProducerMessage {
        Topic: topic,
        Value: sarama.StringEncoder(message),
    }
    p, o, err := producer.SendMessage(msg)
    if err != nil {
        log.Println("Error publish: ", err.Error())
    }

    log.Printf("Partition: %d offset %d", p, o)
}

func cleanup(producer sarama.SyncProducer, f *os.File) {
	log.Println("cleanup!")
	if err := producer.Close(); err != nil {
		log.Println("Failed to close Kafka producer cleanly:", err)
	}
	if err := f.Close(); err != nil {
		log.Println("Failed to close csvFile cleanly:", err)
	}
}

func currentUnixTimestamp() int64 {
    return time.Now().UnixNano() / int64(time.Millisecond)
}

func makeCsvPath(sensorName string) string {
    return relativePathToData + sensorName + ".csv"
}