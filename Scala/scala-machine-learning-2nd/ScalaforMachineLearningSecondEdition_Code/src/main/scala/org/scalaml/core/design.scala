/**
 * Copyright (c) 2013-2017  Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License") you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the
 * concepts and algorithms presented in "Scala for Machine Learning 2nd edition".
 * ISBN: 978-1-783355-874-2 Packt Publishing.
 *
 * Version 0.99.2
 */
package org.scalaml.core

import java.text.SimpleDateFormat

/**
 * Encapsulate the high level trait used for supervised and unsupervised learning
 * algorithms.
 *
 * @author Patrick Nicolas
 * @since 0.98.1 March 4, 2014
 * @version 0.99.2
 * @see Scale for Machine Learning Chapter 2 "Data Pipelines"
 */
private[scalaml] object Design {
  import org.scalaml.util.FileUtils

  /**
   * Define the configuration trait used in the classifiers and optimizers. The configuration
   * parameters is loaded using ''Config.read'' method invoked by one of the constructor of
   * the configuration. The configuration parameters are saved into file by overriding the
   * method '''>>'''.
   *
   * @author Patrick Nicolas
   * @since April 11, 2014 (0.98.2)
   * @version 0.99.2
   * @see Scale for Machine Learning Chapter 2 Hello World!
   */
  trait Config {

    /**
     * Write the configuration parameters associated to this object into a file
     * @param content to write into a file
     * @return true if the write operation is successful, false otherwise
     */
    protected def write(content: String): Boolean =
      FileUtils.write(content, Config.RELATIVE_PATH, getClass.getSimpleName)

    /**
     * Write the configuration parameters associated to this object.
     * @return true if the write operation is successful, false otherwise
     */
    def >> : Boolean = false
  }

  case class ConfigInt(iParam: Int) extends Config
  case class ConfigDouble(fParam: Double) extends Config
  case class ConfigString(sParam: String) extends Config
  case class ConfigDataFormat(dateParam: SimpleDateFormat) extends Config
  case class DataSourceConfig(
    pathName: String,
    normalize: Boolean,
    reverseOrder: Boolean,
    headerLines: Int = 1
  ) extends Config

  /**
   * Companion singleton to the Config trait. It is used to define the simple read
   * method to load the config parameters from file and instantiate the configuration
   * @author Patrick Nicolas
   * @since April 11, 2014 (0.98)
   * @version 0.99.2
   * @see Scale for Machine Learning Chapter 2 Hello World!
   */
  object Config {
    private val RELATIVE_PATH = "configs/"
    /**
     * Read this algorithm configuration parameters from a file defined as
     * '''configs/className'''
     * @param className  file containing the configuration parameters
     * @return Configuration parameters as a comma delimited field of string  if successful,
     * None otherwise
     */
    def read(className: String): Option[String] = FileUtils.read(RELATIVE_PATH, className)
  }

  /**
   * Define the model trait for classification and optimization algorithms.
   * @author Patrick Nicolas
   * @since 0.98.2 March 4, 2014 (0.98.2)
   * @see Scala for Machine Learning Chapter 2 Hello World!
   */
  trait Model[T] {
    /**
     * Write the model parameters associated to this object into a file
     * @param content to write into a file
     * @return true if the write operation is successful, false otherwise
     */
    protected def write(content: String): Boolean =
      FileUtils.write(content, Model.RELATIVE_PATH, getClass.getSimpleName)

    /**
     * This operation or method has to be overwritten for a model to be saved into a file
     * @return It returns true if the model has been properly saved, false otherwise
     */
    def >> : Boolean = false
  }

  /**
   * Companion singleton to the Model trait. It is used to define the simple read
   * method to load the model parameters from file.
   * @author Patrick Nicolas
   * @since 0.98 March 4, 2014 0.98.2
   * @see Scala for Machine Learning Chapter 2 Hello World!
   */
  object Model {
    private val RELATIVE_PATH = "models/"
    /**
     * Read this model parameters from a file defined as '''models/className'''
     * @param className  file containing the model parameters
     * @return Model parameters as a comma delimited string if successful, None otherwise
     */
    def read(className: String): Option[String] = FileUtils.read(RELATIVE_PATH, className)
  }
}
// --------------------------------------------------  EOF ----------------------------------------