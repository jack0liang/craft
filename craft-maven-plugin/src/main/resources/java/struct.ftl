<#import "common.ftl" as common>
package ${packageName};

<#if deprecated>
@java.lang.Deprecated
</#if>
public <@common.struct className fields />