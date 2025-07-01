val ZioVersion             = "2.1.15"
val ZioHttpVersion         = "3.3.3"
val ZioJsonVersion         = "0.7.40"
val ZioConfigVersion       = "4.0.3"
val ZioLoggingVersion      = "2.3.0"
val LogbackVersion         = "1.4.11"
val ZioTestVersion         = "2.0.19"

lazy val root = (project in file("."))
  .enablePlugins(AssemblyPlugin)
  .settings(
    organization                     := "com.rockthejvm",
    name                             := "loadbalancer",
    scalaVersion                     := "3.3.6",
    libraryDependencies ++= Seq(
      "dev.zio"           %% "zio"                 % ZioVersion,
      "dev.zio"           %% "zio-http"            % ZioHttpVersion,
      "dev.zio"           %% "zio-json"            % ZioJsonVersion,
      "dev.zio"           %% "zio-config"          % ZioConfigVersion,
      "dev.zio"           %% "zio-config-typesafe" % ZioConfigVersion,
      "dev.zio"           %% "zio-config-magnolia" % ZioConfigVersion,
      "dev.zio"           %% "zio-config-typesafe" % ZioConfigVersion,
      "dev.zio"           %% "zio-logging"         % ZioLoggingVersion,
      "dev.zio"           %% "zio-logging-slf4j"   % ZioLoggingVersion,
      "ch.qos.logback"     % "logback-classic"     % LogbackVersion,
      "dev.zio"           %% "zio-test"            % ZioTestVersion         % Test,
      "dev.zio"           %% "zio-test-sbt"        % ZioTestVersion         % Test,
      "dev.zio"           %% "zio-http-testkit"    % ZioHttpVersion         % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "versions", "11", "module-info.class") => MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties")        => MergeStrategy.discard
      case "module-info.class"  => MergeStrategy.discard
      case x  => (assembly / assemblyMergeStrategy).value(x)
    },
    assembly / mainClass             := Some("Main"),
    assembly / assemblyJarName       := "lb.jar",
  )