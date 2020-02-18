import os

import pytest
from dagster_aws.s3.resources import S3Resource
from dagster_examples.airline_demo.cache_file_from_s3 import cache_file_from_s3

from dagster import (
    DagsterInvalidDefinitionError,
    ModeDefinition,
    ResourceDefinition,
    execute_pipeline,
    execute_solid,
    pipeline,
)
from dagster.core.storage.file_cache import LocalFileHandle, fs_file_cache
from dagster.seven import mock
from dagster.utils.temp_file import get_temp_dir


def execute_solid_with_resources(solid_def, resource_defs, environment_dict):
    @pipeline(
        name='{}_solid_test'.format(solid_def.name),
        mode_defs=[ModeDefinition(resource_defs=resource_defs)],
    )
    def test_pipeline():
        return solid_def()

    return execute_pipeline(test_pipeline, environment_dict)


def test_cache_file_from_s3_basic():
    s3_session = mock.MagicMock()
    with get_temp_dir() as temp_dir:
        solid_result = execute_solid(
            cache_file_from_s3,
            ModeDefinition(
                resource_defs={
                    'file_cache': fs_file_cache,
                    's3': ResourceDefinition.hardcoded_resource(S3Resource(s3_session)),
                }
            ),
            environment_dict={
                'solids': {
                    'cache_file_from_s3': {
                        'inputs': {'s3_coordinate': {'bucket': 'some-bucket', 'key': 'some-key'}}
                    }
                },
                'resources': {'file_cache': {'config': {'target_folder': temp_dir}}},
            },
        )

        # assert the download occured
        assert s3_session.download_file.call_count == 1

        assert solid_result.success

        expectation_results = solid_result.expectation_results_during_compute
        assert len(expectation_results) == 1
        expectation_result = expectation_results[0]
        assert expectation_result.success
        assert expectation_result.label == 'file_handle_exists'
        path_in_metadata = expectation_result.metadata_entries[0].entry_data.path
        assert isinstance(path_in_metadata, str)
        assert os.path.exists(path_in_metadata)

        assert isinstance(solid_result.output_value(), LocalFileHandle)
        assert 'some-key' in solid_result.output_value().path_desc


def test_cache_file_from_s3_specify_target_key():
    s3_session = mock.MagicMock()
    with get_temp_dir() as temp_dir:
        solid_result = execute_solid(
            cache_file_from_s3,
            ModeDefinition(
                resource_defs={
                    'file_cache': fs_file_cache,
                    's3': ResourceDefinition.hardcoded_resource(S3Resource(s3_session)),
                }
            ),
            environment_dict={
                'solids': {
                    'cache_file_from_s3': {
                        'inputs': {'s3_coordinate': {'bucket': 'some-bucket', 'key': 'some-key'}},
                        'config': {'file_key': 'specified-file-key'},
                    }
                },
                'resources': {'file_cache': {'config': {'target_folder': temp_dir}}},
            },
        )

        # assert the download occured
        assert s3_session.download_file.call_count == 1
        assert solid_result.success
        assert isinstance(solid_result.output_value(), LocalFileHandle)
        assert 'specified-file-key' in solid_result.output_value().path_desc


def test_cache_file_from_s3_skip_download():
    with get_temp_dir() as temp_dir:
        s3_session_one = mock.MagicMock()
        execute_solid(
            cache_file_from_s3,
            ModeDefinition(
                resource_defs={
                    'file_cache': fs_file_cache,
                    's3': ResourceDefinition.hardcoded_resource(S3Resource(s3_session_one)),
                }
            ),
            environment_dict={
                'solids': {
                    'cache_file_from_s3': {
                        'inputs': {'s3_coordinate': {'bucket': 'some-bucket', 'key': 'some-key'}}
                    }
                },
                'resources': {'file_cache': {'config': {'target_folder': temp_dir}}},
            },
        )

        # assert the download occured
        assert s3_session_one.download_file.call_count == 1

        s3_session_two = mock.MagicMock()
        execute_solid(
            cache_file_from_s3,
            ModeDefinition(
                resource_defs={
                    'file_cache': fs_file_cache,
                    's3': ResourceDefinition.hardcoded_resource(S3Resource(s3_session_two)),
                }
            ),
            environment_dict={
                'solids': {
                    'cache_file_from_s3': {
                        'inputs': {'s3_coordinate': {'bucket': 'some-bucket', 'key': 'some-key'}}
                    }
                },
                'resources': {'file_cache': {'config': {'target_folder': temp_dir}}},
            },
        )

        # assert the download did not occur because file is already there
        assert s3_session_two.download_file.call_count == 0


def test_cache_file_from_s3_overwrite():
    with get_temp_dir() as temp_dir:
        s3_session_one = mock.MagicMock()
        execute_solid(
            cache_file_from_s3,
            ModeDefinition(
                resource_defs={
                    'file_cache': fs_file_cache,
                    's3': ResourceDefinition.hardcoded_resource(S3Resource(s3_session_one)),
                }
            ),
            environment_dict={
                'solids': {
                    'cache_file_from_s3': {
                        'inputs': {'s3_coordinate': {'bucket': 'some-bucket', 'key': 'some-key'}}
                    }
                },
                'resources': {
                    'file_cache': {'config': {'target_folder': temp_dir, 'overwrite': True}}
                },
            },
        )

        # assert the download occured
        assert s3_session_one.download_file.call_count == 1

        s3_session_two = mock.MagicMock()
        execute_solid(
            cache_file_from_s3,
            ModeDefinition(
                resource_defs={
                    'file_cache': fs_file_cache,
                    's3': ResourceDefinition.hardcoded_resource(s3_session_two),
                }
            ),
            environment_dict={
                'solids': {
                    'cache_file_from_s3': {
                        'inputs': {'s3_coordinate': {'bucket': 'some-bucket', 'key': 'some-key'}}
                    }
                },
                'resources': {
                    'file_cache': {'config': {'target_folder': temp_dir, 'overwrite': True}}
                },
            },
        )

        # assert the download did not occur because file is already there
        assert s3_session_two.download_file.call_count == 0


def test_missing_resources():
    with pytest.raises(DagsterInvalidDefinitionError):
        with get_temp_dir() as temp_dir:
            execute_solid(
                cache_file_from_s3,
                ModeDefinition(resource_defs={'file_cache': fs_file_cache}),
                environment_dict={
                    'solids': {
                        'cache_file_from_s3': {
                            'inputs': {
                                's3_coordinate': {'bucket': 'some-bucket', 'key': 'some-key'}
                            }
                        }
                    },
                    'resources': {'file_cache': {'config': {'target_folder': temp_dir}}},
                },
            )
