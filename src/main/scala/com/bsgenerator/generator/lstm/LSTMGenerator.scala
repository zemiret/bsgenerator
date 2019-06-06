package com.bsgenerator.generator.lstm

import com.bsgenerator.adapters.NdArrayAdapter
import com.bsgenerator.generator.Generator
import com.bsgenerator.model.Article
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.{BackpropType, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import scala.util.Random

class LSTMGenerator extends Generator {
  val lstmLayerSize = 200
  val inputLength = 1000 // TODO: tweak
  val tbpttLength = 50
  val batches = 32
  val numEpochs = 5
  val generateSamplesEveryNMinibatches = 10
  val seed = 443222
  val rng = new Random(seed)
  var characters: List[Char] = ArticleCharacterLevelIterator.characterSet().toList
  val charactersLength: Int = characters.size


  //TODO: UI

  //Set up network configuration:
  val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
    .seed(seed)
    .l2(0.0001)
    .weightInit(WeightInit.XAVIER)
    .updater(new Adam(0.006))
    .list
    .layer(new LSTM.Builder()
      .nIn(charactersLength)
      .nOut(lstmLayerSize)
      .activation(Activation.TANH).build)
    .layer(new LSTM.Builder()
      .nIn(lstmLayerSize)
      //.dropout(?)
      .nOut(lstmLayerSize)
      .activation(Activation.TANH).build)
    .layer(new RnnOutputLayer.Builder(LossFunction.MCXENT)
      .activation(Activation.SOFTMAX)
      .nIn(lstmLayerSize).nOut(charactersLength).build)
    .backpropType(BackpropType.TruncatedBPTT)
    .tBPTTForwardLength(tbpttLength)
    .tBPTTBackwardLength(tbpttLength).build

  val net = new MultiLayerNetwork(conf)
  net.init()
  net.setListeners(new ScoreIterationListener(1))

  override def train(corpus: Set[Article]): Unit = {
    val iter = new ArticleCharacterLevelIterator(batches, inputLength, corpus.toSeq)
    for (i <- 0 to numEpochs) {
      var counter = 0
      while (iter.hasNext) {
        val ds = iter.next()
        net.fit(ds)
        counter += 1

        if (counter % 10 == 0) {
          print(f"Epoch $i batch $counter, samples:")
          for (elem <- generateChars(500, 3, iter)) {
            print(elem)
          }
        }
      }
    }
  }

  private def generateChars(charsToGen: Int, samples: Int, iter: ArticleCharacterLevelIterator): Array[String] = {
    val primer = String.valueOf(characters(rng.nextInt(charactersLength)))
    val initMatrix = Nd4j.zeros(samples, charactersLength, primer.length)

    // One hot encoding
    for ((ch, i) <- primer.zipWithIndex) {
      val idx = iter.charToIdx(ch)
      for (sample <- 0 until samples) {
        initMatrix.putScalar(Array[Int](sample, idx, i), 1.0f)
      }
    }

    net.rnnClearPreviousState()
    var out = net.rnnTimeStep(initMatrix)
    out = out.tensorAlongDimension(out.size(2) - 1, 1, 0) // previous timestep

    val stringBuilders: Array[StringBuilder] = new Array[StringBuilder](samples)
    for (str <- stringBuilders.indices) stringBuilders(str) = new StringBuilder()

    for (_ <- 0 until charsToGen) {
      val next = NdArrayAdapter.zeros(samples, charactersLength)
      for (sample <- 0 until samples) {
        val probabilityDistribution: Array[Double] = new Array[Double](charactersLength)
        for (charIdx <- 0 until charactersLength) probabilityDistribution(charIdx) = NdArrayAdapter.getDouble(out, sample, charIdx);
        val nextChar = getRandomCharIdFromDistribution(probabilityDistribution)
        next.putScalar(Array[Int](sample, nextChar), 1.0f)
        stringBuilders(sample).append(iter.idxToChar(nextChar))
      }
    }
    stringBuilders.map(_.mkString)
  }

  override def generate(words: Int): String = {
    val initMatrix = Nd4j.zeros()
    ""
  }

  private def getRandomCharIdFromDistribution(distribution: Array[Double]): Int = {
    for (_ <- 0 to 10) {
      val lookup = rng.nextDouble()
      var sum = 0.0
      for ((prob, i) <- distribution.zipWithIndex) {
        sum += prob
        if (sum > lookup) return i
      }
      // unlikely, but possible due to rounding errors
    }
    throw new RuntimeException("Invalid distribution or very unlucky sampling.")
  }
}
