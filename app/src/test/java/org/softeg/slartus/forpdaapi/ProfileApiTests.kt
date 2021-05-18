package org.softeg.slartus.forpdaapi

import org.junit.Assert
import org.junit.Test

internal class ProfileApiTests {
    @Test
    fun loginFormTest() {
        val page = javaClass.classLoader?.getResource("profileapi/loginform01.html")?.readText() ?: ""
        val loginForm = ProfileApi.parseLoginForm(page)
        Assert.assertEquals(loginForm.capPath, "//captcha.app.devapps.ru/captcha/2c5faa01248fb27f970a7f5ef9e44514.gif")
        Assert.assertEquals(loginForm.capTime, "1621310326")
        Assert.assertEquals(loginForm.capSig, "70908825fe6110ecc3d4c53c633dd459")
        Assert.assertEquals(loginForm.session, null)
        Assert.assertEquals(loginForm.error, null)
    }
}