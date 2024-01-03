import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;

public class CustomIoCallback implements IoCallback {
    @Override
    public void onComplete(HttpServerExchange exchange, Sender sender) {
        Main.processesRunning.decrementAndGet();
        exchange.endExchange();
    }

    @Override
    public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
        Main.processesRunning.decrementAndGet();
        exchange.endExchange();
        throw new RuntimeException(exception);
    }
}
