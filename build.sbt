name := """siz-api"""

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(spracebook)
  .aggregate(spracebook)

lazy val spracebook = uri("git://github.com/jdauphant/spracebook.git")

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.2.play24",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.kifi" %% "franz" % "0.3.10",
  "com.logentries" % "logentries-appender" % "1.1.30",
  "org.codehaus.janino" % "janino" % "2.7.8",
  specs2 % Test,
  ws,
  // la dependance com.kifi.franz en version 0.3.10" tire des dépendance mauvaises. On doit forcer celles qui sont en conflit.
  "org.scala-lang" % "scala-compiler" % "2.11.7",
  "org.scala-lang" % "scala-library" % "2.11.7",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  //webjars
  "org.webjars" % "swagger-ui" % "2.1.1"
)

dependencyOverrides +=
  // pareil pour sprong facebook
  "com.google.guava" % "guava" % "18.0"


scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

// Pidfile add problem when docker try to restart
javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

routesGenerator := InjectedRoutesGenerator

fork in Test := false