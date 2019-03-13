package io.craft.idl.generator;

import io.craft.core.annotation.*;
import io.craft.idl.constant.ClassType;
import io.craft.idl.meta.MetaClass;
import io.craft.idl.meta.MetaMethod;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import static io.craft.core.constant.Constants.SEQUENCE_START_OFFSET;

@SuppressWarnings({"unchecked"})
public abstract class Generator {

    private Class clazz;

    public Generator(Class clazz) {
        this.clazz = clazz;
    }

    private MetaClass parse() throws Exception {
        if (!clazz.isInterface()) {
            throw new Exception("类 " + clazz.getName() + "不是一个接口");
        }
        Service service = (Service) clazz.getAnnotation(Service.class);
        if (service == null) {
            throw new Exception("类" + clazz.getName() + "缺少@Service注解");
        }
        String packageName = service.value();
        if (StringUtils.isEmpty(packageName)) {
            throw new Exception("类" + clazz.getName() + "的@Service注解缺少value");
        }

        Annotation deprecated = clazz.getAnnotation(Deprecated.class);

        List<MetaMethod> serviceMethods = new ArrayList<>();

        MetaClass serviceClass = MetaClass.builder()
                                            .packageName(packageName)
                                            .className(clazz.getSimpleName())
                                            .fullClassName(packageName + "." + clazz.getSimpleName())
                                            .deprecated((deprecated != null))
                                            .methods(serviceMethods)
                                            .build();

        //遍历方法
        for(Method method : clazz.getDeclaredMethods()) {
            Provider provider = method.getAnnotation(Provider.class);
            if (provider == null) {
                //如果没有provider注解， 则忽略
                continue;
            }

            Required returnValueRequired = method.getAnnotation(Required.class);

            //处理返回值
            MetaClass returnValue = parse(method.getGenericReturnType());

            //设定默认的返回名称为success
            returnValue.setName("success");
            returnValue.setSequence((short) 0);
            returnValue.setRequired(returnValueRequired != null && returnValueRequired.value());

            //处理参数
            Set<Integer> idSet = new HashSet<>();
            List<MetaClass> metaParameters = new ArrayList<>();

            for(Parameter parameter : method.getParameters()) {
                Sequence sequence = parameter.getAnnotation(Sequence.class);
                if (sequence == null) {
                    throw new Exception(clazz.getName() + "#" + method.getName() + " 参数 " + parameter.getName() + " 缺少@Sequence注解");
                }
                int seq = sequence.value();
                if (idSet.contains(seq)) {
                    throw new Exception(clazz.getName() + "#" + method.getName() + " 参数 " + parameter.getName() + " @Sequence注解value重复");
                }
                if (seq < SEQUENCE_START_OFFSET || seq > Short.MAX_VALUE) {
                    throw new Exception(clazz.getName() + "#" + method.getName() + " 参数 " + parameter.getName() + " @Sequence注解value超过范围：["+ SEQUENCE_START_OFFSET +"-"+Short.MAX_VALUE+"]");
                }
                Required required = parameter.getAnnotation(Required.class);
                idSet.add(seq);
                MetaClass parameterClass = parse(parameter.getParameterizedType());
                parameterClass.setName(StringUtils.isNotEmpty(sequence.name()) ? sequence.name() : parameter.getName());
                parameterClass.setDeprecated(parameter.getAnnotation(Deprecated.class) != null);
                parameterClass.setRequired(required != null ? required.value() : false);
                parameterClass.setSequence((short) seq);
                metaParameters.add(parameterClass);
            }

            //排序参数
            Collections.sort(metaParameters, Comparator.comparing(MetaClass::getSequence));



            MetaMethod metaMethod = new MetaMethod(method.getName(), returnValue, metaParameters, (method.getAnnotation(Deprecated.class) != null));

            serviceMethods.add(metaMethod);
        }

        processService(serviceClass);

        return serviceClass;
    }

    private MetaClass parse(Class cls) throws Exception {
        boolean deprecated = cls.getAnnotation(Deprecated.class) != null;

        Struct struct = (Struct) cls.getAnnotation(Struct.class);

        MetaClass metaClass = MetaClass.builder()
                .deprecated(deprecated)
                .type(ClassType.findByClass(cls))
                .fields(new ArrayList<>())
                .methods(new ArrayList<>())
                .build();

        if (
                cls.equals(Boolean.class) ||
                        cls.equals(Byte.class) ||
                        cls.equals(Double.class) ||
                        cls.equals(Short.class) ||
                        cls.equals(Integer.class) ||
                        cls.equals(Long.class) ||
                        cls.equals(String.class) ||
                        cls.equals(Date.class)
                ) {
            //基本类型不用生成，直接返回即可
            metaClass.setClassName(cls.getSimpleName());
            metaClass.setFullClassName(cls.getName());
        } else if (struct != null) {
            String packageName = struct.value();
            if (StringUtils.isEmpty(packageName)) {
                throw new Exception("@Struct注解value必须是正确的包名");
            }
            metaClass.setPackageName(packageName);
            Set<Integer> idSet = new HashSet<>();
            for (Field field : cls.getDeclaredFields()) {
                if (cls.isEnum() && field.getName().equals("$VALUES")) {
                    continue;
                }
                Sequence sequence = field.getAnnotation(Sequence.class);
                if (sequence == null) {
                    throw new Exception(cls.getName() + "." + field.getName() + " 缺少@Sequence注解");
                }
                int seq = sequence.value();
                if (idSet.contains(seq)) {
                    throw new Exception(cls.getName() + "." + field.getName() + " @Sequence注解value重复");
                }
                if (seq < 0 || seq > Short.MAX_VALUE) {
                    throw new Exception(cls.getName() + "." + field.getName() + " @Sequence注解value超过范围：[0-"+Short.MAX_VALUE+"]");
                }
                Required required = field.getAnnotation(Required.class);
                idSet.add(seq);
                MetaClass fieldClass;
                if (cls.isEnum()) {
                    fieldClass = MetaClass.builder().className(field.getName()).build();
                } else {
                    fieldClass = parse(field.getGenericType());
                }
                fieldClass.setName(field.getName());
                fieldClass.setDeprecated(field.getAnnotation(Deprecated.class) != null);
                fieldClass.setRequired(required != null ? required.value() : false);
                fieldClass.setSequence((short) seq);
                metaClass.getFields().add(fieldClass);
            }

            //排序字段
            Collections.sort(metaClass.getFields(), Comparator.comparing(MetaClass::getSequence));

            metaClass.setClassName(cls.getSimpleName());
            metaClass.setFullClassName(packageName + "." + metaClass.getClassName());

            if (cls.isEnum()) {
                processEnum(metaClass);
            } else {
                processStruct(metaClass);
            }

        } else if (cls.getSimpleName().equals("void")) {
            metaClass.setFullClassName("void");
            metaClass.setClassName("void");
        } else {
            throw new Exception("未知类型：" + cls);
        }

        return metaClass;
    }

    private MetaClass parse(ParameterizedType type) throws Exception {
        String typeName = type.getRawType().getTypeName();

        List<String> actualTypeNames = new ArrayList<>();
        List<MetaClass> genericTypes = new ArrayList<>();
        for(Type actualType : type.getActualTypeArguments()) {
            MetaClass mtClass = parse(actualType);
            actualTypeNames.add(mtClass.getFullClassName());
            genericTypes.add(mtClass);
        }

        MetaClass metaClass;

        //针对泛型，必须是Set/List/Map类型，不能是其他类型
        if (type.getRawType() instanceof Class) {
            //如果泛型的原始类型是Class
            //只能是Set，List，Map其中之一
            Class cls = (Class) type.getRawType();
            if (!(cls.isAssignableFrom(Set.class) || cls.isAssignableFrom(List.class) || cls.isAssignableFrom(Map.class))) {
                throw new Exception("不受支持的泛型类型：" + typeName + ", 只支持Set, List, Map");
            }

            metaClass = MetaClass.builder()
                    .type(ClassType.findByClass(cls))
                    .genericTypes(genericTypes)
                    .className(cls.getName() + "<" + StringUtils.join(actualTypeNames, ", ") + ">")
                    .build();
            metaClass.setFullClassName(metaClass.getClassName());

        } else {
            throw new Exception("不受支持的泛型类型：" + typeName + ", 只支持Set, List, Map");
        }

        return metaClass;
    }

    private MetaClass parse(Type type) throws Exception {
        if (type instanceof ParameterizedType) {
            return parse((ParameterizedType) type);
        } else if (type instanceof Class) {
            return parse((Class) type);
        } else {
            throw new Exception("不支持的类型：" + type);
        }
    }


    public void process() throws Exception {
        parse();
    }

    abstract protected void processService(MetaClass metaClass) throws Exception;

    abstract protected void processStruct(MetaClass metaClass) throws Exception;

    abstract protected void processEnum(MetaClass metaClass) throws Exception;

}
