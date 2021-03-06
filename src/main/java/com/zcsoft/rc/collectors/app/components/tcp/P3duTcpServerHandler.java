package com.zcsoft.rc.collectors.app.components.tcp;

import com.sharingif.cube.security.binary.HexCoder;
import com.zcsoft.rc.collectors.api.rtk.entity.RtkCollectReq;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P3duTcpServerHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private HexCoder hexCoder = new HexCoder();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        byte[] msgByte = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msgByte);
        logger.info("received msg:{}",new String(msgByte));
        logger.info("received msg byte, msg:{}", hexCoder.encode(msgByte));

        String msgString = new String(msgByte);
        String[] msgArray = msgString.split("\r\n");

        String sn = msgArray[0];
        sn = sn.substring(4,sn.length()-3);

        String gphcd = msgArray[1];
        String[] gphcdArray = gphcd.split(",");
        double longitude = Double.valueOf(gphcdArray[10]);
        double latitude = Double.valueOf(gphcdArray[9]);

        RtkCollectReq req = new RtkCollectReq();
        req.setId(sn);
        req.setLongitude(longitude);
        req.setLatitude(latitude);

        logger.info("rtk req:{}",req);
        req.setLongitude(Double.valueOf(GPSFormatUtils.GPHCDFormat(req.getLongitude().toString())));
        req.setLatitude(Double.valueOf(GPSFormatUtils.GPHCDFormat(req.getLatitude().toString())));
        logger.info("rtk req转换后信息:{}",req);

        TcpServerApplication.collect(req);


    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        logger.info("added channel,channelId:{}", channel.id().asLongText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        logger.info("removed channel,channelId:{}", ctx.channel().id().asLongText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("channel connection error,channelId:{}", ctx.channel().id().asLongText(), cause);
        ctx.channel().close();
    }

}
