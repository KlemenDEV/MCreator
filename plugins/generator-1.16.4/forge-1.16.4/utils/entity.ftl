<#-- entity argument needs to be EntityEntry type -->
<#function entityToRegistryName entity>
    <#assign mappedString = entity.getMappedValue(2)>
    <#if mappedString.contains(".CustomEntity")>
        <#assign mappedString = mappedString?replace(".CustomEntity", "")>
        <#return generator.getResourceLocationForModElement(mappedString)>
    <#else>
        <#return "minecraft:" + mappedString>
    </#if>
</#function>