// ============================================================================
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.command;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.talend.dataprep.command.Defaults.asString;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.WebApplicationContext;
import org.talend.ServiceBaseTest;
import org.talend.dataprep.BaseErrorCodes;
import org.talend.dataprep.api.user.UserGroup;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.security.Security;

import com.netflix.hystrix.HystrixCommandGroupKey;

@TestPropertySource(properties = { "security.mode=genericCommandTest", "transformation.service.url=", "preparation.service.url=",
        "dataset.service.url=" })
public class GenericCommandTest extends ServiceBaseTest {

    private static TDPException lastException;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private HttpClient httpClient;

    @Autowired
    private Security security;

    private static RuntimeException error(Exception e) {
        lastException = (TDPException) e;
        return new RuntimeException(e);
    }

    @After
    public void tearDown() throws Exception {
        lastException = null;
    }

    private TestCommand getCommand(String url, Function<Exception, RuntimeException> errorHandling) {
        return context.getBean(TestCommand.class, url, errorHandling);
    }

    private TestGroupedStatusCommand getGroupedCommand(String url) {
        return context.getBean(TestGroupedStatusCommand.class, url);
    }

    @Test
    public void testSuccess() throws Exception {
        // Given
        final GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/success",
                GenericCommandTest::error);
        // When
        final String result = command.run();
        // Then
        assertThat(result, is("success"));
        assertThat(lastException, nullValue());
    }

    @Test
    public void testGenericSuccess() throws Exception {
        // Given
        final GenericCommand<String> command = getGroupedCommand("http://localhost:" + port + "/command/test/success");
        // When
        final String result = command.run();
        // Then
        assertThat(result, is("success"));
        assertThat(lastException, nullValue());
    }

    @Test
    public void testAuthenticationToken() throws Exception {
        // Given
        final GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/authentication/token",
                GenericCommandTest::error);
        // When
        final String result = command.run();
        // Then
        assertThat(result, is(security.getAuthenticationToken()));
        assertThat(lastException, nullValue());
    }

    @Test
    public void testSuccessWithMissingBehavior() throws Exception {
        // Given
        final GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/success_with_unknown",
                GenericCommandTest::error);
        // When
        final String result = command.run();
        // Then
        assertThat(result, nullValue()); // Missing behavior for 202 -> returns null.
        assertThat(lastException, nullValue());
    }

    @Test
    public void testFail_With_400() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_400",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TDPException.class));
            // Underlying exception is expected to be MISSING_ACTION_SCOPE
            assertThat(lastException.getCode().getCode(), is(BaseErrorCodes.MISSING_ACTION_SCOPE.getCode()));
            // And status is 400.
            assertThat(lastException.getCode().getHttpStatus(), is(400));
        }
    }

    @Test
    public void testFail_With_404() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/not_found",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TDPException.class));
            // Underlying exception is expected to be UNEXPECTED_EXCEPTION
            assertThat(lastException.getCode().getCode(), is(CommonErrorCodes.UNEXPECTED_EXCEPTION.getCode()));
            // and thrown because of a 404 error
            assertThat(lastException.getCode().getHttpStatus(), is(404));
        }
    }

    @Test
    public void testFail_With_500() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_500",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TDPException.class));
            // Underlying exception is expected to be UNABLE_TO_SERIALIZE_TO_JSON
            assertThat(lastException.getCode().getCode(), is(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON.getCode()));
            // And status is 500.
            assertThat(lastException.getCode().getHttpStatus(), is(500));
        }
    }

    @Test
    public void testFail_With_Unknown() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_unknown",
                GenericCommandTest::error);
        try {
            // When
            command.run();
        } catch (Exception e) {
            // Then
            // underlying was wrapped in another exception by error() method
            assertThat(e.getCause(), is(lastException));
            // underlying exception is a TDPException
            assertThat(lastException, isA(TDPException.class));
            // Underlying exception is expected to be UNEXPECTED_EXCEPTION
            assertThat(lastException.getCode().getProduct(), is(CommonErrorCodes.UNEXPECTED_EXCEPTION.getProduct()));
            assertThat(lastException.getCode().getCode(), is(CommonErrorCodes.UNEXPECTED_EXCEPTION.getCode()));
            // And status is 418.
            assertThat(lastException.getCode().getHttpStatus(), is(418));
        }
    }

    @Test
    public void testPassThrough() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/fail_with_500",
                Defaults.passthrough());
        try {
            // When
            command.run();
        } catch (TDPException e) {
            // Then
            // error() wasn't called, lastException must be null
            assertThat(lastException, nullValue());
            // underlying is returned as is (passthrough method).
            assertThat(e.getCode().getCode(), is(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON.getCode()));
        }
    }

    @Test
    public void testUnexpected() throws Exception {
        // Given
        GenericCommand<String> command = getCommand("http://localhost:" + port + "/command/test/unexpected",
                Defaults.passthrough());
        try {
            // When
            command.run();
        } catch (TDPException e) {
            // Then error() wasn't called, lastException must be null
            assertThat(lastException, nullValue());
            // underlying is returned as is (passthrough method).
            assertThat(e.getCode().getCode(), is(CommonErrorCodes.UNEXPECTED_SERVICE_EXCEPTION.getCode()));
            final Iterator<Map.Entry<String, Object>> entries = e.getContext().entries().iterator();
            assertThat(entries.hasNext(), is(true));
            final Map.Entry<String, Object> next = entries.next();
            assertThat(next.getKey(), is("message"));
            assertThat(String.valueOf(next.getValue()), is("Unable to execute an operation"));
        }
    }

    // Test command
    @Component
    @Scope("prototype")
    private static class TestGroupedStatusCommand extends GenericCommand<String> {

        protected TestGroupedStatusCommand(String url) {
            super(HystrixCommandGroupKey.Factory.asKey("dataset"));
            execute(() -> new HttpGet(url));
            onInfo().then(asString());
            onSuccess().then(asString());
            onRedirect().then(asString());
        }
    }

    // Test command
    @Component
    @Scope("prototype")
    private static class TestCommand extends GenericCommand<String> {

        protected TestCommand(String url, Function<Exception, RuntimeException> errorHandling) {
            super(HystrixCommandGroupKey.Factory.asKey("dataset"));
            execute(() -> new HttpGet(url));
            onError(errorHandling);
            on(HttpStatus.OK).then(asString());
        }
    }

    @Component
    @ConditionalOnProperty(name = "security.mode", havingValue = "genericCommandTest", matchIfMissing = false)
    private static class TestSecurity implements Security {

        @Override
        public String getUserId() {
            return "anonymous";
        }

        @Override
        public String getAuthenticationToken() {
            return "#1234";
        }

        @Override
        public Set<UserGroup> getGroups() {
            return Collections.emptySet();
        }

        @Override
        public String getTenantId() {
            return "tenant";
        }

        @Override
        public boolean isTDPUser() {
            return true;
        }

        @Override
        public String getTenantName() {
            return "tenant name";
        }
    }
}
