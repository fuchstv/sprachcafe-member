package org.sprachcafe.member.data

import org.junit.Assert.assertEquals
import org.junit.Test

class MemberTierDefaultsTest {

    @Test
    fun `getCoffeeQuota returns expected quota for each tier`() {
        assertEquals(4, MemberTierDefaults.getCoffeeQuota("SILVER"))
        assertEquals(8, MemberTierDefaults.getCoffeeQuota("GOLD"))
        assertEquals(10, MemberTierDefaults.getCoffeeQuota("PLATINUM"))
        assertEquals(10, MemberTierDefaults.getCoffeeQuota("COMPANY"))
    }

    @Test
    fun `getCoffeeQuota is case insensitive`() {
        assertEquals(4, MemberTierDefaults.getCoffeeQuota("silver"))
        assertEquals(8, MemberTierDefaults.getCoffeeQuota("Gold"))
        assertEquals(10, MemberTierDefaults.getCoffeeQuota("PlAtInUm"))
    }

    @Test
    fun `getCoffeeQuota returns default quota for unknown or null tier`() {
        assertEquals(4, MemberTierDefaults.getCoffeeQuota("UNKNOWN"))
        assertEquals(4, MemberTierDefaults.getCoffeeQuota(null))
        assertEquals(4, MemberTierDefaults.getCoffeeQuota(""))
    }
}
