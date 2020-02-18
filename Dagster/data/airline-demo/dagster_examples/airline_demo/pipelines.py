"""Pipeline definitions for the airline_demo."""
from dagster_aws.s3.file_cache import s3_file_cache
from dagster_aws.s3.file_manager import S3FileHandle
from dagster_aws.s3.resources import s3_resource
from dagster_aws.s3.solids import file_handle_to_s3
from dagster_aws.s3.system_storage import s3_plus_default_storage_defs
from dagster_pyspark import pyspark_resource

from dagster import ModeDefinition, PresetDefinition, composite_solid, file_relative_path, pipeline
from dagster.core.storage.file_cache import fs_file_cache
from dagster.core.storage.temp_file_manager import tempfile_resource

from .cache_file_from_s3 import cache_file_from_s3
from .resources import postgres_db_info_resource, redshift_db_info_resource
from .solids import (
    average_sfo_outbound_avg_delays_by_destination,
    delays_by_geography,
    delays_vs_fares,
    delays_vs_fares_nb,
    eastbound_delays,
    ingest_csv_file_handle_to_spark,
    join_q2_data,
    load_data_to_database_from_spark,
    process_sfo_weather_data,
    q2_sfo_outbound_flights,
    s3_to_df,
    s3_to_dw_table,
    sfo_delays_by_destination,
    tickets_with_destination,
    westbound_delays,
)

test_mode = ModeDefinition(
    name='test',
    resource_defs={
        'spark': pyspark_resource,
        'db_info': redshift_db_info_resource,
        'tempfile': tempfile_resource,
        's3': s3_resource,
        'file_cache': fs_file_cache,
    },
    system_storage_defs=s3_plus_default_storage_defs,
)


local_mode = ModeDefinition(
    name='local',
    resource_defs={
        'spark': pyspark_resource,
        's3': s3_resource,
        'db_info': postgres_db_info_resource,
        'tempfile': tempfile_resource,
        'file_cache': fs_file_cache,
    },
    system_storage_defs=s3_plus_default_storage_defs,
)


prod_mode = ModeDefinition(
    name='prod',
    resource_defs={
        'spark': pyspark_resource,  # FIXME
        's3': s3_resource,
        'db_info': redshift_db_info_resource,
        'tempfile': tempfile_resource,
        'file_cache': s3_file_cache,
    },
    system_storage_defs=s3_plus_default_storage_defs,
)


@pipeline(
    # ordered so the local is first and therefore the default
    mode_defs=[local_mode, test_mode, prod_mode],
    preset_defs=[
        PresetDefinition.from_files(
            name='local_fast',
            mode='local',
            environment_files=[
                file_relative_path(__file__, 'environments/local_base.yaml'),
                file_relative_path(__file__, 'environments/local_fast_ingest.yaml'),
            ],
        ),
        PresetDefinition.from_files(
            name='local_full',
            mode='local',
            environment_files=[
                file_relative_path(__file__, 'environments/local_base.yaml'),
                file_relative_path(__file__, 'environments/local_full_ingest.yaml'),
            ],
        ),
    ],
)
def airline_demo_ingest_pipeline():
    # on time data
    load_data_to_database_from_spark.alias('load_q2_on_time_data')(
        data_frame=join_q2_data(
            april_data=s3_to_df.alias('april_on_time_s3_to_df')(),
            may_data=s3_to_df.alias('may_on_time_s3_to_df')(),
            june_data=s3_to_df.alias('june_on_time_s3_to_df')(),
            master_cord_data=s3_to_df.alias('master_cord_s3_to_df')(),
        )
    )

    # load weather data
    load_data_to_database_from_spark.alias('load_q2_sfo_weather')(
        process_sfo_weather_data(
            ingest_csv_file_handle_to_spark.alias('ingest_q2_sfo_weather')(
                cache_file_from_s3.alias('download_q2_sfo_weather')()
            )
        )
    )

    s3_to_dw_table.alias('process_q2_coupon_data')()
    s3_to_dw_table.alias('process_q2_market_data')()
    s3_to_dw_table.alias('process_q2_ticket_data')()


def define_airline_demo_ingest_pipeline():
    return airline_demo_ingest_pipeline


@composite_solid
def process_delays_by_geo() -> S3FileHandle:
    return file_handle_to_s3.alias('upload_delays_by_geography_pdf_plots')(
        delays_by_geography(
            westbound_delays=westbound_delays(), eastbound_delays=eastbound_delays()
        )
    )


@pipeline(
    mode_defs=[test_mode, local_mode, prod_mode],
    preset_defs=[
        PresetDefinition.from_files(
            name='local',
            mode='local',
            environment_files=[
                file_relative_path(__file__, 'environments/local_base.yaml'),
                file_relative_path(__file__, 'environments/local_warehouse.yaml'),
            ],
        )
    ],
)
def airline_demo_warehouse_pipeline():
    process_delays_by_geo()

    outbound_delays = average_sfo_outbound_avg_delays_by_destination(q2_sfo_outbound_flights())

    file_handle_to_s3.alias('upload_delays_vs_fares_pdf_plots')(
        delays_vs_fares_nb.alias('fares_vs_delays')(
            delays_vs_fares(
                tickets_with_destination=tickets_with_destination(),
                average_sfo_outbound_avg_delays_by_destination=outbound_delays,
            )
        )
    )

    file_handle_to_s3.alias('upload_outbound_avg_delay_pdf_plots')(
        sfo_delays_by_destination(outbound_delays)
    )


def define_airline_demo_warehouse_pipeline():
    return airline_demo_warehouse_pipeline
