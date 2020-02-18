build:
	eval $$(minikube docker-env); \
	docker build -t sidecar-app:v1 legacy-app; \
	docker build -t sidecar-sidecar:v1 sidecar-app

deploy:
	kubectl create -f ./deployment.yml,./service.yml

check:
	kubectl get pods

purge:
	kubectl delete -f ./deployment.yml,./service.yml	
