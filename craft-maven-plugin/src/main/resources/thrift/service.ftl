service ${className} {

    <#list methods as method>
        ${method.returnValue.fullClassName} ${method.name}(<#list method.parameters as parameter>${parameter.fullClassName} ${parameter.name}<#sep>, </#sep></#list>)<#sep>,</#sep>
    </#list>

}