lazy val root = (project in file(".")).enablePlugins(PlayScala)

name := "akka-stocks-java8"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  javaWs,
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.3" % "test"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

LessKeys.compress := true

initialize := {
  val _ = initialize.value
  if (sys.props("java.specification.version") != "1.8")
    sys.error("Java 8 is required for this project.")
}
