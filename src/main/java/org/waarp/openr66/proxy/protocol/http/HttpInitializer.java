/**
 * This file is part of Waarp Project.
 * 
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.proxy.protocol.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Pipeline Factory for HTTP support
 * 
 * @author Frederic Bregier
 * 
 */
public class HttpInitializer extends ChannelInitializer<SocketChannel> {
    public boolean useHttpCompression = false;

    public HttpInitializer(boolean useHttpCompression) {
        this.useHttpCompression = useHttpCompression;
    }

    protected void initChannel(SocketChannel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new HttpServerCodec());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1048576));
        pipeline.addLast("streamer", new ChunkedWriteHandler());
        if (useHttpCompression) {
            pipeline.addLast("deflater", new HttpContentCompressor());
        }
        pipeline.addLast("handler", new HttpFormattedHandler());
    }
}
