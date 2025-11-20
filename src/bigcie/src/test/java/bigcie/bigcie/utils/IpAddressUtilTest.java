package bigcie.bigcie.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class IpAddressUtilTest {

    @Test
    void testGetClientIpFromXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.1");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetClientIpFromXForwardedForMultiple() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1, 172.16.0.1");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetClientIpFromXRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "203.0.113.5");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("203.0.113.5", result);
    }

    @Test
    void testGetClientIpFromRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.42");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("198.51.100.42", result);
    }

    @Test
    void testGetClientIpPreferencesXForwardedForOverXRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.1");
        request.addHeader("X-Real-IP", "203.0.113.5");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetClientIpPreferencesXRealIpOverRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "203.0.113.5");
        request.setRemoteAddr("198.51.100.42");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("203.0.113.5", result);
    }

    @Test
    void testGetClientIpWithEmptyXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "");
        request.addHeader("X-Real-IP", "203.0.113.5");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("203.0.113.5", result);
    }

    @Test
    void testGetClientIpWithWhitespaceInMultipleIps() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "  192.168.1.1  , 10.0.0.1");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("192.168.1.1", result);
    }

    @Test
    void testGetClientIpNoHeadersReturnsRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        
        String result = IpAddressUtil.getClientIp(request);
        assertEquals("127.0.0.1", result);
    }
}
