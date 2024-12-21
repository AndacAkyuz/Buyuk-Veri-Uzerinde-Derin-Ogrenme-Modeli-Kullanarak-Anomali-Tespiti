import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import java.util.Properties
import scala.util.Random

object LSTMKafkaDataGenerator {
  def main(args: Array[String]): Unit = {
    val kafkaBootstrapServers = "localhost:9092"
    val topic = "generated"

    // Kafka producer ayarları
    val producerProps = new Properties()
    producerProps.put("bootstrap.servers", kafkaBootstrapServers)
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](producerProps)
    val random = new Random()

    // Normal ve anomali veri üretim parametreleri
    val numFeatures = 21 // Dim_0'dan Dim_20'ye kadar toplam 21 sütun
    val normalRange = (0.0, 1.0) // Normal değer aralığı

    def generateNormalData: String = {
      val features = (0 until numFeatures).map(_ =>
        BigDecimal(normalRange._1 + (normalRange._2 - normalRange._1) * random.nextDouble())
          .setScale(6, BigDecimal.RoundingMode.HALF_UP).toString
      ).mkString(",")
      s"$features,0" // class değeri: 0 (normal)
    }

    def generateAnomalousData: String = {
      val features = (0 until numFeatures).map { i =>
        if (random.nextInt(5) == 0) { // %20 olasılıkla anomali üret
          BigDecimal(1.5 + random.nextDouble() * 0.5).setScale(6, BigDecimal.RoundingMode.HALF_UP).toString
        } else {
          BigDecimal(normalRange._1 + (normalRange._2 - normalRange._1) * random.nextDouble())
            .setScale(6, BigDecimal.RoundingMode.HALF_UP).toString
        }
      }.mkString(",")
      s"$features,1" // class değeri: 1 (anomaly)
    }

    // Kafka'ya veri gönderimi
    for (i <- 1 to 100) {
      val dataType = if (random.nextBoolean()) "anomalies" else "normal_data"
      val features = dataType match {
        case "anomalies" => generateAnomalousData
        case "normal_data" => generateNormalData
      }

      val record = new ProducerRecord[String, String](topic, null, features)
      producer.send(record)

      println(s"Gönderilen veri => Topic: $topic | Tür: $dataType | Değerler: $features")
    }

    producer.close()
    println("Kafka benzer verilerin üretimi tamamlandı.")
  }
}

