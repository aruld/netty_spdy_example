package netty.spdy;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.spdy.DefaultSpdyDataFrame;
import org.jboss.netty.handler.codec.spdy.DefaultSpdySynReplyFrame;
import org.jboss.netty.handler.codec.spdy.SpdySynStreamFrame;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler extends SimpleChannelUpstreamHandler {

  Logger logger = LoggerFactory.getLogger(Handler.class);

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    if (e.getMessage() instanceof SpdySynStreamFrame) {
      spdyResponse((SpdySynStreamFrame) e.getMessage(), e);
    } else if (e.getMessage() instanceof HttpRequest) {
      httpResponse((HttpRequest) e.getMessage(), e);
    }
  }

  void spdyResponse(SpdySynStreamFrame frame, MessageEvent e) {
    logger.info("Creating spdy response");
    DefaultSpdySynReplyFrame response = new DefaultSpdySynReplyFrame(frame.getStreamID());
    response.setLast(false);
    response.addHeader("status", "200 OK");
    response.addHeader("version", "HTTP/1.1");
    e.getChannel().write(response);

    DefaultSpdyDataFrame data = new DefaultSpdyDataFrame(frame.getStreamID());
    data.setLast(true);
    data.setData(ChannelBuffers.copiedBuffer("Served from SPDY", CharsetUtil.UTF_8));
    e.getChannel().write(data);
  }

  void httpResponse(HttpRequest req, MessageEvent e) {
    DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    response.setContent(ChannelBuffers.copiedBuffer("Served from HTTP", CharsetUtil.UTF_8));
    response.setHeader("Content-Length", response.getContent().readableBytes());
    response.setHeader("Alternate-Protocol", "443:npn-spdy/2");
    e.getChannel().write(response);

  }
}
