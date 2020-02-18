import csv
from dagster import solid, pipeline

@solid
def load_csv(context, csv_path: str):
    with open(csv_path, 'r') as fd:
        lines = [row for row in csv.DictReader(fd)]
    context.log.info('Read {n_lines} lines'.format(n_lines=len(lines)))
    return lines


@solid
def sort_by_calories(_, cereals):
    sorted_cereals = list(
        sorted(cereals, key=lambda cereal: cereal['calories'])
    )
    least_caloric = sorted_cereals[0]['name']
    most_caloric = sorted_cereals[-1]['name']
    return (least_caloric, most_caloric)


@solid
def sort_by_protein(_, cereals):
    sorted_cereals = list(
        sorted(cereals, key=lambda cereal: cereal['protein'])
    )
    least_protein = sorted_cereals[0]['name']
    most_protein = sorted_cereals[-1]['name']
    return (least_protein, most_protein)


@solid
def display_results(context, calorie_results, protein_results):
    context.log.info(
        'Least caloric cereal: {least_caloric}'.format(
            least_caloric=calorie_results[0]
        )
    )
    context.log.info(
        'Most caloric cereal: {most_caloric}'.format(
            most_caloric=calorie_results[1]
        )
    )
    context.log.info(
        'Least protein-rich cereal: {least_protein}'.format(
            least_protein=protein_results[0]
        )
    )
    context.log.info(
        'Most protein-rich cereal: {most_protein}'.format(
            most_protein=protein_results[1]
        )
    )


@pipeline
def parametrized_inputs_pipeline():
    cereals = load_csv()
    display_results(
        calorie_results=sort_by_calories(cereals),
        protein_results=sort_by_protein(cereals),
    )
