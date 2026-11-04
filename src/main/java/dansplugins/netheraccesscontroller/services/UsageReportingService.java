package dansplugins.netheraccesscontroller.services;

import dansplugins.netheraccesscontroller.trace.TraceClient;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Holds the running {@link TraceClient}. A client reads its settings once, when it is built, so a
 * change to a usage-reporting option only reaches the running plugin by replacing the client.
 *
 * The replacement is what makes the in-game opt-out work: until it happens,
 * /nac config set usage-reporting.enabled false would list false in /nac config show while events
 * kept being sent.
 *
 * @author Daniel McCoy Stephenson
 */
public class UsageReportingService {
    private final Supplier<TraceClient> clientFactory;
    private final Executor closeExecutor;

    // A no-op until the config has been read, so a command arriving before
    // onEnable() finishes has something safe to report to.
    private TraceClient client = TraceClient.disabled();

    /**
     * @param clientFactory builds a client from the current config
     * @param closeExecutor runs the close of a replaced client; it must not be the main thread,
     *                      because close() can wait for seconds on a server that does not answer
     */
    public UsageReportingService(Supplier<TraceClient> clientFactory, Executor closeExecutor) {
        this.clientFactory = clientFactory;
        this.closeExecutor = closeExecutor;
    }

    /**
     * Replaces the running client with one built from the current config. The swap comes first, so
     * nothing reported after this call reaches the old client. Closing the old client sends any
     * counts it was still holding; those are for commands run before this call.
     */
    public void applyConfig() {
        TraceClient previous = client;
        client = clientFactory.get();
        closeExecutor.execute(previous::close);
    }

    public void reportStartup() {
        client.report("startup");
    }

    public void reportCommand(String commandName) {
        client.report("command", null, Collections.singletonMap("name", commandName));
    }

    public boolean isEnabled() {
        return client.isEnabled();
    }

    public String disabledReason() {
        return client.disabledReason();
    }

    public void close() {
        client.close();
    }
}
