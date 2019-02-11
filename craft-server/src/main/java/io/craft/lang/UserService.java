package io.craft.lang;

import io.craft.core.annotation.*;
import org.apache.thrift.TException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service(value = "io.craft.abc", version = "1.0.0")
@Label("用户服务")
public interface UserService {

    @Struct("io.craft.abc.model")
    @Label("用户数据模型")
    class UserModel {

        @Sequence(0)
        @Label("用户ID")
        Long id;

        @Sequence(1)
        @Label("用户名")
        String name;

        @Sequence(2)
        List<String> lists;

        @Sequence(3)
        Set<Integer> sets;

        @Sequence(4)
        Map<Double, Short> maps;

        @Sequence(5)
        Date joinDate;

    }

    @Struct("io.craft.abc.constant")
    @Label("用户类型")
    enum UserType {

        @Sequence(0)
        @Label("新客")
        A,

        @Sequence(1)
        @Label("老客")
        B,

        ;
    }

    @Provider(error = {
            @ErrorCode(value = 1001, label = "查询不到"),
            @ErrorCode(value = 1002, label = "用户已被停用")
    })
    UserModel get(@Sequence(value = 0) @Label("用户ID") Long id);

    @Provider
    void ping();

    @Provider
    @Label("查询用户")
    UserModel gets(@Sequence(value = 0, name = "ids") @Label("批量用户ID") List<Long> ids);

}
