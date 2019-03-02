package io.craft.proxy.proxy;

import io.craft.core.message.CraftFramedMessage;
import io.craft.proxy.constant.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.util.Map;

public class ProxyImpl implements Proxy {

    private InetSocketAddress address;

    private EventLoopGroup executors;

    private ProxyPoolHandler handler;

    private ChannelPool pool;

    private Bootstrap bootstrap;


    public ProxyImpl(InetSocketAddress address, EventLoopGroup executors, ProxyPoolHandler handler) {
        this.address = address;
        this.executors = executors;
        this.handler = handler;

        this.bootstrap = new Bootstrap();
        this.bootstrap
                .group(this.executors)
                .remoteAddress(this.address)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                ;

        this.pool = new FixedChannelPool(this.bootstrap, this.handler, 2);
        handler.setPool(this.pool);
    }

    private void accept0(Channel server, Channel client, CraftFramedMessage message) {
        //Attribute<Channel> attrClient = server.attr(Constants.SERVER_ATTRIBUTE_CLIENT);
        //attrClient.set(client);

        server.writeAndFlush(message);
    }

    @Override
    public void accept(Channel client, CraftFramedMessage message) {
        Future<Channel> f = pool.acquire();
        if (f.isDone()) {
            accept0(f.getNow(), client, message);
        } else {
            f.addListener(future -> {
                accept0((Channel) future.getNow(), client, message);
            });
        }
    }

//    public static void main(String... args) throws Exception {
//        ProxyImpl proxy = new ProxyImpl(new InetSocketAddress("www.runoob.com", 80), new NioEventLoopGroup(), new ProxyPoolHandler());
//        ByteBuf buffer = Unpooled.buffer();
//        buffer.writeBytes(("GET /http/http-tutorial.html HTTP/1.1\n" +
//                "Host: www.runoob.com\n" +
//                "Connection: keep-alive\n" +
//                "Cache-Control: max-age=0\n" +
//                "Upgrade-Insecure-Requests: 1\n" +
//                "User-Agent: Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1\n" +
//                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" +
//                "Accept-Language: zh-CN,zh;q=0.9\n\n").getBytes());
//        proxy.accept(buffer);
//    }
}
