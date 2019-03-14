# craft
Rpc Service Framework

只支持binary protocol

几点要点:
1,IDL中定义方法参数的时候sequence必须大于10, 小于10的是预留的,不允许使用
2,如果要兼容thrift的话,thrift idl的方法参数中需要3个预置参数(1:string serviceName, 2:string traceId, 3:map<string,string> cookie)