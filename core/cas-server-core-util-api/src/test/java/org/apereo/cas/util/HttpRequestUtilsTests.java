package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HttpRequestUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Utility")
class HttpRequestUtilsTests {

    @Test
    void verifyNoRequest() {
        assertNull(HttpRequestUtils.getHttpServletRequestFromRequestAttributes());
    }

    @Test
    void verifyNoLoc() {
        val loc = HttpRequestUtils.getHttpServletRequestGeoLocation(new MockHttpServletRequest());
        assertNull(loc.getLongitude());
    }

    @Test
    void verifyHeader() {
        val request = new MockHttpServletRequest();
        request.addHeader("h1", "v1");
        request.addHeader("h2", "v2");
        assertNotNull(HttpRequestUtils.getRequestHeaders(request));
    }

    @Test
    void verifyPing() {
        assertNotNull(HttpRequestUtils.pingUrl("https://github.com"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, HttpRequestUtils.pingUrl("bad-endpoint"));
    }
}
