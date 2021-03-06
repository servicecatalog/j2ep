/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.j2ep.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletException;

import net.sf.j2ep.ProxyFilter;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

public class TraceTest extends FilterTestCase {

    private ProxyFilter proxyFilter;

    public void setUp() {
        proxyFilter = new ProxyFilter();

        config.setInitParameter("dataUrl",
                "/WEB-INF/classes/net/sf/j2ep/test/testData.xml");
        try {
            proxyFilter.init(config);
        } catch (ServletException e) {
            fail("Problem with init, error given was " + e.getMessage());
        }
    }

    public void beginNoMaxFowards(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/", null, null);
        theRequest.addHeader("Via", "HTTP/1.0 fakeserver.com");
    }

    public void testNoMaxFowards() throws ServletException, IOException {
        MethodWrappingRequest req = new MethodWrappingRequest(request, "TRACE");
        proxyFilter.doFilter(req, response, filterChain);
    }

    public void endNoMaxFowards(WebResponse theResponse) {
        String serverHostName = null;
        try {
            serverHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            fail("Couldn't get the hostname needed for header Via");
        }

        String contentType = theResponse.getConnection().getContentType();
        assertEquals("Checking content-type", "message/http", contentType);

        String expectedVia = "HTTP/1.0 fakeserver.com, " + "HTTP/1.1 " + serverHostName;
        assertTrue("Checking that the via header is included", theResponse.getText().indexOf(expectedVia)>-1);

        String expectedUserAgent = "Jakarta Commons-HttpClient/3.0-rc3";
        assertTrue("Checking that user-agent is included", theResponse.getText().indexOf(expectedUserAgent)>-1);
    }

    public void beginMaxForwards(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/maxForwards", null, null);
        theRequest.addHeader("Via", "HTTP/1.0 fakeserver.com");
        theRequest.addHeader("Max-Forwards", "0");
    }

    public void testMaxForwards() throws ServletException,
            IOException {
        MethodWrappingRequest req = new MethodWrappingRequest(request, "TRACE");
        proxyFilter.doFilter(req, response, filterChain);
    }

    public void endMaxForwards(WebResponse theResponse) {
        String contentType = theResponse.getConnection().getContentType();
        if (contentType.indexOf(";") != -1) {
            contentType = contentType.substring(0, contentType.indexOf(";"));
        }
        assertEquals("Checking content-type", "message/http", contentType);

        String expectedVia = "HTTP/1.0 fakeserver.com";
        assertTrue("Checking that the via header is included", theResponse.getText().indexOf(expectedVia)>-1);
    }
}
