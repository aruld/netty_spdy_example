package netty.spdy;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.spdy.SpdyFrameDecoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sslnpn.ssl.SSLEngineImpl;

public class HttpOrSpdyDecoder implements ChannelUpstreamHandler {

  Logger logger = LoggerFactory.getLogger(HttpOrSpdyDecoder.class);

  private SpdyFrameDecoder spdyFrameDecoder = new SpdyFrameDecoder();
  private HttpRequestDecoder httpRequestDecoder = new HttpRequestDecoder();

  @Override
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    SSLEngineImpl engine = (SSLEngineImpl) ctx.getPipeline().get(SslHandler.class).getEngine();

    String protocol = engine.getNegotiatedNextProtocol();
    logger.info("decoding event: {} based on protocol: {} ", e, protocol);
    if ("spdy/2".equals(protocol)) {
      spdyFrameDecoder.handleUpstream(ctx, e);
    } else {
      httpRequestDecoder.handleUpstream(ctx, e);
    }
  }
}
