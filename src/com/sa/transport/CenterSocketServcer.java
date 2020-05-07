/**
 * 
 * 项目名称:[NettyServer]
 * 包:	 [com.sa.transport]
 * 类名称: [Chat2Server]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [Y.P]
 * 创建时间:[2017年7月17日 上午10:29:59]
 * 修改人: [Y.P]
 * 修改时间:[2017年7月17日 上午10:29:59]
 * 修改备注:[说明本次修改内容]  
 * 版本:	 [v1.0]   
 * 
 */
package com.sa.transport;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sa.net.codec.PacketDecoder;
import com.sa.net.codec.PacketEncoder;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

public class CenterSocketServcer implements Runnable {
	private int port = 0;

	public CenterSocketServcer(int port) {
		this.port = port;
	}

	public void bind() throws IOException {
		// 避免使用默认线程数参数
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
		System.err.println("CenterSocketServcer服务端已启动，正在监听中心的请求......");
		try {
			ServerBootstrap b = new ServerBootstrap();

			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024*10).childHandler(new ChildChannelHandler());

			// ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);

			ChannelFuture f = b.bind(new InetSocketAddress(this.port)).sync();
			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel arg0) throws Exception {
			ChannelPipeline pipeline = arg0.pipeline();
			pipeline.addLast(new PacketDecoder(1024*100, 0, 4, 0, 4));
			pipeline.addLast(new LengthFieldPrepender(4));
			pipeline.addLast(new PacketEncoder());
			// 当有操作操作超出指定空闲秒数时，便会触发UserEventTriggered事件
			pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0));
			pipeline.addLast(new CenterSocketServcerHandler());
		}
	}

	@Override
	public void run() {
		try {
			this.bind();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
