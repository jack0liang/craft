package io.craft.core.config;

import com.alibaba.fastjson.JSON;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class EtcdClient implements ConfigClient<EtcdEvent>, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(EtcdClient.class);

    private static final Charset charset = Charset.forName("UTF-8");

    private static final int TTL = 5;//ttl=5

    private String[] endpoints;

    private boolean keepAlive;

    private Client client;

    private long leaseId;

    private CloseableClient keepAliveClient;

    private final Set<Watch.Watcher> watcherSet = new HashSet<>(0);

    public void setEndpoints(String[] endpoints) {
        this.endpoints = endpoints;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void init() throws ExecutionException, InterruptedException {
        this.client = Client.builder().endpoints(endpoints).build();
        if (this.keepAlive) {
            Lease lease = client.getLeaseClient();
            this.leaseId = lease.grant(TTL).get().getID();
            this.keepAliveClient = lease.keepAlive(this.leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
                @Override
                public void onNext(LeaseKeepAliveResponse value) {
                    logger.debug("keepAlive success:{}", value.toString());
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("keepAlive error:{}", t.getMessage(), t);
                }

                @Override
                public void onCompleted() {
                    logger.debug("keepAlive complete");
                }
            });
        } else {
            this.leaseId = 0;
            this.keepAliveClient = null;
        }
    }

    public String put(String key, String value) throws ExecutionException, InterruptedException {
        ByteSequence bKey = ByteSequence.from(key, charset);
        ByteSequence bValue = ByteSequence.from(value, charset);
        PutOption option;
        if (this.keepAlive) {
            option = PutOption.newBuilder().withLeaseId(leaseId).build();
        } else {
            option = PutOption.DEFAULT;
        }
        PutResponse response = client.getKVClient().put(bKey, bValue, option).get();
        if (response.hasPrevKv()) {
            return response.getPrevKv().getValue().toString(charset);
        } else {
            return null;
        }
    }

    public boolean delete(String key) throws ExecutionException, InterruptedException {
        ByteSequence bKey = ByteSequence.from(key, charset);
        DeleteResponse response = client.getKVClient().delete(bKey).get();
        return response.getDeleted() == 1;
    }

    public Properties watch(String prefix, Consumer<EtcdEvent> consumer) throws ExecutionException, InterruptedException {
        ByteSequence bPrefix = ByteSequence.from(prefix, charset);
        GetOption getOption = GetOption.newBuilder().withPrefix(bPrefix).build();
        GetResponse response = client.getKVClient().get(bPrefix, getOption).get();
        Watch.Listener listener = Watch.listener(
                resp -> {
                    EVENT_LOOP:
                    for (WatchEvent event : resp.getEvents()) {
                        String key = event.getKeyValue().getKey().toString(charset).substring(prefix.length());
                        String value;
                        String oldValue = event.getPrevKV().getValue().toString(charset);

                        logger.debug("event:{}", JSON.toJSONString(event));

                        switch (event.getEventType()) {

                            case DELETE:
                                value = null;
                                break;

                            case PUT:
                                value = event.getKeyValue().getValue().toString(charset);
                                break;

                            default:
                                logger.debug("discard unsupport event:{}", JSON.toJSONString(event));
                                continue EVENT_LOOP;
                        }

                        EtcdEvent ev = new EtcdEvent(key, value, oldValue);
                        consumer.accept(ev);
                    }
                },
                t -> {
                    logger.error("watch error prefix:{}, error:{}", prefix, t.getMessage(), t);
                }
        );
        WatchOption option = WatchOption.newBuilder().withPrefix(bPrefix).withPrevKV(true).withRevision(response.getHeader().getRevision()).build();
        Watch.Watcher watcher = client.getWatchClient().watch(bPrefix, option, listener);
        watcherSet.add(watcher);
        Properties properties = new Properties();
        for(KeyValue kv : response.getKvs()) {
            properties.setProperty(kv.getKey().toString(charset).substring(prefix.length()), kv.getValue().toString(charset));
        }
        return properties;
    }

    @Override
    public void close() throws IOException {
        if (keepAliveClient != null) {
            keepAliveClient.close();
        }
        try {
            for (Watch.Watcher watcher : watcherSet) {
                watcher.close();
            }
        } finally {
            client.close();
        }
    }

}
