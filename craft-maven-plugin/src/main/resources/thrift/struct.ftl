struct ${className} {
<#list fields as field>
    ${field.sequence} : ${field.fullClassName} ${field.name}
</#list>
}