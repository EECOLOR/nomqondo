import sbt._

object Shared {
  val organization = "org.qirx"
  // nomqondo is a zulu word
  def name(projectName:String):String = "nomqondo-" + projectName
}