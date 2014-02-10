ScoverageSbtPlugin.instrumentSettings

name := Shared.name("core")

organization := Shared.organization

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.2.3" % "test",
  "com.chuusai" % "shapeless" % "2.0.0-M1" % "test" cross CrossVersion.full
)

resolvers ++= Seq(
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)
