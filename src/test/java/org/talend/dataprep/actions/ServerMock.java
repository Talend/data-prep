package org.talend.dataprep.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

public class ServerMock {

    private final LocalTestServer server = new LocalTestServer(null, null);

    private final Set<String> set = new HashSet<>();

    private final String serverUrl;

    public ServerMock() throws Exception {
        server.start();
        serverUrl = "http:/" + server.getServiceAddress();
    }

    public void addEndPoint(String path, InputStream body, Header... headers) throws Exception {
        MockedRequestHandler mockedRequestHandler = new MockedRequestHandler(body, headers);
        server.register(path, mockedRequestHandler);
        set.add(path);
    }

    public void addEndPoint(String path, String body, Header... headers) throws Exception {
        addEndPoint(path, IOUtils.toInputStream(body), headers);
    }

    public void removeEndPoint(String pattern) {
        server.unregister(pattern);
        set.remove(pattern);
    }

    public void removeAllEndPoints(){
        set.forEach(server::unregister);
        set.clear();
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public static class MockedRequestHandler implements HttpRequestHandler {

        private final Header[] headers;

        private final InputStream body;

        public MockedRequestHandler(InputStream body, Header[] headers) {
            this.headers = headers;
            this.body = body;
        }

        @Override
        public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext)
                throws org.apache.http.HttpException, IOException {

            httpResponse.setEntity(new InputStreamEntity(body));
            if (headers != null) {
                for (Header header : headers) {
                    httpResponse.addHeader(header);
                }
            }
            httpResponse.setStatusCode(200);
        }
    }

}
