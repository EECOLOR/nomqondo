ScoverageSbtPlugin.instrumentSettings

name := Shared.name("validation")

libraryDependencies ++= Seq(
  "org.specs2" %% "specs2" % "2.2.3" % "test",
  "com.chuusai" % "shapeless" % "2.0.0-M1" cross CrossVersion.full
)

resolvers ++= Seq(
  "Sonatype OSS Releases"  at "http://oss.sonatype.org/content/repositories/releases/",
  "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
)
