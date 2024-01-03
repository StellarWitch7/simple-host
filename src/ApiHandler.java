import io.undertow.server.HttpServerExchange;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ApiHandler extends RequestHandler {
    @Override
    protected void logic(HttpServerExchange exchange) throws Exception {
        throw new NotImplementedException();
    }
}
