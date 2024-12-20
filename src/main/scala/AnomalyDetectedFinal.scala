import org.apache.spark.sql.functions.lit
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.sys.process._

object AnomalyDetectedFinal {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Kafka Anomaly Sender")
      .master("local[*]")
      .getOrCreate()

    // Veri yolu ve model yolu
    val pythonExecutable = "python"
    val pythonScript = "D:\\Scala Deneme\\proje\\untitled4\\predict_with_tf_model.py"
    val modelPath = "D:\\Scala Deneme\\proje\\untitled4\\lstm_model_smote.h5"
    val dataPath = "D:\\Scala Deneme\\proje\\untitled4\\processed_output.csv"

    val command = Seq(pythonExecutable, pythonScript, modelPath, dataPath)
    val result = command.!

    if (result != 0) {
      println("Python script execution failed!")
    } else {
      println("Python script executed successfully.")
    }

    command.!

    // Tahmin edilen veriyi yükleme
    val predictionsPath = "predictions.csv"
    val predictions = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(predictionsPath)

    println("Veri seti:")
    predictions.show()

    // Anomaliler ve normal veriler için işlem
    val anomalies = predictions.filter("predictions > 0.8").withColumn("topic", lit("anomalies"))
    val normalData = predictions.filter("predictions <= 0.2").withColumn("topic", lit("normal_data"))

    println("Anomaliler:")
    anomalies.show()

    println("Normal Veriler:")
    normalData.show()

    // Anomali Oranını Hesaplama
    val totalCount = predictions.count()
    val anomalyCount = anomalies.count()
    val anomalyRatio = anomalyCount.toDouble / totalCount

    println(s"Toplam Veri Sayısı: $totalCount")
    println(s"Anomali Sayısı: $anomalyCount")
    println(f"Anomali Oranı: $anomalyRatio%.4f")

    // Kafka'ya gönderme
    def sendToKafka(df: DataFrame, topic: String): Unit = {
      import spark.implicits._
      val jsonDF = df.drop("predictions", "topic") // "topic" sütununu da kaldırıyoruz
        .rdd.map(row => row.toSeq.map(_.toString).mkString("[", ",", "]"))
        .toDF("value")

      jsonDF.write
        .format("kafka")
        .option("kafka.bootstrap.servers", "localhost:9092")
        .option("topic", topic)
        .save()
    }

    sendToKafka(anomalies, "anomalies")
    sendToKafka(normalData, "normal_data")

    println("Veriler Kafka'ya başarıyla gönderildi.")

    spark.stop()
  }
}
