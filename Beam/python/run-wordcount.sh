#/bin/bash

docker run --rm -it \
    --name beam \
   konradmalik/interactive-apachebeam-python \
   bash -c "python -m apache_beam.examples.wordcount --input /var/log/apt/history.log --output /count"
