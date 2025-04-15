package com.my.eventtablemerger

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.util.store.FileDataStoreFactory
import com.my.eventtablemerger.features.screens.observe.AuthorizationCodeReceiver
import com.my.eventtablemerger.features.screens.observe.CredentialsProvider
import com.my.eventtablemerger.features.screens.observe.JSON_FACTORY
import com.my.eventtablemerger.features.screens.observe.SCOPES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File
import java.io.InputStreamReader
import kotlin.coroutines.resume

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

class AndroidCredentialsProvider(private val activity: Activity) : CredentialsProvider {
    override suspend fun getCredentials(): Credential = withContext(Dispatchers.IO) {
        val inputStream = activity.resources.openRawResource(R.raw.credentials)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inputStream))

        val tokensDirectory = File(activity.filesDir, "tokens")
        if (!tokensDirectory.exists()) {
            tokensDirectory.mkdirs()
        }

        val flow = GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            JSON_FACTORY,
            clientSecrets,
            SCOPES
        )
            .setDataStoreFactory(FileDataStoreFactory(tokensDirectory))
            .setAccessType("offline")
            .build()

        val receiver = AndroidAuthorizationCodeReceiver(activity)
        val code = receiver.waitForCode()

        // Получаем GoogleTokenResponse
        val tokenResponse =
            flow.newTokenRequest(code).setRedirectUri(receiver.getRedirectUri()).execute()

        // Преобразуем GoogleTokenResponse в Credential
        return@withContext flow.createAndStoreCredential(tokenResponse, "user")
    }
}

val androidCredentialsProviderModule = module {

    singleOf(::AndroidCredentialsProvider) {bind<CredentialsProvider>()}
}

class AndroidAuthorizationCodeReceiver(private val activity: Activity) : AuthorizationCodeReceiver {
    private var redirectUri: String = "com.my.eventtablemerger://oauth2redirect"

    override fun getRedirectUri(): String {
        return redirectUri
    }

    override fun stop() {

    }

    override suspend fun waitForCode(): String = suspendCancellableCoroutine { continuation ->
        val authIntent = Intent(Intent.ACTION_VIEW, redirectUri.toUri())
        activity.startActivity(authIntent)

        // Implement a way to capture the redirect URI and extract the code
        // For example, using a custom scheme or a deep link
        val code = "extracted_code_from_redirect_uri"
        continuation.resume(code)
    }
}

