name := """siz-api"""

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .dependsOn(spracebook)
  .aggregate(spracebook)

lazy val spracebook = uri("git://github.com/jdauphant/spracebook.git")

resolvers ++= Seq(
  "Spray" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo"  % "0.11.2.play24",
  "org.mindrot"       %  "jbcrypt"              % "0.3m",
  "io.spray"          %% "spray-client"         % "1.3.2",
  "io.spray"          %% "spray-json"           % "1.3.1",
  "com.kifi"          %% "franz"                % "0.3.10",
  specs2              %   Test,
  ws
)

scalacOptions ++= Seq("-unchecked", "-deprecation","-feature")

// Pidfile add problem when docker try to restart
javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

routesGenerator := InjectedRoutesGenerator