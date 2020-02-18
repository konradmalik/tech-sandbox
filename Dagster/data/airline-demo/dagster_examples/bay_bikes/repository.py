from dagster import RepositoryDefinition

from .pipelines import (
    extract_daily_weather_data_pipeline,
    extract_monthly_bay_bike_pipeline,
    model_training_pipeline,
)


def define_repo():
    return RepositoryDefinition(
        name='bay_bikes_demo',
        pipeline_dict={
            'extract_monthly_bay_bike_pipeline': lambda: extract_monthly_bay_bike_pipeline,
            'extract_daily_weather_data_pipeline': lambda: extract_daily_weather_data_pipeline,
            'model_training_pipeline': lambda: model_training_pipeline,
        },
    )
