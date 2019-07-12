/**
 * This file is part of Waarp Project.
 * <p>
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the COPYRIGHT.txt in the
 * distribution for a full listing of individual contributors.
 * <p>
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.openr66.proxy.network;

import io.netty.channel.Channel;
import org.waarp.openr66.protocol.utils.R66Future;
import org.waarp.openr66.proxy.configuration.Configuration;

/**
 * Proxy bridge between a request and a proxified server
 *
 * @author "Frederic Bregier"
 */
public class ProxyBridge {
    public static NetworkTransaction transaction = null;

    private ProxyEntry proxyEntry;
    private NetworkServerHandler source;
    private NetworkServerHandler proxified;
    private R66Future futureRemoteConnected = new R66Future(true);

    /**
     * @param proxyEntry
     * @param source
     */
    public ProxyBridge(ProxyEntry proxyEntry, NetworkServerHandler source) {
        this.proxyEntry = proxyEntry;
        this.source = source;
    }

    public static void initialize() {
        transaction = new NetworkTransaction();
    }

    public void initializeProxy() {
        Channel proxy = transaction.createConnectionWithRetry(proxyEntry.getRemoteSocketAddress(),
                                                              proxyEntry.isRemoteSsl());
        if (proxy == null) {
            // Can't connect ?
            this.futureRemoteConnected.cancel();
            return;
        }
        this.proxified =
                (NetworkServerHandler) proxy.pipeline()
                                            .get(NetworkServerInitializer.HANDLER);
        this.proxified.setBridge(this);
    }

    public void remoteConnected() {
        this.futureRemoteConnected.setSuccess();
    }

    public boolean waitForRemoteConnection() {
        try {
            this.futureRemoteConnected.await(Configuration.configuration.getTIMEOUTCON());
        } catch (InterruptedException e) {
        }
        if (!this.futureRemoteConnected.isSuccess()) {
            this.futureRemoteConnected.cancel();
            return false;
        }
        return true;
    }

    /**
     * @return the proxyEntry
     */
    public ProxyEntry getProxyEntry() {
        return proxyEntry;
    }

    /**
     * @return the source
     */
    public NetworkServerHandler getSource() {
        return source;
    }

    /**
     * @return the proxified
     */
    public NetworkServerHandler getProxified() {
        return proxified;
    }
}
