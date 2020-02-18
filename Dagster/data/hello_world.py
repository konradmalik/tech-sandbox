import csv
from dagster import pipeline, execute_pipeline, solid

@solid
def hello_world(context):
    # Assuming the dataset is in the same directory as this file
    dataset_path = 'cereal.csv'
    with open(dataset_path, 'r') as fd:
        # Read the rows in using the standard csv library
        cereals = [row for row in csv.DictReader(fd)]

    context.log.info(
        'Found {n_cereals} cereals'.format(n_cereals=len(cereals))
    )

@pipeline
def hello_world_pipeline():
    hello_world()
