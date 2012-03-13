package netty.spdy;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.spdy.SpdyFrameEncoder;
import org.jboss.netty.handler.codec.spdy.SpdySessionHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import sslnpn.ssl.SSLEngineImpl;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import static org.jboss.netty.channel.Channels.pipeline;

public class SpdyServerPipelineFactory implements ChannelPipelineFactory {

  void initContext(SSLContext context) throws Exception {
    KeyStore ks = KeyStore.getInstance("PKCS12");
    InputStream fs = getClass().getClassLoader().getResource("server.pkcs12").openStream();
    try {
      ks.load(fs, "test123".toCharArray());
    } finally {
      fs.close();
    }
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, "test123".toCharArray());
    context.init(kmf.getKeyManagers(), new TrustManager[]{}, new SecureRandom());
  }

  SSLEngine createSSLEngine() throws Exception {
    SSLContext context = SSLContext.getInstance("TLS", new sslnpn.net.ssl.internal.ssl.Provider());
    initContext(context);

    SSLEngine engine = context.createSSLEngine();
    SSLEngineImpl npnEngine = (sslnpn.ssl.SSLEngineImpl) engine;
    npnEngine.setAdvertisedNextProtocols("spdy/2", "http/1.1");
    engine.setUseClientMode(false);
    return engine;
  }

  public ChannelPipeline getPipeline() throws Exception {
    SSLEngine engine = createSSLEngine();
    ChannelPipeline pipe = pipeline();

    SslHandler sslHandler = new SslHandler(engine);
    sslHandler.setIssueHandshake(true);
    pipe.addLast("ssl", sslHandler);
    pipe.addLast("decoder", new HttpOrSpdyDecoder());
    pipe.addLast("spdy_encoder", new SpdyFrameEncoder());
    pipe.addLast("http_encoder", new HttpResponseEncoder());
    pipe.addLast("spdy_session_handler", new SpdySessionHandler(true));
    pipe.addLast("handler", new Handler());
    return pipe;
  }
}
