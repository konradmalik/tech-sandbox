FROM python:3.6

ADD requirements.txt requirements.txt
RUN pip install -r requirements.txt

CMD ["jupyter", "notebook", "--ip=0.0.0.0", "--port=8080", "--allow-root"]
