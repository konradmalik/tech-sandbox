import csv
from collections import OrderedDict

from dagster_examples.intro_tutorial.custom_types_2 import (
    less_simple_data_frame_type_check as lsdftc2,
)
from dagster_examples.intro_tutorial.custom_types_2 import sort_by_calories
from dagster_examples.intro_tutorial.custom_types_3 import (
    less_simple_data_frame_type_check as lsdftc3,
)
from dagster_examples.intro_tutorial.custom_types_4 import (
    less_simple_data_frame_type_check as lsdftc4,
)
from dagster_examples.intro_tutorial.custom_types_5 import expect_column_to_be_integers
from dagster_examples.intro_tutorial.custom_types_5 import (
    less_simple_data_frame_type_check as lsdftc5,
)

from dagster import ExpectationResult, execute_solid
from dagster.utils import script_relative_path


def test_type_check():
    for less_simple_data_frame_type_check in [lsdftc2, lsdftc3, lsdftc4, lsdftc5]:
        res = less_simple_data_frame_type_check('foo')
        assert res is False or res.success is False

        res = less_simple_data_frame_type_check(
            [OrderedDict([('foo', 1)]), OrderedDict([('foo', 2)])]
        )
        assert res is True or res.success is True

        res = less_simple_data_frame_type_check(
            [OrderedDict([('foo', 1)]), OrderedDict([('bar', 2)])]
        )
        assert res is False or res.success is False

        res = less_simple_data_frame_type_check([OrderedDict([('foo', 1)]), 2])
        assert res is False or res.success is False


def test_sort():
    with open(script_relative_path('../../dagster_examples/intro_tutorial/cereal.csv'), 'r') as fd:
        cereals = [row for row in csv.DictReader(fd)]

    execute_solid(sort_by_calories, input_values={'cereals': cereals})


def test_expectation():
    assert isinstance(
        expect_column_to_be_integers([OrderedDict([('foo', 1)])], 'foo'), ExpectationResult
    )
