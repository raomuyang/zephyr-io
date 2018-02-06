package cn.atomicer.skmq.sdk.socket2;

import cn.atomicer.skmq.sdk.coding.MessageEncoder;
import cn.atomicer.skmq.sdk.functions.Action;
import cn.atomicer.skmq.sdk.functions.Action2;
import cn.atomicer.skmq.sdk.model.Message;
import cn.atomicer.skmq.sdk.OneTimeServiceThread;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Rao-Mengnan
 * on 2018/2/1.
 */
public class SKClientTest {

    private static final Action2<ChannelHandlerContext, Throwable> ON_ERROR = new Action2<ChannelHandlerContext, Throwable>() {
        @Override
        public void doAction(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
            throwable.printStackTrace();
        }
    };


    @Test
    public void testClient() throws IOException, InterruptedException {
        int port = 1234;

        OneTimeServiceThread thread = new OneTimeServiceThread(port, null);
        thread.start();

        MessageAction2 checkMessage = new MessageAction2();
        SKClient client = new SKClient.Builder("127.0.0.1", port)
                .setAction(checkMessage, ON_ERROR)
                .setThread(2)
                .build();

        ChannelFuture future = client.newConnect();
        Channel channel = future.sync().channel();

        channel.writeAndFlush(OneTimeServiceThread.PING);
        Thread.sleep(200);
        client.shutdown();

        Assert.assertEquals(MessageEncoder.PING, thread.getQueue().poll());
        Assert.assertEquals(true, thread.getQueue().isEmpty());

        Assert.assertEquals(false, checkMessage.queue.isEmpty());
        Assert.assertEquals(MessageEncoder.PONG, checkMessage.queue.poll());
        Assert.assertEquals(true, checkMessage.queue.isEmpty());

    }

    class MessageAction2 implements Action2<ChannelHandlerContext, Message> {

        ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();

        @Override
        public void doAction(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
            queue.add(message);
        }
    }

}
