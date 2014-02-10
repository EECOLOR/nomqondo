name := Shared.name("play")

organization := Shared.organization

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.2.2",
  "org.specs2" %% "specs2" % "2.2.3" % "test"
)

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
