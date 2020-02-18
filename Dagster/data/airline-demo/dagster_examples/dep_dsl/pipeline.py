'''
This demo example is to demonstrate that it is possible to construct
the dependencies of a pipeline definition from a yaml file or similar.
This is not the "blessed" file format nor is it our recommendation for
doing things. However we've been asked a number of times if this is possible
and this should serve as a reasonable template to build upon. 
'''
from dagster import (
    DependencyDefinition,
    PipelineDefinition,
    SolidInvocation,
    check,
    file_relative_path,
    solid,
)
from dagster.utils import load_yaml_from_path


@solid
def add_one(_, num: int) -> int:
    return num + 1


@solid
def add_two(_, num: int) -> int:
    return num + 2


@solid
def add(_, left: int, right: int) -> int:
    return left + right


def construct_pipeline_with_yaml(yaml_file, solid_defs):
    yaml_data = load_yaml_from_path(yaml_file)
    solid_def_dict = {s.name: s for s in solid_defs}

    deps = {}

    for solid_yaml_data in yaml_data['pipeline']['solids']:
        check.invariant(solid_yaml_data['def'] in solid_def_dict)
        def_name = solid_yaml_data['def']
        alias = solid_yaml_data.get('alias', def_name)
        solid_deps_entry = {}
        for input_name, input_data in solid_yaml_data.get('deps', {}).items():
            solid_deps_entry[input_name] = DependencyDefinition(
                solid=input_data['solid'], output=input_data.get('output', 'result')
            )
        deps[SolidInvocation(name=def_name, alias=alias)] = solid_deps_entry

    return PipelineDefinition(
        name=yaml_data['pipeline']['name'],
        description=yaml_data['pipeline'].get('description'),
        solid_defs=solid_defs,
        dependencies=deps,
    )


def define_dep_dsl_pipeline():
    return construct_pipeline_with_yaml(
        file_relative_path(__file__, 'example.yaml'), [add_one, add_two, add]
    )
