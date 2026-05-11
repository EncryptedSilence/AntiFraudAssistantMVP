package com.qalqan.antifraud.calls

import androidx.test.core.app.ApplicationProvider
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.ContactProfile
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.TrustStatus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class IsKnownContactResolverTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repos = Repositories.inMemory(context)

    @After
    fun tearDown() {
        repos.close()
    }

    @Test
    fun `unknown phone hash resolves to false`() {
        runBlocking {
            val resolver = IsKnownContactResolver(repos.contacts)
            resolver.isKnown(PhoneHash("nope")) shouldBe false
        }
    }

    @Test
    fun `phone hash present in ContactProfile resolves to true when isInContacts`() {
        runBlocking {
            repos.contacts.save(
                ContactProfile(
                    id = UUID.randomUUID().toString(),
                    phoneNormalizedEnc = ByteArray(0),
                    phoneHash = PhoneHash("h1"),
                    phoneLast4 = "1234",
                    isShortCode = false,
                    displayNameLocal = null,
                    isInContacts = true,
                    trustStatus = TrustStatus.NEUTRAL,
                    firstSeenAt = Instant.now(),
                    lastSeenAt = Instant.now(),
                    riskCounter = 0,
                    userComment = null,
                ),
            )
            IsKnownContactResolver(repos.contacts).isKnown(PhoneHash("h1")) shouldBe true
        }
    }

    @Test
    fun `phone hash present but not isInContacts resolves to false`() {
        runBlocking {
            repos.contacts.save(
                ContactProfile(
                    id = UUID.randomUUID().toString(),
                    phoneNormalizedEnc = ByteArray(0),
                    phoneHash = PhoneHash("h2"),
                    phoneLast4 = null,
                    isShortCode = false,
                    displayNameLocal = null,
                    isInContacts = false,
                    trustStatus = TrustStatus.SUSPICIOUS,
                    firstSeenAt = Instant.now(),
                    lastSeenAt = Instant.now(),
                    riskCounter = 0,
                    userComment = null,
                ),
            )
            IsKnownContactResolver(repos.contacts).isKnown(PhoneHash("h2")) shouldBe false
        }
    }
}
