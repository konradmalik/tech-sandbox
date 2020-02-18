import csv

from dagster import (
    Bool,
    Field,
    Output,
    OutputDefinition,
    String,
    as_dagster_type,
    execute_pipeline,
    pipeline,
    solid,
)


class _DataFrame(list):
    pass


DataFrame = as_dagster_type(_DataFrame, name='DataFrame')


@solid
def read_csv(context, csv_path):
    with open(csv_path, 'r') as fd:
        lines = [row for row in csv.DictReader(fd)]

    context.log.info('Read {n_lines} lines'.format(n_lines=len(lines)))
    return DataFrame(lines)


@solid(
    config={
        'process_hot': Field(Bool, is_optional=True, default_value=True),
        'process_cold': Field(Bool, is_optional=True, default_value=True),
    },
    output_defs=[
        OutputDefinition(
            name='hot_cereals', dagster_type=DataFrame, is_optional=True
        ),
        OutputDefinition(
            name='cold_cereals', dagster_type=DataFrame, is_optional=True
        ),
    ],
)
def split_cereals(context, cereals):
    if context.solid_config['process_hot']:
        hot_cereals = DataFrame(
            [cereal for cereal in cereals if cereal['type'] == 'H']
        )
        yield Output(hot_cereals, 'hot_cereals')
    if context.solid_config['process_cold']:
        cold_cereals = DataFrame(
            [cereal for cereal in cereals if cereal['type'] == 'C']
        )
        yield Output(cold_cereals, 'cold_cereals')


@solid(config_field=Field(String))
def sort_cereals_by_calories(context, cereals):
    sorted_cereals = sorted(cereals, key=lambda cereal: cereal['calories'])
    context.log.info(
        'Least caloric {cereal_type} cereal: {least_caloric}'.format(
            cereal_type=context.solid_config,
            least_caloric=sorted_cereals[0]['name'],
        )
    )


@pipeline
def reusable_solids_pipeline():
    hot_cereals, cold_cereals = split_cereals(read_csv())
    sort_hot_cereals = sort_cereals_by_calories.alias('sort_hot_cereals')
    sort_cold_cereals = sort_cereals_by_calories.alias('sort_cold_cereals')
    sort_hot_cereals(hot_cereals)
    sort_cold_cereals(cold_cereals)


if __name__ == '__main__':
    environment_dict = {
        'solids': {
            'read_csv': {'inputs': {'csv_path': {'value': 'cereal.csv'}}},
            'sort_cold_cereals': {'config': 'cold'},
            'sort_hot_cereals': {'config': 'hot'},
        }
    }
    result = execute_pipeline(
        reusable_solids_pipeline, environment_dict=environment_dict
    )
    assert result.success
