name := """tweet-engagement"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  ws,
  "org.scalatestplus" %% "play" % "1.1.0" % "test"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
