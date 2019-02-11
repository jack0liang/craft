package io.craft.core.codec;

import io.craft.core.constant.Constants;
import io.craft.core.message.CraftFramedMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.thrift.transport.TTransportException;

import java.util.List;

import static io.craft.core.constant.Constants.DEFAULT_MAX_FRAME_LENGTH;

public class CraftFramedMessageDecoder extends ByteToMessageDecoder {

    private int maxFrameLength;

    private ByteBuf readBuffer;

    private boolean isNewFrame;

    private int frameLength;

    private Long requestTime;

    public CraftFramedMessageDecoder(int maxFrameLength) {
        this.maxFrameLength = maxFrameLength;
        this.isNewFrame = true;
        this.frameLength = 0;
    }

    public CraftFramedMessageDecoder() {
        this(DEFAULT_MAX_FRAME_LENGTH);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.readBuffer = ctx.alloc().directBuffer(Constants.DEFAULT_BYTEBUF_SIZE);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (readBuffer.refCnt() > 0) {
            readBuffer.release(readBuffer.refCnt());
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        System.out.println("received bytes: " + in.readableBytes());

        readBuffer.writeBytes(in);

        while (true) {
            if (isNewFrame) {
                //收到第一个数据包, 记录请求开始时间
                requestTime = System.currentTimeMillis();
                if (readBuffer.readableBytes() < 4) {
                    //如果当前的缓冲区可读取字节小于int占用长度，此次无法读取帧大小，直接放弃
                    break;
                }
                //读取帧大小
                frameLength = readBuffer.readInt();

                if (frameLength < 0) {
                    throw new TTransportException(TTransportException.CORRUPTED_DATA, "Read a negative frame size (" + frameLength + ")!");
                }

                if (frameLength > maxFrameLength) {
                    //跳过帧长度
                    readBuffer.skipBytes(frameLength);
                    throw new TTransportException(TTransportException.CORRUPTED_DATA,
                            "Frame size (" + frameLength + ") larger than max length (" + maxFrameLength + ")!");
                }

                isNewFrame = false;
            }

            if (readBuffer.readableBytes() >= frameLength) {
                //当前帧数据读取完成
                try {
                    System.out.println(System.currentTimeMillis() + " get request");
                    ByteBuf buffer = ctx.alloc().directBuffer(frameLength);
                    readBuffer.readBytes(buffer);
                    out.add(new CraftFramedMessage(buffer, requestTime));
                    //一个请求读取完成后，就丢弃已读的内容
                    readBuffer.discardReadBytes();
                } finally {
                    isNewFrame = true;
                    frameLength = 0;
                }
            } else {
                //如果需要读取的数据没有帧大小，直接放弃
                break;
            }
        }
    }
}
