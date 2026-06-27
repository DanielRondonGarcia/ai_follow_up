package com.example.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fixture-based tests for [OllamaService.isOllamaSessionExpired].
 *
 * Loads captured HTML fixtures from the test classpath and asserts the pure
 * predicate classifies them correctly, without any network access.
 */
class OllamaExpiryTest {

  private val service = OllamaService()

  @Test
  fun signinFixture_isExpired() {
    val html = loadResource("ollama_signin_fixture.html")
    assertTrue("signin fixture must be detected as expired", service.isOllamaSessionExpired(html))
  }

  @Test
  fun settingsFixture_isNotExpired() {
    val html = loadResource("ollama_settings_fixture.html")
    assertFalse("settings fixture must be detected as valid", service.isOllamaSessionExpired(html))
  }

  @Test
  fun emptyString_isNotExpired_fallsThroughToParseError() {
    // Per design: empty/garbage returns false so the caller emits ParseError,
    // NOT AuthExpired. An empty body is a parse problem, not an auth problem.
    assertFalse("empty string must not be flagged as expired", service.isOllamaSessionExpired(""))
  }

  @Test
  fun blankString_isNotExpired() {
    assertFalse(service.isOllamaSessionExpired("   "))
  }

  @Test
  fun htmlWithOnlySessionLabel_isNotExpired() {
    val html = "<div aria-label=\"Session usage 50 %\">50%</div>"
    assertFalse("session label alone is enough to be valid", service.isOllamaSessionExpired(html))
  }

  @Test
  fun htmlWithOnlyWeeklyLabel_isNotExpired() {
    val html = "<div aria-label=\"Weekly usage 10 %\">10%</div>"
    assertFalse("weekly label alone is enough to be valid", service.isOllamaSessionExpired(html))
  }

  @Test
  fun htmlWithPasswordInput_isExpired() {
    val html = "<form><input name=\"password\" type=\"password\"/></form>"
    assertTrue("password input marks a signin page", service.isOllamaSessionExpired(html))
  }

  @Test
  fun htmlWithSigninPath_isExpired() {
    val html = "<a href=\"/signin/google\">Continue with Google</a>"
    assertTrue("/signin path marks a signin page", service.isOllamaSessionExpired(html))
  }

  @Test
  fun htmlWithNeitherLabelNorSignin_isExpired() {
    val html = "<html><body><h1>Welcome</h1><p>Some random page</p></body></html>"
    assertTrue("no labels and no signin marker is expired", service.isOllamaSessionExpired(html))
  }

  private fun loadResource(name: String): String {
    val stream = javaClass.classLoader!!.getResourceAsStream(name)
      ?: error("Missing test resource: $name")
    return stream.bufferedReader().use { it.readText() }
  }
}