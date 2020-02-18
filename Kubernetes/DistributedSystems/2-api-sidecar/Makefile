build:
	eval $$(minikube docker-env); \
	docker build -f src/Dockerfile.app -t sidecar2-app:v1 src; \
	docker build -f src/Dockerfile.sidecar -t sidecar2-sidecar:v1 src

deploy:
	kubectl create -f ./deployment.yml,./service.yml

check:
	kubectl get pods

purge:
	kubectl delete -f ./deployment.yml,./service.yml	
