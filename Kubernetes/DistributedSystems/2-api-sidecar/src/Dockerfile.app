#build
FROM golang:stretch as builder

RUN apt-get update && apt-get upgrade -y && \
    apt-get install protobuf-compiler -y

RUN mkdir /go/src/app

WORKDIR /go/src/app

COPY transport transport
COPY app.go app.go
RUN go get -u github.com/golang/protobuf/protoc-gen-go
RUN protoc -I=transport --go_out=plugins=grpc:transport transport/app.proto

RUN go get
RUN CGO_ENABLED=0 GOOS=linux go build app.go

# run
FROM alpine

RUN mkdir -p /usr/app

WORKDIR /usr/app

COPY --from=builder /go/src/app/app .

ENV PORT 80

CMD ["./app"]
