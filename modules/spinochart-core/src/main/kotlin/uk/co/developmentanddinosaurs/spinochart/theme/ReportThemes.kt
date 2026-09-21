package uk.co.developmentanddinosaurs.spinochart.theme

object ReportThemes {
  val SPINO: ReportTheme = SpinoTheme()
  val CLASSIC: ReportTheme = ClassicTheme()

  val all: List<ReportTheme> = listOf(SPINO, CLASSIC)

  fun fromIdOrNull(id: String): ReportTheme? =
      when (id.trim().lowercase()) {
        "classic",
        "gatling" -> CLASSIC
        "spino",
        "modern" -> SPINO
        else -> all.firstOrNull { it.id.equals(id.trim(), ignoreCase = true) }
      }

  fun fromId(id: String): ReportTheme =
      fromIdOrNull(id)
          ?: throw IllegalArgumentException(
              "Unknown theme '$id'. Available themes: ${all.joinToString { it.id }}"
          )
}
