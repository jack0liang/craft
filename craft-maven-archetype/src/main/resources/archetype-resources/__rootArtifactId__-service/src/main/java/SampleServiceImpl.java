package ${package};

import io.craft.core.thrift.TException;

public class ServiceImpl implements SampleService {

    @Override
    public void ping() throws TException {
        System.out.println("call ping");
    }
}
