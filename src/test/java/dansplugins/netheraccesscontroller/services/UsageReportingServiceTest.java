package dansplugins.netheraccesscontroller.services;

import com.sun.net.httpserver.HttpServer;
import dansplugins.netheraccesscontroller.trace.TraceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Covers {@link UsageReportingService#applyConfig}, which is what makes a usage-reporting change
 * made with /nac config set take effect while the server runs (#105). The damaging case is the
 * opt-out: an operator who turned reporting off and was shown false must not have events sent.
 *
 * The client is the real one and reports to a real HTTP server on a loopback port, as in
 * TraceClientTest, so that what is asserted is what is sent. A DO_NOT_TRACK or
 * TRACE_USAGE_REPORTING on the machine running the suite turns every client off; the tests that
 * need one on are then skipped by {@link #assumeReportingIsOn}.
 */
class UsageReportingServiceTest {

    private HttpServer server;
    private final List<String> received = new CopyOnWriteArrayList<>();

    private boolean enabledInConfig = true;

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            received.add(readAll(exchange.getRequestBody()));
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    /**
     * The regression: the name reported after the change differs from the one before it, so that
     * a client left running would send it at once rather than count it as a repeat.
     */
    @Test
    void turningReportingOff_stopsWhatIsReportedAfterTheChange() {
        UsageReportingService service = serviceClosingOnTheCallingThread();
        service.applyConfig();
        assumeReportingIsOn(service);
        service.reportCommand("before");

        enabledInConfig = false;
        service.applyConfig();
        service.reportCommand("after");
        service.close();

        assertFalse(service.isEnabled());
        assertEquals(TraceClient.REASON_CONFIG, service.disabledReason());
        assertEquals(1, received.size(), "Only the report from before the change may be sent");
        assertTrue(received.get(0).contains("before"));
    }

    @Test
    void turningReportingOn_startsSendingWithoutARestart() {
        enabledInConfig = false;
        UsageReportingService service = serviceClosingOnTheCallingThread();
        service.applyConfig();
        service.reportCommand("before");

        enabledInConfig = true;
        service.applyConfig();
        assumeReportingIsOn(service);
        service.reportCommand("after");
        service.close();

        assertEquals(1, received.size(), "Only the report from after the change may be sent");
        assertTrue(received.get(0).contains("after"));
    }

    /**
     * close() can wait for seconds on a server that does not answer, and applyConfig runs inside a
     * command on the main thread, so the close has to be handed off rather than run here.
     */
    @Test
    void applyConfig_handsTheCloseOfTheReplacedClientToTheExecutor() {
        List<Runnable> handedOff = new ArrayList<>();
        UsageReportingService service = new UsageReportingService(this::client, handedOff::add);
        service.applyConfig();
        handedOff.clear();

        service.applyConfig();

        assertEquals(1, handedOff.size());
    }

    /**
     * The client's own window holds a repeat for up to a minute. Closing the replaced client sends
     * it, and the count is for commands run before the change.
     */
    @Test
    void applyConfig_sendsARepeatTheReplacedClientWasHolding() {
        UsageReportingService service = serviceClosingOnTheCallingThread();
        service.applyConfig();
        assumeReportingIsOn(service);
        service.reportCommand("repeated");
        service.reportCommand("repeated");

        enabledInConfig = false;
        service.applyConfig();

        assertEquals(2, received.size(), "The first report and the held repeat");
        assertTrue(received.get(1).contains("coalesced"));
    }

    @Test
    void beforeTheConfigIsApplied_nothingIsReported() {
        UsageReportingService service = serviceClosingOnTheCallingThread();

        service.reportStartup();
        service.reportCommand("nac");
        service.close();

        assertFalse(service.isEnabled());
        assertTrue(received.isEmpty());
    }

    private UsageReportingService serviceClosingOnTheCallingThread() {
        return new UsageReportingService(this::client, Runnable::run);
    }

    /** Built the way the plugin builds its client, from what is in the config at the time. */
    private TraceClient client() {
        return TraceClient.builder("http://127.0.0.1:" + server.getAddress().getPort(), "test", "1.0.0")
                .key("test-key")
                .enabled(enabledInConfig)
                .build();
    }

    private static void assumeReportingIsOn(UsageReportingService service) {
        assumeTrue(service.isEnabled(), "The environment has turned usage reporting off");
    }

    private static String readAll(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
}
