package uk.co.developmentanddinosaurs.spinochart.plugin

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import org.gradle.testfixtures.ProjectBuilder

class SpinochartPluginSpec :
    BehaviorSpec({
      Given("a Gradle project applying the SpinoChart plugin") {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("uk.co.developmentanddinosaurs.spinochart")

        When("inspecting the registered extension and tasks") {
          val extension = project.extensions.findByType(SpinochartExtension::class.java)
          val task = project.tasks.findByName("spinochartReport") as? GenerateSpinochartReportTask

          Then("the extension should be present with default values") {
            extension shouldNotBe null
            extension!!.theme.get() shouldBe "spino"
            extension.outputFileName.get() shouldBe "index.html"
            extension.reportsDir.get().asFile.path shouldBe
                project.layout.buildDirectory.dir("reports/gatling").get().asFile.path
          }

          Then("the spinochartReport task should be registered with conventions") {
            task shouldNotBe null
            task!!.group shouldBe "reporting"
            task.theme.get() shouldBe "spino"
            task.outputFileName.get() shouldBe "index.html"
          }
        }

        When("a gatlingRun task is added to the project") {
          val gatlingTask = project.tasks.register("gatlingRun")
          val spinochartTask = project.tasks.getByName("spinochartReport")

          Then("gatlingRun should be finalized by spinochartReport") {
            gatlingTask
                .get()
                .finalizedBy
                .getDependencies(gatlingTask.get())
                .contains(spinochartTask) shouldBe true
          }
        }

        When("executing the task with multiple simulation logs") {
          val task = project.tasks.getByName("spinochartReport") as GenerateSpinochartReportTask
          val run1Dir = File(project.projectDir, "build/reports/gatling/run-1")
          val run2Dir = File(project.projectDir, "build/reports/gatling/run-2")
          run1Dir.mkdirs()
          run2Dir.mkdirs()

          val sampleBytes =
              SpinochartPluginSpec::class
                  .java
                  .getResourceAsStream("/sample-simulation.log")!!
                  .readBytes()
          File(run1Dir, "simulation.log").writeBytes(sampleBytes)
          File(run2Dir, "simulation.log").writeBytes(sampleBytes)

          task.reportsDir.set(project.layout.buildDirectory.dir("reports/gatling"))
          task.generate()

          Then("reports should be generated in every simulation run directory") {
            val report1 = File(run1Dir, "index.html")
            val report2 = File(run2Dir, "index.html")
            report1.shouldExist()
            report2.shouldExist()
            (report1.length() > 0) shouldBe true
            (report2.length() > 0) shouldBe true
          }

          Then("running generate again when reports are up to date should complete safely") {
            val report1Modified = File(run1Dir, "index.html").lastModified()
            task.generate()
            File(run1Dir, "index.html").lastModified() shouldBe report1Modified
          }
        }
      }

      Given("a functional build using GradleRunner") {
        val testProjectDir = kotlin.io.path.createTempDirectory("spinochart-runner-test").toFile()
        testProjectDir.deleteOnExit()

        testProjectDir
            .resolve("settings.gradle.kts")
            .writeText("rootProject.name = \"runner-test\"")
        testProjectDir
            .resolve("build.gradle.kts")
            .writeText(
                """
                plugins {
                  id("uk.co.developmentanddinosaurs.spinochart")
                }
                spinochart {
                  theme.set("classic")
                }
                """
                    .trimIndent()
            )

        val runDir = testProjectDir.resolve("build/reports/gatling/run-sample")
        runDir.mkdirs()
        val sampleLogStream =
            SpinochartPluginSpec::class.java.getResourceAsStream("/sample-simulation.log")
        sampleLogStream?.use { input ->
          runDir.resolve("simulation.log").outputStream().use { output -> input.copyTo(output) }
        }

        When("running the spinochartReport task via GradleRunner") {
          val result =
              org.gradle.testkit.runner.GradleRunner.create()
                  .withProjectDir(testProjectDir)
                  .withPluginClasspath()
                  .withArguments("spinochartReport", "--stacktrace")
                  .build()

          Then("the build should succeed and generate classic report") {
            result.task(":spinochartReport")?.outcome shouldBe
                org.gradle.testkit.runner.TaskOutcome.SUCCESS
            val reportFile = runDir.resolve("index.html")
            reportFile.shouldExist()
            val text = reportFile.readText()
            text shouldContain "Gatling Stats"
            text shouldContain "Response Time Ranges"
          }
        }
      }
    })
