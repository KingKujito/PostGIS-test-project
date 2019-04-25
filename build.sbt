name := "PostGIS POC"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "org.scalikejdbc"          %%  "scalikejdbc"         % "3.3.2",
  "org.scalikejdbc"          %%  "scalikejdbc-config"  % "3.3.2",
  "ch.qos.logback"            %  "logback-classic"     % "1.2.3",
  "org.seleniumhq.selenium"   %  "selenium-server"     % "3.141.59",
  "postgresql"                %  "postgresql"          % "9.1-901-1.jdbc4",
  "org.scalikejdbc"          %%  "scalikejdbc-test"    % "3.3.2"   % "test"
)
