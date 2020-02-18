package com.lightbend.java.model;

import com.lightbend.java.model.PMML.PMMLModelFactory;
import com.lightbend.java.model.tensorflow.TensorFlowModelFactory;
import com.lightbend.model.Modeldescriptor.ModelDescriptor;
import com.lightbend.model.Winerecord;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

/**
 * Helper for handling a data record or model as a byte array, including parsing and I/O.
 * Created by boris on 6/28/17.
 */
public class DataConverter {

    private static final Map<Integer, ModelFactory> factories = new HashMap<Integer, ModelFactory>() {
        {
            put(ModelDescriptor.ModelType.TENSORFLOW.getNumber(), TensorFlowModelFactory.getInstance());
            put(ModelDescriptor.ModelType.PMML.getNumber(), PMMLModelFactory.getInstance());
        }
    };

    private DataConverter(){}

    // We inject random parsing errors.
    private static int percentErrors = 5;  // 5%
    private static Random rand = new Random();

    public static Optional<Winerecord.WineRecord> convertData(byte[] binary){
        try {
            if (rand.nextInt(100) < percentErrors) {
              return Optional.empty();
            } else {
              // Unmarshall record
              return Optional.of(Winerecord.WineRecord.parseFrom(binary));
            }
        } catch (Throwable t) {
            // Oops
            System.out.println("Exception parsing input record" + new String(binary));
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<CurrentModelDescriptor> convertModel(byte[] binary){
        try {
            // Unmarshall record
            ModelDescriptor model = ModelDescriptor.parseFrom(binary);
            // Return it
            if(model.getMessageContentCase().equals(ModelDescriptor.MessageContentCase.DATA)){
               return Optional.of(new CurrentModelDescriptor(
                        model.getName(), model.getDescription(), model.getModeltype(),
                        model.getData().toByteArray(), null, model.getDataType()));
            }
            else {
                System.out.println("Location based model is not yet supported");
                return Optional.empty();
            }
        } catch (Throwable t) {
            // Oops
            System.out.println("Exception parsing input record" + new String(binary));
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<ModelWithDescriptor> convertModel(Optional<CurrentModelDescriptor> descriptor){
        return convertModel(descriptor.get());
    }

    public static Optional<ModelWithDescriptor> convertModel(CurrentModelDescriptor model){

        System.out.println("New scoring model " + model);
        if(model.getModelData() == null) {
            System.out.println("Location based model is not yet supported");
            return Optional.empty();
        }
        ModelFactory factory = factories.get(model.getModelType().ordinal());
        if(factory == null){
            System.out.println("Bad model type " + model.getModelType());
            return Optional.empty();
        }
        Optional<Model> current = factory.create(model);
        if(current.isPresent())
            return Optional.of(new ModelWithDescriptor(current.get(),model));
        return Optional.empty();
    }

    public static Optional<Model> readModel(DataInputStream input) {

        try {
            int length = (int)input.readLong();
            if (length == 0)
                return Optional.empty();
            int type = (int) input.readLong();
            byte[] bytes = new byte[length];
            input.read(bytes);
            ModelFactory factory = factories.get(type);
            return Optional.of(factory.restore(bytes));
        } catch (Throwable t) {
            System.out.println("Error Deserializing model");
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<ModelServingInfo> readServingInfo(DataInputStream input){
        try {
            long length = input.readLong();
            if (length == 0)
                return Optional.empty();
            String descriprtion = input.readUTF();
            String name = input.readUTF();
            double duration = input.readDouble();
            long invocations = input.readLong();
            long max  = input.readLong();
            long min = input.readLong();
            long since = input.readLong();
            return Optional.of(new ModelServingInfo(name, descriprtion, since, invocations, duration, min, max));
        } catch (Throwable t) {
            System.out.println("Error Deserializing serving info");
            t.printStackTrace();
            return Optional.empty();
        }
    }


    public static void writeModel(Model model, DataOutputStream output){
        try{
            if(model == null){
                output.writeLong(0);
                return;
            }
            byte[] bytes = model.getBytes();
            output.writeLong(bytes.length);
            output.writeLong(model.getType());
            output.write(bytes);
        }
        catch (Throwable t){
            System.out.println("Error Serializing model");
            t.printStackTrace();
        }
    }

    public static void writeServingInfo(ModelServingInfo servingInfo, DataOutputStream output){
        try{
            if(servingInfo == null) {
                output.writeLong(0);
                return;
            }
            output.writeLong(5);
            output.writeUTF(servingInfo.getDescription());
            output.writeUTF(servingInfo.getName());
            output.writeDouble(servingInfo.getDuration());
            output.writeLong(servingInfo.getInvocations());
            output.writeLong(servingInfo.getMax());
            output.writeLong(servingInfo.getMin());
            output.writeLong(servingInfo.getSince());
        }
        catch (Throwable t){
            System.out.println("Error Serializing servingInfo");
            t.printStackTrace();
        }
    }
}
