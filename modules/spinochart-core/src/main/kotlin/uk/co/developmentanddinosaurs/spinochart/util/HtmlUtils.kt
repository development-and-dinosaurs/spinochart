package uk.co.developmentanddinosaurs.spinochart.util

object HtmlUtils {

  fun escapeHtml(value: String): String =
      value
          .replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;")
          .replace("\"", "&quot;")
          .replace("'", "&#39;")
}
