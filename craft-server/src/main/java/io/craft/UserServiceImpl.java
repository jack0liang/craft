package io.craft;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.craft.abc.UserService;
import io.craft.abc.constant.UserType;
import io.craft.abc.model.UserModel;
import io.craft.core.thrift.TException;
import io.craft.core.util.TraceUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
public class UserServiceImpl implements UserService {

    @Override
    public UserModel get(Long id) throws TException {
        UserModel model = new UserModel();
        model.setId(id);
        model.setName(UUID.randomUUID().toString());
        model.setSets(Sets.newHashSet(1,2,3));
        model.setMaps(TraceUtil.getCookie());
        model.setLists(Lists.newArrayList("a", "b"));
        model.setJoinDate(new Date());
        model.setUserType(UserType.A);

        return model;
    }

    @Override
    public UserModel gets(List<List<List<Long>>> ids) throws TException {
        return null;
    }

    @Override
    public void ping() throws TException {
        throw new TException("test exception");
        //logger.debug("ping, traceId={}", TraceUtil.getTraceId());
    }

}
