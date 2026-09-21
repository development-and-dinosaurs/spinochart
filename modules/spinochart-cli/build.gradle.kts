import com.vanniktech.maven.publish.DeploymentValidation

plugins {
  application
  alias(libs.plugins.vanniktech.maven.publish)
  `kotlin-convention`
  `spotless-convention`
  id("spinochart.publishing")
}

description = "CLI tool for generating SpinoChart reports from Gatling simulation logs"

dependencies {
  implementation(projects.modules.spinochartCore)
  implementation(libs.clikt)
  testImplementation(libs.kotest.assertions.core)
}

application {
  mainClass.set("uk.co.developmentanddinosaurs.spinochart.cli.SpinochartCliKt")
  applicationDefaultJvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED")
}

val fatJar =
    tasks.register<Jar>("fatJar") {
      group = "build"
      description =
          "Assembles a runnable standalone fat JAR containing SpinoChart CLI and all dependencies"
      archiveClassifier.set("all")
      manifest {
        attributes["Main-Class"] = "uk.co.developmentanddinosaurs.spinochart.cli.SpinochartCliKt"
      }
      from(sourceSets.main.get().output)
      dependsOn(configurations.runtimeClasspath)
      from({
        configurations.runtimeClasspath
            .get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
      })
      exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
      duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

val installCli =
    tasks.register("installCli") {
      group = "distribution"
      description =
          "Assembles a standalone self-executable SpinoChart binary in build/bin/spinochart"
      dependsOn(fatJar)
      val fatJarFile = fatJar.flatMap { it.archiveFile }
      val outputBinary = layout.buildDirectory.file("bin/spinochart")
      inputs.files(fatJarFile)
      outputs.file(outputBinary)
      doLast {
        val binFile = outputBinary.get().asFile
        binFile.parentFile.mkdirs()
        binFile.writeText(
            "#!/bin/sh\nexec java --add-opens=java.base/java.lang=ALL-UNNAMED -jar \"\$0\" \"\$@\"\n"
        )
        binFile.appendBytes(fatJarFile.get().asFile.readBytes())
        binFile.setExecutable(true, false)
      }
    }

tasks.named("assemble") { dependsOn(installCli) }

mavenPublishing {
  publishToMavenCentral(
      automaticRelease = true,
      validateDeployment = DeploymentValidation.VALIDATED,
  )
  signAllPublications()
  pom {
    name.set("SpinoChart CLI")
    description.set("Command-line interface for SpinoChart")
  }
}
