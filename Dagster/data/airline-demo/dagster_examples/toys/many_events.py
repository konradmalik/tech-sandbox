from dagster import (
    EventMetadataEntry,
    ExpectationResult,
    InputDefinition,
    Materialization,
    Nothing,
    Output,
    OutputDefinition,
    file_relative_path,
    pipeline,
    solid,
)

MARKDOWN_EXAMPLE = 'markdown_example.md'

raw_files = [
    'raw_file_users',
    'raw_file_groups',
    'raw_file_events',
    'raw_file_friends',
    'raw_file_pages',
    'raw_file_fans',
    'raw_file_event_admins',
    'raw_file_group_admins',
]


def create_raw_file_solid(name):
    def do_expectation(_context, _value):
        return ExpectationResult(
            success=True,
            label='output_table_exists',
            description='Checked {name} exists'.format(name=name),
        )

    @solid(
        name=name,
        description='Inject raw file for input to table {} and do expectation on output'.format(
            name
        ),
    )
    def raw_file_solid(_context):
        yield Materialization(
            label='table_info',
            metadata_entries=[
                EventMetadataEntry.path(label='table_path', path='/path/to/{}.raw'.format(name))
            ],
        )
        yield do_expectation(_context, name)
        yield Output(name)

    return raw_file_solid


raw_tables = [
    'raw_users',
    'raw_groups',
    'raw_events',
    'raw_friends',
    'raw_pages',
    'raw_fans',
    'raw_event_admins',
    'raw_group_admins',
]


def create_raw_file_solids():
    return list(map(create_raw_file_solid, raw_files))


def input_name_for_raw_file(raw_file):
    return raw_file + '_ready'


@solid(
    input_defs=[InputDefinition('start', Nothing)],
    output_defs=[OutputDefinition(Nothing)],
    description='Load a bunch of raw tables from corresponding files',
)
def many_table_materializations(_context):
    with open(file_relative_path(__file__, MARKDOWN_EXAMPLE), 'r') as f:
        md_str = f.read()
        for table in raw_tables:
            yield Materialization(
                label='table_info',
                metadata_entries=[
                    EventMetadataEntry.text(text=table, label='table_name'),
                    EventMetadataEntry.fspath(path='/path/to/{}'.format(table), label='table_path'),
                    EventMetadataEntry.json(data={'name': table}, label='table_data'),
                    EventMetadataEntry.url(
                        url='https://bigty.pe/{}'.format(table), label='table_name_big'
                    ),
                    EventMetadataEntry.md(md_str=md_str, label='table_blurb'),
                ],
            )


@solid(
    input_defs=[InputDefinition('start', Nothing)],
    output_defs=[OutputDefinition(Nothing)],
    description='This simulates a solid that would wrap something like dbt, '
    'where it emits a bunch of tables and then say an expectation on each table, '
    'all in one solid',
)
def many_materializations_and_passing_expectations(_context):
    tables = [
        'users',
        'groups',
        'events',
        'friends',
        'pages',
        'fans',
        'event_admins',
        'group_admins',
    ]

    for table in tables:
        yield Materialization(
            label='table_info',
            metadata_entries=[
                EventMetadataEntry.path(label='table_path', path='/path/to/{}.raw'.format(table))
            ],
        )
        yield ExpectationResult(
            success=True,
            label='{table}.row_count'.format(table=table),
            description='Row count passed for {table}'.format(table=table),
        )


@solid(
    input_defs=[InputDefinition('start', Nothing)],
    output_defs=[],
    description='A solid that just does a couple inline expectations, one of which fails',
)
def check_users_and_groups_one_fails_one_succeeds(_context):
    yield ExpectationResult(
        success=True,
        label='user_expectations',
        description='Battery of expectations for user',
        metadata_entries=[
            EventMetadataEntry.json(
                label='table_summary',
                data={
                    'columns': {
                        'name': {'nulls': 0, 'empty': 0, 'values': 123, 'average_length': 3.394893},
                        'time_created': {'nulls': 1, 'empty': 2, 'values': 120, 'average': 1231283},
                    }
                },
            )
        ],
    )

    yield ExpectationResult(
        success=False,
        label='groups_expectations',
        description='Battery of expectations for groups',
        metadata_entries=[
            EventMetadataEntry.json(
                label='table_summary',
                data={
                    'columns': {
                        'name': {'nulls': 1, 'empty': 0, 'values': 122, 'average_length': 3.394893},
                        'time_created': {'nulls': 1, 'empty': 2, 'values': 120, 'average': 1231283},
                    }
                },
            )
        ],
    )


@solid(
    input_defs=[InputDefinition('start', Nothing)],
    output_defs=[],
    description='A solid that just does a couple inline expectations',
)
def check_admins_both_succeed(_context):
    yield ExpectationResult(success=True, label='Group admins check out')
    yield ExpectationResult(success=True, label='Event admins check out')


@pipeline
def many_events():
    raw_files_solids = [raw_file_solid() for raw_file_solid in create_raw_file_solids()]

    mtm = many_table_materializations(raw_files_solids)
    mmape = many_materializations_and_passing_expectations(mtm)
    check_users_and_groups_one_fails_one_succeeds(mmape)
    check_admins_both_succeed(mmape)
