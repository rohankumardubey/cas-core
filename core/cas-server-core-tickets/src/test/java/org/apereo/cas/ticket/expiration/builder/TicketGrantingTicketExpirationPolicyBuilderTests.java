package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.ThrottledUseAndTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.TicketGrantingTicketExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TicketGrantingTicketExpirationPolicyBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Tickets")
class TicketGrantingTicketExpirationPolicyBuilderTests {

    @Test
    void verifyRememberMe() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getRememberMe().setEnabled(true);
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof RememberMeDelegatingExpirationPolicy);
        assertNotNull(builder.toString());
        assertNotNull(builder.casProperties());
    }

    @Test
    void verifyNever() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getPrimary().setMaxTimeToLiveInSeconds("-1");
        props.getTicket().getTgt().getPrimary().setTimeToKillInSeconds("-1");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof NeverExpiresExpirationPolicy);
    }

    @Test
    void verifyDefault() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getPrimary().setMaxTimeToLiveInSeconds("10");
        props.getTicket().getTgt().getPrimary().setTimeToKillInSeconds("10");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof TicketGrantingTicketExpirationPolicy);
    }

    @Test
    void verifyTimeout() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getTimeout().setMaxTimeToLiveInSeconds("10");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof TimeoutExpirationPolicy);
    }

    @Test
    void verifyHard() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getHardTimeout().setTimeToKillInSeconds("PT10S");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof HardTimeoutExpirationPolicy);
    }

    @Test
    void verifyThrottle() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getThrottledTimeout().setTimeInBetweenUsesInSeconds("10");
        props.getTicket().getTgt().getThrottledTimeout().setTimeToKillInSeconds("10");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof ThrottledUseAndTimeoutExpirationPolicy);
    }

    @Test
    void verifyAlways() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getPrimary().setMaxTimeToLiveInSeconds("0");
        props.getTicket().getTgt().getPrimary().setTimeToKillInSeconds("NEVER");
        val builder = new TicketGrantingTicketExpirationPolicyBuilder(props);
        assertTrue(builder.buildTicketExpirationPolicy() instanceof AlwaysExpiresExpirationPolicy);
    }
}
