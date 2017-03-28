
lazy val commonSettings: Seq[Setting[_]] = Seq(
  organization := "org.nlp4l",
  //javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  autoScalaLibrary := false,
  crossPaths := false
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "NLP4L-solr",
    version := "0.1.1",
    parallelExecution := true
  )

libraryDependencies ++= Seq(
  "org.apache.lucene" % "lucene-core" % "6.4.2",
  "org.apache.solr" % "solr-core" % "6.4.2",
  "junit" % "junit" % "4.11" % "test",
  "com.novocode" % "junit-interface" % "0.11-RC1" % "test",
  "com.typesafe" % "config" % "1.3.0"
)
