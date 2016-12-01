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


    public void addEndPoint(String path, String body,  Header... headers) throws Exception {
        addEndPoint(path, body, 200, headers);
    }
    public void addEndPoint(String path, InputStream body, int statusCode, Header... headers) throws Exception {
        MockedRequestHandler mockedRequestHandler = new MockedRequestHandler(body, statusCode, headers);
        server.register(path, mockedRequestHandler);
        set.add(path);
    }

    public void addEndPoint(String path, InputStream body, Header... headers) throws Exception {
        addEndPoint(path, body, 200, headers);
    }

    public void addEndPoint(String path, String body,int statusCode,  Header... headers) throws Exception {
        addEndPoint(path, IOUtils.toInputStream(body), statusCode, headers);
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

        private final int statusCode;

        public MockedRequestHandler(InputStream body, Header[] headers) {
            this(body, 200, headers);
        }

        public MockedRequestHandler(InputStream body, int statusCode, Header[] headers) {
            this.headers = headers;
            this.body = body;
            this.statusCode = statusCode;
        }

        public MockedRequestHandler(String body, Header[] headers) {
            this(body, 200, headers);
        }

        public MockedRequestHandler(String body, int statusCode, Header[] headers) {
            this.headers = headers;
            this.body = IOUtils.toInputStream(body);
            this.statusCode = statusCode;
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
            httpResponse.setStatusCode(statusCode);
        }
    }

}
