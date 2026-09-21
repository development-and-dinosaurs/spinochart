import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.kotlin.jvm")
}

val libs = the<LibrariesForLibs>()

dependencies {
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.kotest.framework.engine)
  testImplementation(libs.kotest.runner.junit5)
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions.jvmTarget = JvmTarget.JVM_21
}

tasks.withType<Test> {
  useJUnitPlatform()
  jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
  filter {
    isFailOnNoMatchingTests = false
  }
}
