package netty.spdy;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class SpdyServer {
  public static void main(String[] args) {
    ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()));

    bootstrap.setPipelineFactory(new SpdyServerPipelineFactory());
    bootstrap.bind(new InetSocketAddress(4567));


    ServerBootstrap bootstrap2 = new ServerBootstrap(new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()));

    bootstrap2.setPipelineFactory(new HttpServerPipelineFactory());
    bootstrap2.bind(new InetSocketAddress(4568));
    System.out.println("Server ready.");
  }
}
