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
package org.scalaml.scalability.akka.message

import org.scalaml.Predef._

/**
 * Generic message exchanged between a master and worker actors.
 * @param id  Unique identifier for this message.
 * @author Patrick Nicolas
 * @since 0.98 March 28, 2014
 * @version 0.99.2
 * @note Scala for Machine learning Chapter 16 Scalable Framework / Akka / Master-workers
 */
sealed abstract class Message(val id: Int)


/**
 * Message sent to the master to initialize the computation.
 * @param i unique identifier for this message.
 * @author Patrick Nicolas
 * @since 0.98 March 28, 2014
 * @version 0.99.2
 * @note Scala for Machine learning Chapter 16 Scalable Framework / Akka / Master-workers
 */
case class Start(i: Int = 0) extends Message(i)

/**
 * Message sent by the worker actors to notify the master their tasks is completed.
 * @param i unique identifier for this message.
 * @param xt time series transformed (or processed)
 * @author Patrick Nicolas
 * @since 0.98 March 28, 2014
 * @version 0.99.2
 * @note Scala for Machine learning Chapter 16 Scalable Frameworks / Akka / Master-workers
 */
case class Completed(i: Int, xt: DblVec) extends Message(i)

/**
 * Message sent by the master to the worker actors to start the computation.
 * @param i unique identifier for this message.
 * @param xt time series to transform (or process)
 * @author Patrick Nicolas
 * @since 0.98 March 28, 2014
 * @version 0.99.2
 * @note Scala for Machine learning Chapter 16 Scalable Frameworks / Akka / Master-workers
 */
case class Activate(i: Int, xt: DblVec) extends Message(i)

// ---------------------------------  EOF -------------------------