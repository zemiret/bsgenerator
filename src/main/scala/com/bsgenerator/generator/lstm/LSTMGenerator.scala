package com.bsgenerator.generator.lstm

import java.io.File

import com.bsgenerator.Config
import com.bsgenerator.adapters.NdArrayAdapter
import com.bsgenerator.generator.Generator
import com.bsgenerator.model.Article
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.{BackpropType, MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.api.TrainingListener
import org.deeplearning4j.optimize.listeners.{CheckpointListener, ScoreIterationListener}
import org.deeplearning4j.ui.api.UIServer
import org.deeplearning4j.ui.stats.StatsListener
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.nd4j.evaluation.classification.Evaluation
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Adam
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

import scala.util.Random

class LSTMGenerator extends Generator {
  val lstmLayerSize = 100
  val inputLength = 1000 // TODO: tweak
  val tbpttLength = 50
  val trainingPerc = 0.99
  val batches = 32
  val numEpochs = 500
  val generateSamplesEveryNMinibatches = 10
  val seed = 13132134
  val rng = new Random(seed)
  val charactersLength: Int = ArticleCharacterLevelIterator.validCharacters.size

  //Set up network configuration:
  val conf: MultiLayerConfiguration = new NeuralNetConfiguration.Builder()
    .seed(seed)
    .l2(0.0001)
    .weightInit(WeightInit.XAVIER)
    .updater(new Adam(0.004))
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


  val statsStore = new FileStatsStorage(new File(Config.config.getString("bsgenerator.net.statsfile")))
  if (Config.config.getBoolean("bsgenerator.net.ui")) {
    val uiServer: UIServer = UIServer.getInstance
    uiServer.attach(statsStore)
  }

  net.setListeners(new ScoreIterationListener(1))
  net.setListeners(new StatsListener(statsStore))

  val checkpoints = new File("./checkpoints")
  if (!checkpoints.exists()) {
    checkpoints.mkdirs()
  }

  val listeners: java.util.List[TrainingListener] = java.util.Arrays.asList(
    new ScoreIterationListener(1),
    new StatsListener(statsStore),
    new CheckpointListener.Builder(checkpoints).keepLastAndEvery(5, 3).saveEveryEpoch().build()
  )

  net.setListeners(listeners)

  private def splitCorpus(corpus: Set[Article]): (Set[Article], Set[Article]) = {
    val shuffled = scala.util.Random.shuffle(corpus)
    return (shuffled.take((corpus.size * trainingPerc).toInt), shuffled.takeRight((corpus.size * (1 - trainingPerc)).toInt))
  }

  override def train(corpus: Set[Article]): Unit = {
    val split = splitCorpus(corpus)
    val trainingData = new ArticleCharacterLevelIterator(batches, inputLength, split._1.toSeq)
    val evaluationData = new ArticleCharacterLevelIterator(batches, inputLength, split._2.toSeq)

    for (i <- 0 to numEpochs) {
      var counter = 0
      while (trainingData.hasNext) {
        val ds = trainingData.next()
        net.fit(ds)
        counter += 1

        if (counter % 10 == 0) {
          print(f"Epoch $i batch $counter, samples:")
          for (elem <- generateChars(500, 3)) {
            print(elem)
          }
        }
      }

      //evaluate every epoch
      var eval: Evaluation = net.evaluate(evaluationData)
      print(eval.stats())

      net.incrementEpochCount()
      trainingData.reset()
      evaluationData.reset()
    }
  }

  private def generateChars(charsToGen: Int, samples: Int): Array[String] = {
    val primer = String.valueOf(ArticleCharacterLevelIterator.randomCharacter(rng))
    val initMatrix = Nd4j.zeros(samples, charactersLength, primer.length)

    // One hot encoding
    for ((ch, i) <- primer.zipWithIndex) {
      val idx = ArticleCharacterLevelIterator.charToIdx(ch)
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
        for (charIdx <- 0 until charactersLength) probabilityDistribution(charIdx) = NdArrayAdapter.getDouble(out, sample, charIdx)
        val nextChar = getRandomCharIdFromDistribution(probabilityDistribution)
        next.putScalar(Array[Int](sample, nextChar), 1.0f)
        stringBuilders(sample).append(ArticleCharacterLevelIterator.idxToChar(nextChar))
      }
    }
    stringBuilders.map(_.mkString)
  }

  override def generate(words: Int): String = generateChars(words * 32, 1)(0).split(" ").take(words).mkString(" ")

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
