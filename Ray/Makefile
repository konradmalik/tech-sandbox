build:
	docker build -t konradmalik:ray .

run:
	docker run --rm -it  \
		-p 8080:8080 \
		-v `pwd`:/root \
		konradmalik:ray \
		jupyter notebook --ip=0.0.0.0 --port=8080 --allow-root --no-browser --NotebookApp.token=jupyter --notebook-dir=/root
