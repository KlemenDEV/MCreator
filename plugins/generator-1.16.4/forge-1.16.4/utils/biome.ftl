<#include "entity.ftl">

<#function getEntitiesOfType entityList type>
    <#assign retval = []>
    <#list entityList as entity>
        <#if entity.spawnType == type>
            <#assign retval = retval + [entity]>
        </#if>
    </#list>
    <#return retval>
</#function>

<#macro generateEntityList entityList type>
    <#assign entities = getEntitiesOfType(entityList, type)>
    <#list entities as entry>
    <#-- @formatter:off -->
    {
        "type": "${entityToRegistryName(entry.entity)}",
        "weight": ${entry.weight},
        "minCount": ${entry.minGroup},
        "maxCount": ${entry.maxGroup}
    }<#if entry?has_next>,</#if>
    <#-- @formatter:on -->
    </#list>
</#macro>