import org.apache.spark.ml.feature.{StandardScaler, VectorAssembler}
import org.apache.spark.ml.tuning.CrossValidatorModel
import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SparkSession}

object KafkaAnomalySender {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Kafka Anomaly Sender")
      .master("local[*]")
      .getOrCreate()

    // Kafka ayarları
    val kafkaBootstrapServers = "localhost:9092"

    // 1. Veri Setini Okuma
    val data = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("D:\\Scala Deneme\\proje\\untitled3\\annthyroid_21feat_normalised.csv")

    println("Veri seti:")
    data.show()

    // 2. Model Yükleme ve Tahmin
    val modelPath = "D:\\Scala Deneme\\proje\\untitled3\\decision_tree_classification_model"
    val loadedModel = CrossValidatorModel.load(modelPath)

    // Özellik sütunları
    val featureColumns = data.columns.filter(_ != "class")

    val assembler = new VectorAssembler()
      .setInputCols(featureColumns)
      .setOutputCol("features")

    val assembledData = assembler.transform(data)

    val scaler = new StandardScaler()
      .setInputCol("features")
      .setOutputCol("scaledFeatures")
      .setWithStd(true)
      .setWithMean(false)

    val scalerModel = scaler.fit(assembledData)
    val scaledData = scalerModel.transform(assembledData)

    val predictions = loadedModel.transform(scaledData)

    // 3. Anomali ve Normal Verileri Ayırma
    val anomalies = predictions.filter("prediction = 1.0").withColumn("topic", lit("anomalies"))
    val normalData = predictions.filter("prediction = 0.0").withColumn("topic", lit("normal_data"))

    println("Anomaliler:")
    anomalies.show()

    println("Normal Veriler:")
    normalData.show()

    // 4. Anomali Oranını Hesaplama
    val totalCount = predictions.count()
    val anomalyCount = anomalies.count()
    val anomalyRatio = anomalyCount.toDouble / totalCount

    println(s"Toplam Veri Sayısı: $totalCount")
    println(s"Anomali Sayısı: $anomalyCount")
    println(f"Anomali Oranı: $anomalyRatio%.4f")

    // 5. Kafka'ya Gönderme
    def sendToKafka(df: DataFrame, topic: String): Unit = {
      df.selectExpr("CAST(features AS STRING) AS key", "CAST(features AS STRING) AS value")
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", kafkaBootstrapServers)
        .option("topic", topic)
        .save()
    }

    sendToKafka(anomalies, "anomalies")
    sendToKafka(normalData, "normal_data")

    println("Veriler Kafka'ya başarıyla gönderildi.")

    spark.stop()
  }
}
