package io.craft.idl.meta;

import io.craft.idl.constant.ClassType;
import io.craft.idl.constant.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class MetaClass {

    @Builder.Default
    private String version = Constants.GENERATOR_VERSION;

    @Builder.Default
    private Date date = new Date();

    private Short sequence;

    private ClassType type;

    @Builder.Default
    private List<MetaClass> genericTypes = new ArrayList<>(0);

    private String packageName;

    private String className;

    private String fullClassName;

    private String name;

    private Boolean deprecated;

    private Boolean required;

    @Builder.Default
    private List<MetaClass> fields = new ArrayList<>(0);

    @Builder.Default
    private List<MetaMethod> methods = new ArrayList<>(0);


}
