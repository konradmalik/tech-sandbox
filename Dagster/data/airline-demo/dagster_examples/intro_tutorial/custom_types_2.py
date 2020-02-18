import csv

from dagster import dagster_type, execute_pipeline, pipeline, solid


def less_simple_data_frame_type_check(value):
    if not isinstance(value, list):
        return False

    fields = [field for field in value[0].keys()]

    for i in range(len(value)):
        row = value[i]
        if not isinstance(row, dict):
            return False
        row_fields = [field for field in row.keys()]
        if fields != row_fields:
            return False

    return True


@dagster_type(
    name='LessSimpleDataFrame',
    description='A more sophisticated data frame that type checks its structure.',
    type_check=less_simple_data_frame_type_check,
)
class LessSimpleDataFrame(list):
    pass


@solid
def bad_read_csv(context, csv_path: str) -> LessSimpleDataFrame:
    with open(csv_path, 'r') as fd:
        lines = [row for row in csv.DictReader(fd)]

    context.log.info('Read {n_lines} lines'.format(n_lines=len(lines)))
    return LessSimpleDataFrame(["not_a_dict"])


@solid
def sort_by_calories(context, cereals: LessSimpleDataFrame):
    sorted_cereals = sorted(cereals, key=lambda cereal: cereal['calories'])
    context.log.info(
        'Least caloric cereal: {least_caloric}'.format(
            least_caloric=sorted_cereals[0]['name']
        )
    )
    context.log.info(
        'Most caloric cereal: {most_caloric}'.format(
            most_caloric=sorted_cereals[-1]['name']
        )
    )


@pipeline
def custom_type_pipeline():
    sort_by_calories(bad_read_csv())


if __name__ == '__main__':
    execute_pipeline(
        custom_type_pipeline,
        {
            'solids': {
                'bad_read_csv': {
                    'inputs': {'csv_path': {'value': 'cereal.csv'}}
                }
            }
        },
    )
