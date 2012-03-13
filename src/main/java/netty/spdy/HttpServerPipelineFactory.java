package netty.spdy;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import static org.jboss.netty.channel.Channels.pipeline;

public class HttpServerPipelineFactory implements ChannelPipelineFactory {

  @Override
  public ChannelPipeline getPipeline() throws Exception {
    ChannelPipeline pipe = pipeline();
    pipe.addLast("decoder", new HttpRequestDecoder());
    pipe.addLast("http_encoder", new HttpResponseEncoder());
    pipe.addLast("handler", new Handler());
    return pipe;
  }
}
