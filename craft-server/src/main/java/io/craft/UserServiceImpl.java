package io.craft;

import io.craft.abc.UserService;
import io.craft.abc.model.UserModel;
import org.apache.thrift.TException;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserServiceImpl implements UserService {

    @Override
    public UserModel get(Long id) throws TException {
        System.out.println("get id=" + id);
        return null;
    }

    @Override
    public UserModel gets(List<Long> ids) throws TException {
        System.out.println("gets ids size=" + ids.size());
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void ping() throws TException {
        System.out.println("ping");
    }

}
