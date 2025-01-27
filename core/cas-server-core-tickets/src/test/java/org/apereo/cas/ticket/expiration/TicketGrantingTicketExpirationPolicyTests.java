package org.apereo.cas.ticket.expiration;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.DefaultServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.ServiceTicketSessionTrackingPolicy;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Clock;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link TicketGrantingTicketExpirationPolicy}.
 *
 * @author William G. Thompson, Jr.
 * @since 3.4.10
 */
@Tag("Tickets")
class TicketGrantingTicketExpirationPolicyTests {

    private static final long HARD_TIMEOUT = 200;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ticketGrantingTicketExpirationPolicyTests.json");

    private static final long SLIDING_TIMEOUT = 50;

    private static final String TGT_ID = "test";

    private TicketGrantingTicketExpirationPolicy expirationPolicy;

    private TicketGrantingTicketImpl ticketGrantingTicket;

    private static ServiceTicketSessionTrackingPolicy getTrackingPolicy() {
        val props = new CasConfigurationProperties();
        props.getTicket().getTgt().getCore().setOnlyTrackMostRecentSession(true);
        return new DefaultServiceTicketSessionTrackingPolicy(props, new DefaultTicketRegistry(mock(TicketSerializationManager.class), new DefaultTicketCatalog()));
    }

    @BeforeEach
    public void initialize() {
        this.expirationPolicy = new TicketGrantingTicketExpirationPolicy(HARD_TIMEOUT, SLIDING_TIMEOUT);
        this.ticketGrantingTicket = new TicketGrantingTicketImpl(TGT_ID, CoreAuthenticationTestUtils.getAuthentication(), this.expirationPolicy);
    }

    @Test
    void verifyTgtIsExpiredByHardTimeOut() {
        val creationTime = this.ticketGrantingTicket.getCreationTime();

        this.expirationPolicy.setClock(Clock.fixed(creationTime.plusSeconds(HARD_TIMEOUT).minusNanos(1).toInstant(), ZoneOffset.UTC));
        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(),
            expirationPolicy, false, getTrackingPolicy());
        assertFalse(ticketGrantingTicket.isExpired());

        this.expirationPolicy.setClock(Clock.fixed(creationTime.plusSeconds(HARD_TIMEOUT).plusNanos(1).toInstant(), ZoneOffset.UTC));
        assertTrue(ticketGrantingTicket.isExpired());
    }

    @Test
    void verifyTgtIsExpiredBySlidingWindow() {
        ticketGrantingTicket.grantServiceTicket(TGT_ID, RegisteredServiceTestUtils.getService(),
            expirationPolicy, false, getTrackingPolicy());

        this.expirationPolicy.setClock(Clock.fixed(this.ticketGrantingTicket.getLastTimeUsed().plusSeconds(SLIDING_TIMEOUT).minusNanos(1).toInstant(), ZoneOffset.UTC));
        assertFalse(ticketGrantingTicket.isExpired());

        this.expirationPolicy.setClock(Clock.fixed(this.ticketGrantingTicket.getLastTimeUsed().plusSeconds(SLIDING_TIMEOUT).plusNanos(1).toInstant(), ZoneOffset.UTC));
        assertTrue(ticketGrantingTicket.isExpired());
    }

    @Test
    void verifySerializeAnExpirationPolicyToJson() throws IOException {
        val policy = new TicketGrantingTicketExpirationPolicy(100, 100);
        MAPPER.writeValue(JSON_FILE, policy);
        val policyRead = MAPPER.readValue(JSON_FILE, TicketGrantingTicketExpirationPolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    void verifySerialization() {
        val result = SerializationUtils.serialize(expirationPolicy);
        val policyRead = SerializationUtils.deserialize(result, TicketGrantingTicketExpirationPolicy.class);
        assertEquals(expirationPolicy, policyRead);
    }
}
