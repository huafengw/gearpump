import com.typesafe.sbt.SbtPgp.autoImport._
import de.johoop.jacoco4sbt.JacocoPlugin.jacoco
import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin._
import xerial.sbt.Sonatype._

import scala.collection.immutable.Map.WithDefault

object Build extends sbt.Build {

  class DefaultValueMap[+B](value : B) extends WithDefault[String, B](null, (key) => value) {
    override def get(key: String) = Some(value)
  }

  /**
   * deploy can recognize the path
   */
  val travis_deploy = taskKey[Unit]("use this after sbt assembly packArchive, it will rename the package so that travis deploy can find the package.")
  
  val akkaVersion = "2.3.6"
  val kryoVersion = "0.3.2"
  val codahaleVersion = "3.0.2"
  val commonsCodecVersion = "1.6"
  val commonsHttpVersion = "3.1"
  val commonsLangVersion = "3.3.2"
  val commonsLoggingVersion = "1.1.3"
  val commonsIOVersion = "2.4"
  val findbugsVersion = "2.0.1"
  val guavaVersion = "15.0"
  val dataReplicationVersion = "0.7"
  val hadoopVersion = "2.5.1"
  val jgraphtVersion = "0.9.0"
  val json4sVersion = "3.2.10"
  val kafkaVersion = "0.8.2.0"
  val sigarVersion = "1.6.4"
  val slf4jVersion = "1.7.7"
  val uPickleVersion = "0.2.5"
  
  val scalaVersionMajor = "scala-2.11"
  val scalaVersionNumber = "2.11.5"
  val sprayVersion = "1.3.2"
  val sprayJsonVersion = "1.3.1"
  val scalaTestVersion = "2.2.0"
  val scalaCheckVersion = "1.11.3"
  val mockitoVersion = "1.10.8"
  val bijectionVersion = "0.7.0"

  val gearPumpVersion = "0.2.4-SNAPSHOT"

  val commonSettings = Defaults.defaultSettings ++ Seq(jacoco.settings:_*) ++ sonatypeSettings  ++ net.virtualvoid.sbt.graph.Plugin.graphSettings ++
    Seq(
        resolvers ++= Seq(
          "patriknw at bintray" at "http://dl.bintray.com/patriknw/maven",
          "maven-repo" at "http://repo.maven.apache.org/maven2",
          "maven1-repo" at "http://repo1.maven.org/maven2",
          "maven2-repo" at "http://mvnrepository.com",
          "sonatype" at "https://oss.sonatype.org/content/repositories/releases",
          "bintray/non" at "http://dl.bintray.com/non/maven",
          "clockfly" at "http://dl.bintray.com/clockfly/maven",
          "sonatype snapshot" at "https://oss.sonatype.org/content/repositories/snapshots"
        )
    ) ++
    Seq(
      scalaVersion := scalaVersionNumber,
      version := gearPumpVersion,
      organization := "com.github.intel-hadoop",
      parallelExecution in Test := false,
      parallelExecution in ThisBuild := false,
      useGpg := false,
      pgpSecretRing := file("./secring.asc"),
      pgpPublicRing := file("./pubring.asc"),
      scalacOptions ++= Seq("-Yclosure-elim","-Yinline"),
      publishMavenStyle := true,

      pgpPassphrase := Option(System.getenv().get("PASSPHRASE")).map(_.toArray),
      credentials += Credentials(
                   "Sonatype Nexus Repository Manager", 
                   "oss.sonatype.org", 
                   System.getenv().get("SONATYPE_USERNAME"), 
                   System.getenv().get("SONATYPE_PASSWORD")),
      
      pomIncludeRepository := { _ => false },

      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
      
      pomExtra := {
      <url>https://github.com/intel-hadoop/gearpump</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/intel-hadoop/gearpump</connection>
        <developerConnection>scm:git:git@github.com:intel-hadoop/gearpump</developerConnection>
        <url>github.com/intel-hadoop/gearpump</url>
      </scm>
      <developers>
        <developer>
          <id>gearpump</id>
          <name>Gearpump Team</name>
          <url>https://github.com/intel-hadoop/teams/gearpump</url>
        </developer>
      </developers>
    }
  ) 

  val coreDependencies = Seq(
        libraryDependencies ++= Seq(
        "com.github.intel-hadoop" %% "gearpump-core" % gearPumpVersion % "provided",
        "com.github.intel-hadoop" %% "gearpump-core" % gearPumpVersion % "test" classifier "tests",
        "com.github.intel-hadoop" %% "gearpump-streaming" % gearPumpVersion % "provided",
        "com.github.intel-hadoop" %% "gearpump-streaming" % gearPumpVersion % "test" classifier "tests",
        "com.github.intel-hadoop" %% "gearpump-external-kafka" % gearPumpVersion % "provided",
        "com.github.intel-hadoop" %% "gearpump-external-kafka" % gearPumpVersion % "test" classifier "tests",
        "org.apache.hadoop" % "hadoop-common" % hadoopVersion % "provided",
        "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion,
        "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
        "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
        "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test",
        "org.mockito" % "mockito-core" % mockitoVersion % "test",
        ("org.apache.kafka" %% "kafka" % kafkaVersion classifier("test")) % "test"
      )
  )

  val myAssemblySettings = assemblySettings ++ Seq(
    test in assembly := {},
    assemblyOption in assembly ~= { _.copy(includeScala = false) }
  )

  lazy val root = Project(
    id = "gearpump-examples",
    base = file("."),
    settings = commonSettings ++ coreDependencies ++ myAssemblySettings
  ) aggregate (wordcount, complexdag, sol, fsio, examples_kafka)

  lazy val examples_kafka = Project(
    id = "gearpump-examples-kafka",
    base = file("kafka"),
    settings = commonSettings ++ coreDependencies
  )

  lazy val fsio = Project(
    id = "gearpump-examples-fsio",
    base = file("fsio"),
    settings = commonSettings ++ coreDependencies
  )

  lazy val sol = Project(
    id = "gearpump-examples-sol",
    base = file("sol"),
    settings = commonSettings ++ coreDependencies
  )

  lazy val wordcount = Project(
    id = "gearpump-examples-wordcount",
    base = file("wordcount"),
    settings = commonSettings ++ coreDependencies
  )

  lazy val complexdag = Project(
    id = "gearpump-examples-complexdag",
    base = file("complexdag"),
    settings = commonSettings ++ coreDependencies
  )

//  lazy val distributedshell = Project(
//    id = "gearpump-experiments-distributedshell",
//    base = file("experiments/distributedshell"),
//    settings = commonSettings
//  )
}
