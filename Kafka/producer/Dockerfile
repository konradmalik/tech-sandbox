FROM python:3.7

# Install requirements early so we can change code and re-build quickly
COPY requirements.txt .
RUN  pip install -r requirements.txt

COPY app.py app.py

# exectute start up script (use unbuffered for docker logs)
CMD ["python", "-u", "app.py"]
