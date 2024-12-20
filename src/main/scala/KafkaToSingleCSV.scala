import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.hadoop.fs.{FileSystem, Path}
import java.net.URI

object KafkaToSingleCSV {
  def main(args: Array[String]): Unit = {
    // SparkSession ve StreamingContext oluşturuluyor
    val spark = SparkSession.builder()
      .appName("Kafka to Single CSV")
      .master("local[*]")
      .getOrCreate()

    val streamingContext = new StreamingContext(spark.sparkContext, Seconds(5))

    // Kafka ayarları
    val kafkaParams = Map(
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "kafka-to-csv-group",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> "false"
    )

    val topics = Array("generated") // Kafka topic tanımı
    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    stream.map(record => record.value()).foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        import spark.implicits._

        val data = rdd.toDF("value") // Kafka mesajını DataFrame'e dönüştürme

        val tempOutputPath = "temp_output" // Geçici klasör
        val finalOutputPath = "kafka_output/generated_data.csv" // Nihai dosya yolu

        // Veriyi geçici klasöre tek partition olarak yaz
        data.coalesce(1)
          .write
          .mode("overwrite")
          .option("header", "true")
          .csv(tempOutputPath)

        // Geçici klasördeki part dosyasını bul ve yeniden adlandır
        val fs = FileSystem.get(new URI("file:///"), spark.sparkContext.hadoopConfiguration)
        val tempDir = new Path(tempOutputPath)
        val files = fs.listStatus(tempDir)
        val csvFile = files.find(_.getPath.getName.endsWith(".csv")).map(_.getPath)

        csvFile.foreach { file =>
          fs.rename(file, new Path(finalOutputPath)) // Dosyayı yeniden adlandır
          println(s"Veri başarıyla yazıldı: $finalOutputPath")
        }

        // Geçici klasörü temizle
        fs.delete(tempDir, true)
      }
    }

    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
