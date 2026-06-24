package com.example.lifelab.feature.profile.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProfileOverviewTest {

    @Test
    fun guestSessionProvidesStablePlaceholderOverviewWithoutFakeEmail() {
        val overview = ProfileSession.Guest.toOverview()

        assertEquals("Guest", overview.displayName)
        assertEquals("Sign in to sync your LifeLab profile.", overview.description)
        assertEquals("G", overview.avatarInitial)
        assertNull(overview.email)
    }

    @Test
    fun signedInSessionUsesRealUserInformationForOverview() {
        val user = ProfileUser(
            id = "user-1",
            displayName = "Avery Chen",
            email = "avery@example.com",
            membershipLabel = "Pro member",
            avatarInitial = "A",
        )

        val overview = ProfileSession.SignedIn(user).toOverview()

        assertEquals("Avery Chen", overview.displayName)
        assertEquals("Pro member", overview.description)
        assertEquals("avery@example.com", overview.email)
        assertEquals("A", overview.avatarInitial)
    }
}
