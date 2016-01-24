/**
 * Copyright (C) 2009-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package akka

import sbt._
import sbt.Keys._
import java.io.File

object Publish extends AutoPlugin {

  val defaultPublishTo = settingKey[File]("Default publish directory")

  override def trigger = allRequirements

  override lazy val projectSettings = Seq(
    crossPaths := false,
    pomExtra := akkaPomExtra,
    publishTo := akkaPublishTo.value,
    credentials ++= akkaCredentials,
    organizationName := "Typesafe Inc.",
    organizationHomepage := Some(url("http://www.typesafe.com")),
    publishMavenStyle := true,
    pomIncludeRepository := { x => false },
    defaultPublishTo := crossTarget.value / "repository"
  )

  def akkaPomExtra = {
    <scm>
      <url>git@github.com:akka/akka-persistence-cassandra.git</url>
      <connection>scm:git:git@github.com:akka/akka-persistence-cassandra.git</connection>
    </scm>
  }

  private def akkaPublishTo = Def.setting {
    sonatypeRepo(version.value) orElse localRepo(defaultPublishTo.value)
  }

  private def sonatypeRepo(version: String): Option[Resolver] =
    Option(sys.props("publish.maven.central")) filter (_.toLowerCase == "true") map { _ =>
      val nexus = "https://oss.sonatype.org/"
      if (version endsWith "-SNAPSHOT") "snapshots" at nexus + "content/repositories/snapshots"
      else "releases" at nexus + "service/local/staging/deploy/maven2"
    }

  private def localRepo(repository: File) =
    Some(Resolver.file("Default Local Repository", repository))

  private def akkaCredentials: Seq[Credentials] =
    Option(System.getProperty("akka.publish.credentials", null)).map(f => Credentials(new File(f))).toSeq

}
