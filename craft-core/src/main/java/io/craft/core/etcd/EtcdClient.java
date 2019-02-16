package io.craft.core.etcd;

import com.alibaba.fastjson.JSON;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EtcdClient implements Closeable {

    private static final Logger logger = LoggerFactory.getLogger(EtcdClient.class);

    private static final Charset charset = Charset.forName("UTF-8");

    private static final int TTL = 5;//ttl=5

    private final Client client;

    private final boolean keepAlive;

    private final long leaseId;

    private final CloseableClient keepAliveClient;

    public EtcdClient(Client client, boolean keepAlive) throws ExecutionException, InterruptedException {
        this.client = client;
        this.keepAlive = keepAlive;
        if (this.keepAlive) {
            Lease lease = client.getLeaseClient();
            this.leaseId = lease.grant(TTL).get().getID();
            this.keepAliveClient = lease.keepAlive(this.leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
                @Override
                public void onNext(LeaseKeepAliveResponse value) {
                    logger.debug("refresh success:{}", value.toString());
                }

                @Override
                public void onError(Throwable t) {
                    logger.error("refresh error:{}", t.getMessage(), t);
                }

                @Override
                public void onCompleted() {
                    logger.debug("refresh complete");
                }
            });
        } else {
            this.leaseId = 0;
            this.keepAliveClient = null;
        }
    }

    public PutResponse put(String key, String value) throws ExecutionException, InterruptedException {
        ByteSequence bKey = ByteSequence.from(key, charset);
        ByteSequence bValue = ByteSequence.from(value, charset);
        PutOption option;
        if (this.keepAlive) {
            option = PutOption.newBuilder().withLeaseId(leaseId).build();
        } else {
            option = PutOption.DEFAULT;
        }
        return client.getKVClient().put(bKey, bValue, option).get();
    }

    public DeleteResponse delete(String key) throws ExecutionException, InterruptedException {
        ByteSequence bKey = ByteSequence.from(key, charset);
        return client.getKVClient().delete(bKey).get();
    }

    public Watch.Watcher watch(String prefix, Consumer<EtcdEvent> consumer) {
        ByteSequence bPrefix = ByteSequence.from(prefix, charset);
        Watch.Listener listener = Watch.listener(
                resp -> {
                    for (WatchEvent event : resp.getEvents()) {
                        String key = event.getKeyValue().getKey().toString(charset);

                        logger.debug("event:{}", JSON.toJSONString(event));

                        switch (event.getEventType()) {

                            case DELETE:
                                consumer.accept(new EtcdEvent(key, null));
                                break;

                            case PUT:
                                consumer.accept(new EtcdEvent(key, event.getKeyValue().getValue().toString(charset)));
                                break;

                            default:
                                logger.debug("discard unsupport event:{}", JSON.toJSONString(event));
                        }
                    }
                },
                t -> {
                    logger.error("watch error prefix:{}, error:{}", prefix, t.getMessage(), t);
                }
        );
        WatchOption option = WatchOption.newBuilder().withPrefix(bPrefix).build();
        return client.getWatchClient().watch(bPrefix, option, listener);
    }

    @Override
    public void close() throws IOException {
        if (keepAliveClient != null) {
            keepAliveClient.close();
        }
        client.close();
    }

}
