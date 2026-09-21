import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins { `kotlin-dsl` }

dependencies {
  implementation(libs.plugin.spotless)
  implementation(libs.plugin.kotlin.jvm)
  // Exposes the `libs` version catalog to precompiled script plugins. See https://github.com/gradle/gradle/issues/15383.
  implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<KotlinCompile>().configureEach { compilerOptions.jvmTarget = JvmTarget.JVM_21 }
