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
package org.scalaml.supervised

/**
 * This package object contains the classes implementing the different kernel functions
 * and support vector machines for classification, outlier detection and regression
 *
 * - Typical set of configuration parameters for Support Vector Machine '''SVMConfig'''
 *
 * - Configuration parameters for the support vector machine '''SVMExecution'''<
 *
 * - Model generated through the training of support vector machine '''SVMModel'''
 *
 * - Implementation of the support vector machines using the LIBSVM library '''SVM'''
 * @see This implementation uses the LIBSVM library:
 * ''http://www.csie.ntu.edu.tw/~cjlin/libsvm/''
 * @see Scala for Machine Learning - Chapter 12 ''Kernel models and support vector machines''
 */
package object svm {}
// ---------------------------------------  EOF -----------------------------------------