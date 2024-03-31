package com.cs446g15.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.playintegrity.v1.PlayIntegrity
import com.google.api.services.playintegrity.v1.PlayIntegrityRequestInitializer
import com.google.api.services.playintegrity.v1.model.DecodeIntegrityTokenRequest
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.google.gson.Gson
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import java.io.File
import java.io.StringWriter
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.Security
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.KeyGenerator

fun Application.configureRouting() {
    val file = File("backend/src/main/resources/key.p8")
    if (!file.exists()) {
        KeyPairGenerator.getInstance("EC")
            .apply { initialize(256) }
            .generateKeyPair()
            .private
            .encoded
            .also { file.writeBytes(it) }
    }
    val der = file.readBytes()
    val key = PKCS8EncodedKeySpec(der)
    val ecPrivateKey = KeyFactory.getInstance("EC").generatePrivate(key)
    val issuer = "com.cs446g15.backend"
    val algorithm = Algorithm.ECDSA256(ecPrivateKey as ECPrivateKey)
    val pub = getPublicKeyPEMFromECPrivateKey(ecPrivateKey)

    val playIntegrity = PlayIntegrity.Builder(
        GoogleNetHttpTransport.newTrustedTransport(),
        GsonFactory.getDefaultInstance(),
        HttpCredentialsAdapter(GoogleCredentials.fromStream(
            File("backend/src/main/resources/google.json").inputStream()
        )),
    ).apply {
        applicationName = "com.cs446g15.backend"
        googleClientRequestInitializer = PlayIntegrityRequestInitializer()
    }.build()

    routing {
        get("/pubkey") {
            call.respondText(pub)
        }

        post("/sign") {
            val token = call.request.header("integrity-token")
            val result = playIntegrity.v1()
                .decodeIntegrityToken(
                    "com.cs446g15.app",
                    DecodeIntegrityTokenRequest().apply {
                        integrityToken = token
                    }
                )
                .execute()
                .tokenPayloadExternal
            val dataRaw = call.receive<String>()

            if (dataRaw.sha256() != (result.requestDetails.requestHash ?: "")) {
                call.respondText("Invalid hash", status = HttpStatusCode.BadRequest)
                return@post
            }

            val dataJson = Json.parseToJsonElement(dataRaw)
            val appIntegrityJsonString = Gson().toJson(result)
            val appIntegrityJson = Json.parseToJsonElement(appIntegrityJsonString)
            val payload = mapOf(
                "appIntegrity" to appIntegrityJson,
                "data" to dataJson
            )
            val payloadJson = Json.encodeToString(payload)

            val jwt = JWT.create().apply {
                withIssuer(issuer)
                withPayload(payloadJson)
            }.sign(algorithm)
            call.respond(jwt)
        }
    }
}

fun getPublicKeyPEMFromECPrivateKey(privateKey: ECPrivateKey): String {
    // https://stackoverflow.com/a/61739800
    Security.addProvider(BouncyCastleProvider())
    val keyFactory: KeyFactory = KeyFactory.getInstance("EC", "BC")
    val spec = ECNamedCurveTable.getParameterSpec("prime256v1")
    val q = spec.g.multiply(privateKey.s)
    val publicKey = keyFactory.generatePublic(org.bouncycastle.jce.spec.ECPublicKeySpec(q, spec))
    return StringWriter().use {
        val pemWriter = JcaPEMWriter(it)
        pemWriter.writeObject(publicKey)
        pemWriter.flush()
        it.toString()
    }
}

private fun String.sha256()
        = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray())
    .joinToString("") { "%02x".format(it) }
