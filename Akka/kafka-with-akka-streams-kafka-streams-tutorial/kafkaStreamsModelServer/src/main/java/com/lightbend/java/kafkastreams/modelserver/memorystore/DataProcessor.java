package com.lightbend.java.kafkastreams.modelserver.memorystore;

import com.lightbend.java.model.ServingResult;
import com.lightbend.java.kafkastreams.store.StoreState;
import com.lightbend.java.model.DataConverter;
import com.lightbend.java.model.ModelServingInfo;
import com.lightbend.model.Winerecord;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;

import java.util.Optional;

/**
 * Handle scoring where the model will be updated occasionally.
 * Adapted from this example:
 * https://github.com/bbejeck/kafka-streams/blob/master/src/main/java/bbejeck/processor/stocks/StockSummaryProcessor.java
 * Created by boris on 7/12/17.
 */
public class DataProcessor extends AbstractProcessor<byte[], byte[]> {

    private StoreState modelStore;
    private ProcessorContext ctx;

    // Exercise:
    // See the exercises described in com.lightbend.java.kafkastreams.modelserver.customstore.DataProcessor.
    @Override
    public void process(byte[] key, byte[] value) {
        Optional<Winerecord.WineRecord> dataRecord = DataConverter.convertData(value);
        if(!dataRecord.isPresent()) {
            return;                                 // Bad record
            // Exercise:
            // Like all good production code, we're ignoring errors ;) here! That is, we filter to keep
            // messages where `isPresent()` is true and ignore the failures.
            // With the topology API, this is harder to fix; what could you do right here??
            // See the implementation of `DataConverter`, where we inject fake errors.
        }
        if(modelStore.getNewModel() != null){
            // update the model
            if(modelStore.getCurrentModel() != null)
                modelStore.getCurrentModel().cleanup();
            modelStore.setCurrentModel(modelStore.getNewModel());
            modelStore.setCurrentServingInfo(new ModelServingInfo(modelStore.getNewServingInfo().getName(),
                    modelStore.getNewServingInfo().getDescription(), System.currentTimeMillis()));
            modelStore.setNewServingInfo(null);
            modelStore.setNewModel(null);
        }
        // Actually score
        if(modelStore.getCurrentModel() == null) {
            // No model currently
//            System.out.println("No model available - skipping");
            ctx.forward(key, ServingResult.noModel);
        }
        else{
            // Score the model
            long start = System.currentTimeMillis();
            double quality = (double) modelStore.getCurrentModel().score(dataRecord.get());
            long duration = System.currentTimeMillis() - start;
            modelStore.getCurrentServingInfo().update(duration);
//            System.out.println("Calculated quality - " + quality + " in " + duration + "ms");
            ctx.forward(key, new ServingResult(quality, duration));
         }
         ctx.commit();
    }

    @Override
    public void init(ProcessorContext context) {
        modelStore = StoreState.getInstance();
        ctx = context;
    }
}
