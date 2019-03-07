package io.craft.core.lbschedule;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class WeightedRoundRobinSchedule<E extends WeightedNode> extends RoundRobinSchedule<E> {

    private static final Logger logger = LoggerFactory.getLogger(WeightedRoundRobinSchedule.class);

    public WeightedRoundRobinSchedule(E[] nodes) {
        super(nodes);
    }

    @Override
    public void set(E[] nodes) {
        int size = 0;
        for(E e : nodes) {
            int weight = e.weight();
            if (weight <= 0) {
                logger.debug("node added ignore, weight={}", weight);
                continue;
            }
            size += weight;
        }
        E[] vnodes = (E[]) Array.newInstance(nodes.getClass().getComponentType(), size);
        int offset = 0;
        for(E e : nodes) {
            int weight = e.weight();
            if (weight <= 0) {
                continue;
            }
            Arrays.fill(vnodes, offset, offset + weight, e);
            offset += weight;
        }
        super.set(nodes);
    }

}
