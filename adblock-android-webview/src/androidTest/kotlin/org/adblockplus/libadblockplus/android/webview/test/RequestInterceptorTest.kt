package org.adblockplus.libadblockplus.android.webview.test

import androidx.test.platform.app.InstrumentationRegistry
import org.adblockplus.libadblockplus.android.settings.AdblockHelper
import org.adblockplus.libadblockplus.android.webview.RequestInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URLEncoder

class RequestInterceptorTest : BaseAdblockWebViewTest() {

    companion object {
        private const val SCHEMA = "http://"
        const val BASE_URL = SCHEMA + RequestInterceptor.DEBUG_URL_HOSTNAME + "/"
        const val ADD_URL = BASE_URL + RequestInterceptor.COMMAND_STRING_ADD
        const val REMOVE_URL = BASE_URL + RequestInterceptor.COMMAND_STRING_REMOVE
        const val CLEAR_URL = BASE_URL + RequestInterceptor.COMMAND_STRING_CLEAR
        const val PAYLOAD_QUERY_PARAM_NAME = "/?" + RequestInterceptor.PAYLOAD_QUERY_PARAMETER_KEY + "="

        private const val PLAIN_URL_BASE = "data:" + RequestInterceptor.RESPONSE_MIME_TYPE + ","
        const val RESPONSE_INVALID_COMMAND = PLAIN_URL_BASE + RequestInterceptor.COMMAND_STRING_INVALID_COMMAND
        const val RESPONSE_INVALID_PAYLOAD = PLAIN_URL_BASE + RequestInterceptor.COMMAND_STRING_INVALID_PAYLOAD
        const val RESPONSE_OK = PLAIN_URL_BASE + RequestInterceptor.COMMAND_STRING_OK
    }

    @Test
    fun testInvalidCommand() {
        assertTrue("$BASE_URL exceeded loading timeout", testSuitAdblock.loadUrlAndWait(BASE_URL, RESPONSE_INVALID_COMMAND))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_INVALID_COMMAND)
        }
    }

    @Test
    fun testAddRemoveErrorWithoutPayload() {
        assertTrue("$ADD_URL exceeded loading timeout",
                testSuitAdblock.loadUrlAndWait(ADD_URL, RESPONSE_INVALID_PAYLOAD))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_INVALID_PAYLOAD)
        }
        assertTrue("$REMOVE_URL exceeded loading timeout",
                testSuitAdblock.loadUrlAndWait(REMOVE_URL, RESPONSE_INVALID_PAYLOAD))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_INVALID_PAYLOAD)
        }
    }

    @Test
    fun testAddRemoveClearFilter() {
        val filter1 = "dp-testpages.adblockplus.org##.testcase-examplecontent + .eh-sibling"
        val filter2 = "/ad_iframe/*\\\$domain=~convert-video-online.com|~online-audio-converter.com"
        val filters = "$filter1\n$filter2"
        val addUrl = ADD_URL + PAYLOAD_QUERY_PARAM_NAME + URLEncoder.encode(filters, RequestInterceptor.URL_ENCODE_CHARSET)

        assertEquals(0, AdblockHelper.get().provider.engine.settings().listedFilters.size)

        // Test add
        assertTrue("$addUrl exceeded loading timeout", testSuitAdblock.loadUrlAndWait(addUrl, RESPONSE_OK))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_OK)
        }
        assertEquals(2, AdblockHelper.get().provider.engine.settings().listedFilters.size)

        // Test duplicate
        assertTrue("$addUrl exceeded loading timeout", testSuitAdblock.loadUrlAndWait(addUrl, RESPONSE_OK))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_OK)
        }
        assertEquals(2, AdblockHelper.get().provider.engine.settings().listedFilters.size)

        // Test remove
        val removeUrl = REMOVE_URL + PAYLOAD_QUERY_PARAM_NAME + URLEncoder.encode(filter1,
                RequestInterceptor.URL_ENCODE_CHARSET)
        assertTrue("$removeUrl exceeded loading timeout", testSuitAdblock.loadUrlAndWait(removeUrl, RESPONSE_OK))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_OK)
        }
        assertEquals(AdblockHelper.get().provider.engine.settings().listedFilters.size, 1)

        // Test repeated remove
        assertTrue("$removeUrl exceeded loading timeout", testSuitAdblock.loadUrlAndWait(removeUrl, RESPONSE_OK))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_OK)
        }
        assertEquals(1, AdblockHelper.get().provider.engine.settings().listedFilters.size)

        // Test duplicate
        assertTrue("$addUrl exceeded loading timeout", testSuitAdblock.loadUrlAndWait(addUrl, RESPONSE_OK))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_OK)
        }
        assertEquals(2, AdblockHelper.get().provider.engine.settings().listedFilters.size)

        // Test clear
        assertTrue("$CLEAR_URL exceeded loading timeout", testSuitAdblock.loadUrlAndWait(CLEAR_URL, RESPONSE_OK))
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            assertEquals(testSuitAdblock.webView.originalUrl, RESPONSE_OK)
        }
        assertEquals(0, AdblockHelper.get().provider.engine.settings().listedFilters.size)
    }

}
