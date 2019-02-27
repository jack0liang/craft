package io.craft.idl.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class MetaMethod {

    private String name;

    private MetaClass returnValue;

    private List<MetaClass> parameters;

    private Boolean deprecated;

    private String visible;
}
