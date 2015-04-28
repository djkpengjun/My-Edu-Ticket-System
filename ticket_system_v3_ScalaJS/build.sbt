import sbt.Project.projectToRef

lazy val jsProjects = Seq(js)
lazy val scalaV = "2.11.5"

lazy val jvm = (project in file("Ticket-Server")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  scalaJSProjects := jsProjects,
  pipelineStages := Seq(scalaJSProd, gzip),
  libraryDependencies ++= Seq(
    jdbc,
    anorm,
    cache,
    ws,
    "org.webjars" %% "webjars-play" % "2.3.0-2",
    "org.webjars" % "bootstrap" % "3.1.1-2",
    "org.webjars" % "jquery" % "1.11.1",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
    "com.vmunier" %% "play-scalajs-scripts" % "0.2.1"
  )
).enablePlugins(PlayScala).aggregate(jsProjects.map(projectToRef): _*)

lazy val js = (project in file("Ticket-Client")).settings(
  scalaVersion := scalaV,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.0"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay)

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project jvm", _: State)) compose (onLoad in Global).value