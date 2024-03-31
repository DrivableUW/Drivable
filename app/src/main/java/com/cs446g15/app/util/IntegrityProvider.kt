import com.cs446g15.app.MainActivity
import com.cs446g15.common.sha256
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.google.android.play.core.integrity.StandardIntegrityManager.StandardIntegrityTokenRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import kotlinx.coroutines.tasks.await

object IntegrityProvider {
    private val _provider by lazy {
        IntegrityManagerFactory.createStandard(MainActivity.appContext).prepareIntegrityToken(
            StandardIntegrityManager.PrepareIntegrityTokenRequest.builder().apply {
                setCloudProjectNumber(62906597950)
            }.build()
        )
    }

    private val httpClient by lazy { HttpClient(CIO) }

    private suspend fun provider() = _provider.await()

    private suspend fun request(builder: StandardIntegrityTokenRequest.Builder.() -> Unit = {})
        = provider()
            .request(StandardIntegrityTokenRequest.builder().apply(builder).build())
            .await()
            .token()

    suspend fun attest(
        json: String
    ): String {
        val token = request {
            setRequestHash(json.sha256())
        }
        val result = httpClient.post {
            // 10.0.2.2 refers to localhost from the Android emulator
            url(Url("http://10.0.2.2:8081/sign"))
            headers {
                append("integrity-token", token)
            }
            setBody(json)
        }
        return result.bodyAsText()
    }
}
