WD=$(shell pwd)

build-proto:
	docker run --rm -v "$(WD)":"$(WD)" -w "$(WD)" znly/protoc --go_out=plugins=grpc:. -I. \
		proto/consignment/consignment.proto

build-go:
	docker run --rm -v "$(WD)":/usr/src/myapp -w /usr/src/myapp golang go build -v
