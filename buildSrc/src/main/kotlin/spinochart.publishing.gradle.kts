import java.util.Base64

plugins {
  `maven-publish`
  signing
}

fun decode(base64Key: String?): String =
    if (base64Key == null) "" else String(Base64.getDecoder().decode(base64Key))

configure<SigningExtension> {
  val signingKeyBase64: String? by project
  val signingPassword: String? by project
  useInMemoryPgpKeys(decode(signingKeyBase64), signingPassword)
}

configure<PublishingExtension> {
  publications.withType<MavenPublication> {
    pom {
      inceptionYear.set("2026")
      url.set("https://github.com/development-and-dinosaurs/spinochart/")
      licenses {
        license {
          name.set("The MIT License")
          url.set("https://opensource.org/licenses/MIT")
          distribution.set("https://opensource.org/licenses/MIT")
        }
      }
      developers {
        developer {
          id.set("tyrannoseanus")
          name.set("Tyrannoseanus")
          url.set("https://github.com/Tyrannoseanus/")
          email.set("tyrannoseanus@developmentanddinosaurs.co.uk")
        }
      }
      scm {
        url.set("https://github.com/development-and-dinosaurs/spinochart/")
        connection.set("scm:git:git://github.com/development-and-dinosaurs/spinochart.git")
        developerConnection.set(
            "scm:git:ssh://git@github.com/development-and-dinosaurs/spinochart.git"
        )
      }
    }
  }
}
