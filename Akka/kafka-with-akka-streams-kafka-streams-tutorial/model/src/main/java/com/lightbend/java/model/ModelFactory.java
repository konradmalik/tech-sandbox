package com.lightbend.java.model;

import java.util.Optional;


/**
 * Basic trait for a model factory.
 * Created by boris on 7/14/17.
 */
public interface ModelFactory {
    Optional<Model> create(CurrentModelDescriptor descriptor);
    Model restore(byte[] bytes);
}
