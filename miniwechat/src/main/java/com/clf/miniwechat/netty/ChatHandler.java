package com.clf.miniwechat.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @Author: clf
 * @Date: 19-12-31
 * @Description: 处理消息的handler
 * TextWebSocketFrame: 在netty中, 用于为WebSocket专门处理文本的对象, frame是消息的载体
 */
@Slf4j
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /**
     * 用于记录和管理所有客户端的channel
     */
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {
        //获取客户端传输过来的消息
        String content = msg.text();
        log.info("接收到的数据: " + content);
//        for (Channel channel : clients) {
//            channel.writeAndFlush(new TextWebSocketFrame("[服务器接收到消息: ]"
//                    + LocalDateTime.now()
//                    + "接收到消息,消息为:" + content));
//        }
        //下面这个方法, 和上面的for循环, 一致
        clients.writeAndFlush(new TextWebSocketFrame("[服务器在]"
                + LocalDateTime.now()
                + "接收到消息,消息为:" + content));
    }

    /**
     * 当客户端连接服务器之后(打开链接)
     * 获取客户端的channel,并放到ChannelGroup中进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当初发handlerRemoved, ChannelGroup会自动移除对应客户端的channel
//        clients.remove(ctx.channel());
        log.warn("客户端断开, channel对应的长id: " + ctx.channel().id().asLongText());
        log.warn("客户端断开, channel对应的短id: " + ctx.channel().id().asShortText());
    }
}
