import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.DecisionTreeClassifier
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator, MulticlassClassificationEvaluator}
import org.apache.spark.ml.feature.{StandardScaler, StringIndexer, VectorAssembler, VectorIndexer}
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.sql.SparkSession

object RandomForest {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Decision Tree Classification with Metrics")
      .master("local[*]")
      .getOrCreate()

    // 1. CSV dosyasını okuma
    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("D:\\Scala Deneme\\proje\\untitled3\\annthyroid_21feat_normalised.csv")

    println("Orijinal veri:")
    df.show()

    // 2. Özellikleri ve Hedef Değişkeni Belirleme
    val featureColumns = df.columns.filter(_ != "class") // Tüm sütunlar dışında 'class'
    val labelColumn = "class"

    // 3. Özellik Sütunlarını Vektör Haline Getirme
    val assembler = new VectorAssembler()
      .setInputCols(featureColumns)
      .setOutputCol("features")

    val assembledDf = assembler.transform(df)

    // 4. Standartlaştırma
    val scaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("scaledFeatures")
      .setWithStd(true)
      .setWithMean(false)

    val scalerModel = scaler.fit(assembledDf)
    val scaledDf = scalerModel.transform(assembledDf)

    // 5. Veri Eğitim ve Test Olarak Bölme
    val Array(trainingData, testData) = scaledDf.randomSplit(Array(0.8, 0.2), seed = 1234L)

    // 6. Sınıflandırma için Veri Hazırlığı
    val labelIndexer = new StringIndexer()
      .setInputCol(labelColumn)
      .setOutputCol("label")
      .fit(scaledDf)

    val featureIndexer = new VectorIndexer()
      .setInputCol("scaledFeatures")
      .setOutputCol("indexedFeatures")
      .setMaxCategories(4)
      .fit(scaledDf)

    // 7. Karar Ağacı Sınıflandırıcı
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label")
      .setFeaturesCol("indexedFeatures")

    // 8. Pipeline
    val pipeline = new Pipeline()
      .setStages(Array(labelIndexer, featureIndexer, dt))

    // 9. Parametre Grid'i
    val paramGrid = new ParamGridBuilder()
      .addGrid(dt.maxDepth, Array(5, 10, 20))
      .addGrid(dt.minInstancesPerNode, Array(1, 2, 5))
      .build()

    // 10. Cross Validator
    val crossValidator = new CrossValidator()
      .setEstimator(pipeline)
      .setEvaluator(new MulticlassClassificationEvaluator()
        .setLabelCol("label")
        .setPredictionCol("prediction")
        .setMetricName("accuracy"))
      .setEstimatorParamMaps(paramGrid)
      .setNumFolds(5)

    // 11. Modeli Eğitme
    val cvModel = crossValidator.fit(trainingData)

    // 12. Tahmin ve Değerlendirme
    val predictions = cvModel.transform(testData)

    // 13. Metrikleri Hesaplama
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("label")
      .setPredictionCol("prediction")

    val accuracy = evaluator.setMetricName("accuracy").evaluate(predictions)
    val f1 = evaluator.setMetricName("f1").evaluate(predictions)
    val precision = evaluator.setMetricName("weightedPrecision").evaluate(predictions)
    val recall = evaluator.setMetricName("weightedRecall").evaluate(predictions)

    println(s"Accuracy: $accuracy")
    println(s"F1 Score: $f1")
    println(s"Precision: $precision")
    println(s"Recall: $recall")

    // 14. Binary Metrikler (ROC ve AUC)
    val binaryEvaluator = new BinaryClassificationEvaluator()
      .setLabelCol("label")
      .setRawPredictionCol("rawPrediction")
      .setMetricName("areaUnderROC")

    val auc = binaryEvaluator.evaluate(predictions)
    println(s"Area Under ROC (AUC): $auc")

    // 15. Confusion Matrix
    val confusionMatrix = predictions.groupBy("label", "prediction").count()
    println("Confusion Matrix:")
    confusionMatrix.show()

    // 16. Ezber Kontrolü (Eğitim Metrikleri)
    val trainPredictions = cvModel.transform(trainingData)
    val trainAccuracy = evaluator.setMetricName("accuracy").evaluate(trainPredictions)
    println(s"Training Accuracy (Overfitting Check): $trainAccuracy")

    spark.stop()
  }
}
