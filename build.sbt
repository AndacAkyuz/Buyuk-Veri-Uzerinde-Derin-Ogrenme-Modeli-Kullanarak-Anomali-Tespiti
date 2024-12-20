ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.11.8"

lazy val root = (project in file("."))
  .settings(
    name := "untitled3"
  )
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

resolvers ++= Seq(
  "Maven Central" at "https://repo1.maven.org/maven2/"
)
