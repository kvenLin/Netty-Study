package com.clf.miniwechat.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * @Author: clf
 * @Date: 19-12-30
 * @Description:
 */
public class WSServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();

        //WebSocket基于http协议,所以要有http的编解码器
        pipeline.addLast(new HttpServerCodec());

        //对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());

        /**
         * 对HttpMessage进行聚合,聚合成FullHttpRequest或FullHttpResponse
         * 几乎在netty中的编解码都会使用到此handler
         */
        pipeline.addLast(new HttpObjectAggregator(1024 * 64));

        //====================== 以上是用于支持http协议 ======================

        //====================== 增加心跳支持 start ======================

        //针对客户端, 如果在1分钟没有没有想客户端发送读写心跳, 则主动断开
        //如果读空闲/写空闲, 不做处理
        pipeline.addLast(new IdleStateHandler(8, 10, 12));
        //自定义的空闲状态检测
        pipeline.addLast(new HeartBeatHandler());

        //====================== 增加心跳支持 end ======================


        /**
         * WebSocket 服务器处理的协议,用于指定给客户端连接访问的路由: /ws
         * 本handler会帮你处理一些繁重的复杂的事
         * 会帮你处理握手动作: handshaking (close, ping, pong) ping + pong = 心跳
         * 对于WebSocket来讲, 都是以frames进行传输,不同的数据类型对应的frames也不同
         */
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //自定义的handler, 读取客户端的消息
        pipeline.addLast(new ChatHandler());
    }
}
