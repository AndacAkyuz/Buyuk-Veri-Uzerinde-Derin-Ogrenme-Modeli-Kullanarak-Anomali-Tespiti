# Büyük Veri Analizi Projesi

Bu proje, büyük ölçekli veri setlerinde anomali tespiti yapmak amacıyla geliştirilmiştir. Apache Kafka ve Apache Spark gibi güçlü büyük veri araçları kullanılarak, gerçek zamanlı veri akışı ve analiz altyapısı oluşturulmuştur. Proje, karar ağaçları ve LSTM gibi makine öğrenimi modellerini kullanarak yüksek doğrulukta anomali tespiti sağlamaktadır.

## Proje Özeti

- **Amaç**: Büyük veri ortamında anomali tespiti yapmak.
- **Teknolojiler**:
  - Apache Spark
  - Apache Kafka
  - Scala
  - Python
- **Kullanılan Modeller**:
  - Karar Ağaçları
  - Uzun Kısa Süreli Bellek (LSTM)

## Kurulum

Proje ortamını oluşturmak için aşağıdaki adımları izleyin:

1. **Java 1.8 Kurulumu**:
   - Bilgisayarınızdaki mevcut Java sürümlerini kaldırın.
   - Gerekli Java sürümünü yükleyin ve `JAVA_HOME` ortam değişkenini ayarlayın.

2. **Apache Spark ve Scala Kurulumu**:
   - Apache Spark 2.3.1 sürümünü indirin.
   - Scala 2.11 sürümünü yükleyin.
   - Spark ve Scala kurulumlarının doğru çalıştığından emin olmak için `spark-shell` komutunu kullanın.

3. **Gerekli Kütüphaneler**:
   `build.sbt` dosyasına aşağıdaki bağımlılıkları ekleyin:
   ```scala
   libraryDependencies ++= Seq(
     "org.apache.spark" %% "spark-core" % "2.3.1",
     "org.apache.spark" %% "spark-sql" % "2.3.1",
     "org.apache.spark" %% "spark-mllib" % "2.3.1",
     "com.esotericsoftware" % "kryo" % "4.0.2",
     "org.plotly-scala" %% "plotly-almond" % "0.5.2",
     "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.3.1",
     "org.apache.kafka" % "kafka-clients" % "2.0.0",
     "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.3.1",
   )

4. **Python Ortamı**:
   - `pandas`, `numpy`, `matplotlib`, ve `scikit-learn` gibi kütüphaneleri yüklemek için:
     ```bash
     pip install pandas numpy matplotlib scikit-learn
     ```

5. **Kafka Kurulumu**:
   - Kafka'nın gerekli yapılandırmalarını tamamlayın.
   - `zookeeper` ile Kafka'yı başlatın.

## Veri Seti

- **Veri Seti**: `annthyroid_21feat_normalised.csv`
- Veri seti, özellik mühendisliği ve ölçeklendirme gibi ön işleme adımlarından geçirilmiştir.

## Uygulama

### Model Eğitimi:

- Karar Ağacı ve LSTM modelleri kullanılarak veri eğitimi yapılmıştır.
- Modelin performansı `RMSE`, `MAE`, ve `R²` gibi metriklerle değerlendirilmiştir.

### Gerçek Zamanlı Veri İşleme:

- Kafka üzerinden gelen veriler Spark Streaming ile işlenmiştir.
- Anomaliler ve normal veriler farklı Kafka topic'lerinde saklanmıştır.

### Görselleştirme:

- Python kullanılarak veri görselleştirme işlemleri gerçekleştirilmiştir.

### Sonuçlar:

- Model doğruluk oranları ve anomali tespit istatistikleri görseller ve raporlarla sunulmuştur.


