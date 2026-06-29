package com.example.network

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetSocketAddress
import java.util.UUID

/**
 * Tests the SyncResult mapping in [ChatGPTService], [AnthropicService], and
 * [OllamaService] using the JDK built-in [HttpServer] (no extra dependency).
 *
 * Each test subclasses the service to redirect its endpoint to the local
 * server, then asserts the [SyncResult] variant returned for a given status
 * code and body.
 *
 * Scenarios covered:
 * - 401/403 -> AuthExpired (with the account userId carried through)
 * - 500 -> NetworkError
 * - 200 with malformed JSON -> ParseError
 * - Ollama 200 with signin HTML -> AuthExpired
 * - Ollama 200 with settings HTML -> Success
 */
class SyncResultTest {

  private lateinit var server: HttpServer
  private var status: Int = 200
  private var body: String = ""

  @Before
  fun setUp() {
    server = HttpServer.create(InetSocketAddress(0), 0)
    server.createContext("/", TestHandler { status to body })
    server.start()
  }

  @After
  fun tearDown() {
    server.stop(0)
  }

  private fun baseUrl(): String = "http://localhost:${server.address.port}/"

  @Test
  fun chatgpt_401_returnsAuthExpired() = runTest {
    status = 401; body = "unauthorized"
    val service = object : ChatGPTService(oaiDeviceId = UUID.randomUUID().toString()) {
      override fun usageEndpoint() = baseUrl()
    }
    val result = service.fetchUsage("tok", "", "", "user-123")
    assertTrue("expected AuthExpired got $result", result is SyncResult.AuthExpired)
    val auth = result as SyncResult.AuthExpired
    assertTrue(auth.provider == "OpenAI")
    assertTrue(auth.userId == "user-123")
  }

  @Test
  fun chatgpt_403_returnsAuthExpired() = runTest {
    status = 403; body = "forbidden"
    val service = object : ChatGPTService(oaiDeviceId = UUID.randomUUID().toString()) {
      override fun usageEndpoint() = baseUrl()
    }
    val result = service.fetchUsage("tok", "", "", "user-456")
    assertTrue(result is SyncResult.AuthExpired)
  }

  @Test
  fun chatgpt_500_returnsNetworkError() = runTest {
    status = 500; body = "server error"
    val service = object : ChatGPTService(oaiDeviceId = UUID.randomUUID().toString()) {
      override fun usageEndpoint() = baseUrl()
    }
    val result = service.fetchUsage("tok", "", "", "user-789")
    assertTrue("expected NetworkError got $result", result is SyncResult.NetworkError)
  }

  @Test
  fun chatgpt_200_badJson_returnsParseError() = runTest {
    status = 200; body = "not json"
    val service = object : ChatGPTService(oaiDeviceId = UUID.randomUUID().toString()) {
      override fun usageEndpoint() = baseUrl()
    }
    val result = service.fetchUsage("tok", "", "", "user-789")
    assertTrue("expected ParseError got $result", result is SyncResult.ParseError)
  }

  @Test
  fun anthropic_401_returnsAuthExpiredWithOrgId() = runTest {
    status = 401; body = "unauthorized"
    val service = object : AnthropicService(
      anonymousId = "claudeai.v1.${UUID.randomUUID()}",
      deviceId = UUID.randomUUID().toString()
    ) {
      override fun usageEndpoint(orgId: String) = baseUrl()
    }
    val result = service.fetchUsage("org-uuid", "sk-ant-x", "", "")
    assertTrue("expected AuthExpired got $result", result is SyncResult.AuthExpired)
    val auth = result as SyncResult.AuthExpired
    assertTrue(auth.provider == "Anthropic")
    assertTrue(auth.userId == "org-uuid")
  }

  @Test
  fun anthropic_500_returnsNetworkError() = runTest {
    status = 500; body = "server error"
    val service = object : AnthropicService(
      anonymousId = "claudeai.v1.${UUID.randomUUID()}",
      deviceId = UUID.randomUUID().toString()
    ) {
      override fun usageEndpoint(orgId: String) = baseUrl()
    }
    val result = service.fetchUsage("org-uuid", "sk-ant-x", "sessionKey=abc", "")
    assertTrue("expected NetworkError got $result", result is SyncResult.NetworkError)
  }

  @Test
  fun ollama_signinHtml_returnsAuthExpired() = runTest {
    status = 200
    body = javaClass.classLoader!!.getResourceAsStream("ollama_signin_fixture.html")
      .bufferedReader().use { it.readText() }
    val service = object : OllamaService() {
      override fun settingsEndpoint() = baseUrl()
    }
    val result = service.fetchUsage("cookies", "ua", "user@example.com")
    assertTrue("expected AuthExpired got $result", result is SyncResult.AuthExpired)
    val auth = result as SyncResult.AuthExpired
    assertTrue(auth.provider == "Ollama")
    assertTrue(auth.userId == "user@example.com")
  }

  @Test
  fun ollama_settingsHtml_returnsSuccess() = runTest {
    status = 200
    body = javaClass.classLoader!!.getResourceAsStream("ollama_settings_fixture.html")
      .bufferedReader().use { it.readText() }
    val service = object : OllamaService() {
      override fun settingsEndpoint() = baseUrl()
    }
    val result = service.fetchUsage("cookies", "ua", "user@example.com")
    assertTrue("expected Success got $result", result is SyncResult.Success)
  }

  @Test
  fun ollama_500_returnsNetworkError() = runTest {
    status = 500; body = "server error"
    val service = object : OllamaService() {
      override fun settingsEndpoint() = baseUrl()
    }
    val result = service.fetchUsage("cookies", "ua", "user@example.com")
    assertTrue("expected NetworkError got $result", result is SyncResult.NetworkError)
  }

  private inner class TestHandler(private val responder: () -> Pair<Int, String>) : HttpHandler {
    override fun handle(exchange: HttpExchange) {
      val (code, text) = responder()
      val bytes = text.toByteArray(Charsets.UTF_8)
      exchange.sendResponseHeaders(code, bytes.size.toLong())
      exchange.responseBody.use { it.write(bytes) }
      exchange.close()
    }
  }
}