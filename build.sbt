name := Shared.name("root")

organization := Shared.organization

publishArtifact := false

lazy val root =
  project.in( file(".") )
  .aggregate(core, validation, play)

lazy val core = project

lazy val validation = project.dependsOn(core)

lazy val play = project.dependsOn(core)
