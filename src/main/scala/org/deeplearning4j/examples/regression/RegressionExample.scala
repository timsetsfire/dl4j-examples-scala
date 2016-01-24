package org.deeplearning4j.examples.regression

import org.canova.api.records.reader.RecordReader
import org.canova.api.records.reader.impl.CSVRecordReader
import org.canova.api.split.FileSplit
import org.deeplearning4j.datasets.canova.RecordReaderDataSetIterator
import org.deeplearning4j.datasets.iterator.DataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.conf.{Updater, GradientNormalization, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.dataset.{DataSet, SplitTestAndTrain}
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.springframework.core.io.ClassPathResource


object RegressionExample {

    def main(args: Array[String]) = {
        val seed = 123
        val iterations = 100
        val reader: RecordReader = new CSVRecordReader()
        reader.initialize(new FileSplit(new ClassPathResource("regression-example.txt").getFile))
        val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder().miniBatch(false)
                .weightInit(WeightInit.XAVIER)
                .seed(seed) // Seed to lock in weight initialization for tuning
                .iterations(iterations) // # training iterations predict/classify & backprop
                .updater(Updater.SGD).dropOut(0.5)
                .learningRate(1e-6f) // Optimization step size
                .optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT) // Backprop method (calculate the gradients)
                .list(1) // # NN layers (does not count input layer)
                .layer(0, new OutputLayer.Builder(LossFunctions.LossFunction.RMSE_XENT)
                                .nIn(12) // # input nodes
                                .nOut(1) // # output nodes
                                .activation("identity")
                                .weightInit(WeightInit.XAVIER)
                                .build()
                ) // NN layer type
                .build()
        val iter: DataSetIterator = new RecordReaderDataSetIterator(reader,null,2029,12,1,true)
        val next: DataSet = iter.next()
        next.normalizeZeroMeanZeroUnitVariance()
        val testAndTrain: SplitTestAndTrain = next.splitTestAndTrain(0.9)
        val network: MultiLayerNetwork = new MultiLayerNetwork(conf)
        network.init()
        network.setListeners(new ScoreIterationListener(1))
        network.fit(testAndTrain.getTrain)



    }

}
