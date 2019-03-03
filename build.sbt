import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker._
import com.typesafe.sbt.packager.SettingsHelper._

organization := "vevo"

name := "var-svc"

scalaVersion := "2.11.8"

import TodoListPlugin._

compileWithTodolistSettings

testWithTodolistSettings

lazy val root = (project in file("."))
  .configs(IntegrationTest).settings(Defaults.itSettings: _*)
  .enablePlugins(
    BuildInfoPlugin,
    JavaAppPackaging,
    DockerPlugin,
    UniversalPlugin).settings(
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      buildInfoBuildNumber),
    buildInfoPackage := "info",
    buildInfoOptions ++= Seq(BuildInfoOption.BuildTime, BuildInfoOption.ToJson)
  )

mainClass in (Compile) := Some("Bootstrap")

mappings in Universal ++= Seq(
  findJarFromUpdate("aspectjweaver", update.value) ->
    "aspectj/aspectjweaver.jar"
)

scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-explaintypes",
  "-encoding", "UTF-8",
  "-feature",
  "-Xlog-reflective-calls",
  "-Ywarn-unused",
  //"-Ylog-classpath", // show me the classpath
  "-Ywarn-value-discard",
  "-Xlint",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  "-Xfuture",
  "-language:postfixOps",
  "-language:implicitConversions"
)

resolvers ++= Seq(
   Resolver.sonatypeRepo("snapshots"),
  "tpolecat" at "http://dl.bintray.com/tpolecat/maven",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "vevo.release" at "http://nexus.vevodev.com/content/repositories/releases"
)

libraryDependencies ++= {
  object V {
    val specs2 = "3.7"
    val kamon = "0.6.6"
    val scalacheck = "1.13.2"
    val http4s = "0.15.5a"
    val circe = "0.6.1"
    val doobie = "0.4.0"
    val genesisToken = "1.2.9"
  }
  Seq(
    "com.github.cb372" %% "scalacache-guava" % "0.9.3",
    "org.scalaz" %% "scalaz-core" % "7.2.9",
    "com.vevo.genesis.module" % "tokens" % V.genesisToken
      exclude("com.vevo.genesis", "core"),
    "org.http4s" %% "http4s-blaze-server" % V.http4s ,
    "org.http4s" %% "http4s-blaze-client" % V.http4s,
    "org.http4s" %% "http4s-dsl" % V.http4s,
    "org.http4s" %% "http4s-circe" % V.http4s,
    "io.circe" %% "circe-core" % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-parser" % V.circe,
    "io.circe" %% "circe-optics" % V.circe,
    "org.tpolecat"   %% "doobie-core" % V.doobie,
    "org.tpolecat"   %% "doobie-hikari" % V.doobie,
    "org.tpolecat"   %% "doobie-specs2" % V.doobie,
    "mysql" % "mysql-connector-java" % "5.1.38",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    "com.typesafe" % "config" % "1.2.1",
    "com.h2database" % "h2" % "1.3.175" % "it, test",
    "org.specs2" %% "specs2-core" % V.specs2 % "it, test",
    "org.specs2" %% "specs2-scalacheck" % V.specs2 % "it, test",
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % "1.1.3" % "it, test",
    "com.lihaoyi" % "ammonite" % "0.8.0" % "test" cross CrossVersion.full,
    "io.kamon" %% "kamon-core" % V.kamon
      exclude("com.typesafe.akka", "akka-actor_2.11"),
    "io.kamon" %% "kamon-datadog" % V.kamon,
    "io.kamon" %% "kamon-statsd" % V.kamon,
    "io.kamon" %% "kamon-log-reporter" % V.kamon,
    "io.kamon" %% "kamon-system-metrics" % V.kamon,
    "org.aspectj" % "aspectjweaver" % "1.8.9"
)}

parallelExecution in Test := false

publishMavenStyle := true

buildInfoKeys += buildInfoBuildNumber

buildInfoOptions += BuildInfoOption.BuildTime

dockerBaseImage := "openjdk:jdk"

dockerCommands += Cmd("ENV", "LANG en_US.UTF-8")

dockerExposedPorts := Seq(9000, 9443)

maintainer in Docker := "admin@vevo.com"

version in Docker := version.value +
  "-b" + sys.props.getOrElse("build_number", default = "dev")

dockerRepository := Some("vevo")

dockerCommands ++= Seq(
)

dockerUpdateLatest := true

Seq(bintrayResolverSettings: _*)

deploymentSettings

publish <<= publish.dependsOn(publish in config("universal"))

coverageEnabled.in(Test, test) := true

coverageMinimum := 50

coverageFailOnMinimum := false

javaOptions += "-Xmx1G"

javaOptions in Universal ++= Seq(
  "-J-Xmx1G",
  "-J-Xms640M",
  "-XX:+PrintFlagsFinal",
  "-XX:+PrintGCDetails",
  s"""-Dbuild_number=${sys.props.getOrElse("build_number", default = "000")}""" )

bashScriptExtraDefines ++= Seq("addJava -javaagent:${app_home}/../aspectj/aspectjweaver.jar")

javaOptions in Universal += s"-Dkamon.auto-start=true"

def findJarFromUpdate(jarName: String, report: UpdateReport): File = {
  val filter = artifactFilter(name = jarName + "*", extension = "jar")
  val matches = report.matching(filter)
  if (matches.isEmpty) {
    val err: (String => Unit) = System.err.println
    err("can’t find jar file in resources named " + jarName)
    err("unfiltered jar list:")
    report.matching(artifactFilter(extension = "jar")).foreach(x => err(x.toString))
    throw new ResourcesException("can’t find jar file in resources named " + jarName)
  } else {
    matches.head
  }
}
