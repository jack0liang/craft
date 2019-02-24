package io.craft;

import io.craft.abc.UserService;
import io.craft.abc.model.UserModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UserServiceImpl implements UserService {

    @Override
    public UserModel get(Long id) throws TException {
        logger.debug("get id = {}", id);
        return null;
    }

    @Override
    public UserModel gets(List<Long> ids) throws TException {
        logger.debug("gets ids size = {}", ids.size());
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void ping() throws TException {
        logger.debug("ping");
    }

}
