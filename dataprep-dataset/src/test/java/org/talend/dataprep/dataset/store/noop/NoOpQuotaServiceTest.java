package org.talend.dataprep.dataset.store.noop;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class NoOpQuotaServiceTest {

    private NoOpQuotaService service = new NoOpQuotaService();

    @Mock
    private ConditionContext context;

    @Test
    public void shouldEnableQuotaServiceBecausePropertyIsMissing() {
        // given
        givenQuotaProperty(null);

        // when
        final boolean matches = service.matches(context, null);

        // then
        assertTrue(matches);
    }

    @Test
    public void shouldEnableQuotaServiceBecausePropertyIsFalse() {
        // given
        givenQuotaProperty("false");

        // when
        final boolean matches = service.matches(context, null);

        // then
        assertTrue(matches);
    }

    @Test
    public void shouldDisableQuotaServiceBecausePropertyIsTrue() {
        // given
        givenQuotaProperty("true");

        // when
        final boolean matches = service.matches(context, null);

        // then
        assertFalse(matches);
    }

    private void givenQuotaProperty(String returnValue) {
        Environment environment = mock(Environment.class);
        when(context.getEnvironment()).thenReturn(environment);
        when(environment.getProperty("dataset.quota.check.enabled")).thenReturn(returnValue);
    }

}
