package com.lightbend.java.model;

import java.io.Serializable;
import com.lightbend.model.Modeldescriptor.ModelDescriptor;

/**
 * Metadata about the current model.
 * Created by boris on 6/28/17.
 */
public class CurrentModelDescriptor implements Serializable {

    private String name;
    private String description;
    private ModelDescriptor.ModelType modelType;
    private byte[] modelData;
    private String modelDataLocation;
    private String dataType;

    public CurrentModelDescriptor(String name, String description, ModelDescriptor.ModelType modelType,
                                  byte[] dataContent, String modelDataLocation, String dataType){
        this.name = name;
        this.description = description;
        this.modelType = modelType;
        this.modelData = dataContent;
        this.modelDataLocation = modelDataLocation;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ModelDescriptor.ModelType getModelType() {
        return modelType;
    }

    public String getDataType() {
        return dataType;
    }

    public byte[] getModelData() {
        return modelData;
    }

    public String getModelDataLocation() {
        return modelDataLocation;
    }

    @Override
    public String toString() {
        return "ModelToServe{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", modelType=" + modelType +
                ", dataType='" + dataType + '\'' +
                '}';
    }
}
