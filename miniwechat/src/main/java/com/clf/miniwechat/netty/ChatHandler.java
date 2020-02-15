package com.clf.miniwechat.netty;

import com.clf.miniwechat.enums.MsgActionEnum;
import com.clf.miniwechat.service.UserService;
import com.clf.miniwechat.utils.JsonUtils;
import com.clf.miniwechat.utils.SpringUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
    public static ChannelGroup users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {
        //获取客户端传输过来的消息
        String content = msg.text();
        Channel currentChannel = ctx.channel();
        //1. 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();
        //2. 判断消息类型,根据不同的类型处理不同的业务
        if(action == MsgActionEnum.CONNECT.type) {
            //2.1. 当websocket第一次open的时候,初始化channel, 把用户的channel和userId关联起来
            String senderId = dataContent.getChatMsgNio().getSenderId();
            UserChannelRel.put(senderId, currentChannel);
            for (Channel user : users) {
                log.info("============== websocket建立连接, channelId = "  + user.id().asLongText() + "=================");
            }
//            UserChannelRel.outPut(); // 输出所有userId和对应的channelId的信息
        } else if(action == MsgActionEnum.CHAT.type) {
            //2.2. 聊天类型的消息, 把聊天记录保存数据库, 同时标记消息的签收状态[未签收]
            ChatMsgNio chatMsgNio = dataContent.getChatMsgNio();
            String msgText = chatMsgNio.getMsg();
            String receiverId = chatMsgNio.getReceiverId();
            String senderId = chatMsgNio.getSenderId();

            //保存消息到数据库,并且标记为未签收
            UserService userService = (UserService) SpringUtils.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsgNio);
            chatMsgNio.setMsgId(msgId);

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setAction(MsgActionEnum.CHAT.type);
            dataContentMsg.setChatMsgNio(chatMsgNio);
            //发送消息
            //从全局用户channel关系中获取接受方的channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if(receiverChannel == null) {
                //TODO channel为空代表用户离线, 推送消息(第三方推送: JPush, 个推, 小米推送)
            } else {
                //当receiverChannel不为空的时候, 从ChannelGroup中去查找对应的Channel是否存在
                Channel findChannel = users.find(receiverChannel.id());
                log.info("接收消息: " + dataContentMsg.toString());
                if(findChannel != null) {
                    //用户在线
                    findChannel.writeAndFlush(
                            new TextWebSocketFrame(
                                    JsonUtils.objectToJson(dataContentMsg)));
                } else {
                    //用户离线 TODO 推送消息
                }
            }


        } else if(action == MsgActionEnum.SIGNED.type) {
            //2.3. 签收消息类型, 针对具体的消息进行签收, 修改数据库中对应消息的签收状态[已签收]
            UserService userService = (UserService) SpringUtils.getBean("userServiceImpl");
            //扩展字段在signed类型的消息中, 代表需要去签收的消息的id, 逗号间隔
            String msgIdsStr = dataContent.getExtend();
            String[] msgIds = msgIdsStr.split(",");
            List<String> msgIdList = new ArrayList<>();
            for (String msgId : msgIds) {
                if(StringUtils.isNotEmpty(msgId)) {
                    msgIdList.add(msgId);
                }
            }
            log.warn("签收消息的id: " + msgIdList.toString());
            if(msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
                //批量签收
                userService.updateMsgSigned(msgIdList);
            }
        } else if(action == MsgActionEnum.KEEPALIVE.type) {
            //2.4. 心跳类型的消息
            log.warn("收到来自channel为: [" + currentChannel + "] 的心跳包...." );
        }
    }

    /**
     * 当客户端连接服务器之后(打开链接)
     * 获取客户端的channel,并放到ChannelGroup中进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //当初发handlerRemoved, ChannelGroup会自动移除对应客户端的channel
        users.remove(ctx.channel());
        log.warn("客户端断开, channel对应的长id: " + ctx.channel().id().asLongText());
        log.warn("客户端断开, channel对应的短id: " + ctx.channel().id().asShortText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("连接发生异常...");
        cause.printStackTrace();
        //发生异常之后关闭channel,随后从ChannelGroup中移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
