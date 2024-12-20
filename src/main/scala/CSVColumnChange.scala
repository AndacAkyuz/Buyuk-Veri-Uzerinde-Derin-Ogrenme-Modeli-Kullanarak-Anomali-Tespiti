import org.apache.spark.sql.SparkSession
import scala.sys.process._

object CSVColumnChange {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Kafka Anomaly Sender")
      .master("local[*]")
      .getOrCreate()

    // Veri etiketlerini değiştirme işlemi kaldırıldı, Python betiği üzerinden yapılacak
    val originalDataPath = "kafka_output/generated_data.csv"

    // Python betiğini çağırma
    val pythonExecutable = "python"
    val pythonScript = "csv_processing.py"
    val outputCsvPath = "processed_output.csv"

    val pythonCommand = Seq(pythonExecutable, pythonScript, originalDataPath, outputCsvPath)
    val pythonResult = pythonCommand.!

    if (pythonResult != 0) {
      println("CSV islem Python betigi basarisiz oldu!")
      sys.exit(1)
    } else {
      println("CSV isleme Python betigi basariyla calistirildi.")
    }

    spark.stop()
  }
}