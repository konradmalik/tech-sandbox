package com.lightbend.java.kafkastreams.modelserver.memorystore;

import com.lightbend.java.kafkastreams.store.StoreState;
import com.lightbend.java.model.CurrentModelDescriptor;
import com.lightbend.java.model.DataConverter;
import com.lightbend.java.model.ModelServingInfo;
import com.lightbend.java.model.ModelWithDescriptor;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Optional;

/**
 * Handle new model parameters; updates the current model used for scoring.
 * Created by boris on 7/12/17.
 */
public class ModelProcessor extends AbstractProcessor<byte[], byte[]> {

    private StoreState modelStore;

    @Override
    public void process(byte[] key, byte[] value) {

        Optional<CurrentModelDescriptor> descriptor = DataConverter.convertModel(value);
        if(!descriptor.isPresent()){
            return;                                                 // Bad record
            // Exercise:
            // Like all good production code, we're ignoring errors ;) here! That is, we filter to keep
            // messages where `isPresent()` is true and ignore the failures.
            // With the topology API, this is harder to fix; what could you do right here??
            // See the implementation of `DataConverter`, where we inject fake errors. Add the same logic for models there.
        }
        Optional<ModelWithDescriptor> modelWithDescriptor = DataConverter.convertModel(descriptor);
        if(modelWithDescriptor.isPresent()){
           modelStore.setNewModel(modelWithDescriptor.get().getModel());
           modelStore.setNewServingInfo(new ModelServingInfo(modelWithDescriptor.get().getDescriptor().getName(),
                    modelWithDescriptor.get().getDescriptor().getDescription(), 0));
        }
    }

    @Override
    public void init(ProcessorContext context) {
        modelStore = StoreState.getInstance();
    }
}
