package pt.ulisboa.tecnico.cnv.scaling.loadbalancer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.apache.commons.io.IOUtils;
import pt.ulisboa.tecnico.cnv.scaling.ScalingInstance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

/** Handles the scan requests that the LoadBalancer receives from Clients. */
public class LBScanHandler implements HttpHandler {

  private final LoadBalancer loadBalancer;

  public LBScanHandler(Map<String, ScalingInstance> instances, Level level) {
    this.loadBalancer = new LoadBalancer(instances, level);
  }

  /**
   * Extracts the request query, converts it in the LoadBalancer representation of a request, and
   * calls the auxiliary Method: sendRequest.
   *
   * @param t Encapsulates an HTTP request.
   */
  @Override
  public void handle(final HttpExchange t) throws IOException {

    final String query = t.getRequestURI().getQuery();

    UserRequest uRequest = UserRequest.parseFromQuery(query);
    if (uRequest == null) {
      t.sendResponseHeaders(HttpURLConnection.HTTP_BAD_REQUEST, 0);
      t.close();
      return;
    }
    this.sendRequest(t, query, uRequest);
  }

  /**
   * Forwards the Incoming Request to the WebServer that has the lowest associated cost and adds the
   * estimated cost of the request to it. When the WebServer responds, it responds to the client
   * with the image that the WebServer returned and removes the cost associated with the request
   * from the WebServer to which it forwarded the Request.
   *
   * @param t Encapsulates an HTTP request.
   * @param query Incoming Request query.
   * @param uRequest LoadBalancer representation of the incoming request.
   */
  private void sendRequest(HttpExchange t, String query, UserRequest uRequest) throws IOException {
    Map.Entry<ScalingInstance, Double> instanceRequestCost =
        loadBalancer.onReceiveRequest(uRequest);
    if (instanceRequestCost == null) {
      t.sendResponseHeaders(HttpURLConnection.HTTP_UNAVAILABLE, 0);
      t.close();
      return;
    }
    String instanceDns = instanceRequestCost.getKey().getPublicDnsName();
    String URL = String.format("http://%s:%d/scan?%s", instanceDns, 8000, query);
    try {
      this.forwardRequest(t, URL);
      loadBalancer.onInstanceSuccess(query, instanceRequestCost);
    } catch (IOException e) {
      loadBalancer.onInstanceFailure(instanceRequestCost);
      this.sendRequest(t, query, uRequest);
    }
  }

  /**
   * Forwards the incoming request to the WebServer with the URL provided.
   *
   * @param t Encapsulates an HTTP request.
   * @param URL WebServer URL that has the lowest associated cost.
   */
  private void forwardRequest(HttpExchange t, String URL) throws IOException {
    HttpURLConnection con = (HttpURLConnection) new URL(URL).openConnection();
    con.setRequestMethod("GET");

    if (con.getResponseCode() != HttpURLConnection.HTTP_OK) throw new IOException();
    final InputStream is = con.getInputStream();
    final OutputStream os = t.getResponseBody();

    final Headers hdrs = t.getResponseHeaders();

    hdrs.add("Content-Type", "image/png");
    hdrs.add("Access-Control-Allow-Origin", "*");
    hdrs.add("Access-Control-Allow-Credentials", "true");
    hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
    hdrs.add(
        "Access-Control-Allow-Headers",
        "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

    t.sendResponseHeaders(HttpURLConnection.HTTP_OK, con.getContentLength());
    IOUtils.copy(is, os);
  }
}
