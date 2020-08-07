name := "example-k8s-scala-operator-zio"
version := "0.1"
scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "1.0.0",
  "dev.zio" %% "zio-config" % "1.0.0-RC27",
  "io.fabric8" % "kubernetes-client" % "5.0.0-alpha-2"
)

dockerUpdateLatest := true
dockerUsername := Some("jwiii")
dockerRepository := deriveRegistry()
dockerBaseImage := "adoptopenjdk/openjdk11:debianslim-jre"
dockerExposedPorts := Nil

enablePlugins(JavaServerAppPackaging, DockerPlugin)

def deriveRegistry(): Option[String] =
  if (sys.env.exists {
        case ("MICROK8S", "1") => true
        case ("MICROK8S", "0") => false
        case ("MICROK8S", b)   => b.toBoolean
        case _                 => false
      }) Some("localhost:32000")
  else None
