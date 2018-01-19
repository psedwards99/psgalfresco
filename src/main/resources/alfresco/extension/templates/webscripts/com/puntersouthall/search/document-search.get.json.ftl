<#macro renderValue value><#if value?is_boolean>${value?string}<#elseif value?is_number>${value?c}<#elseif value?is_date>"${xmldate(value)}"<#elseif value?is_string>"${value}"<#elseif value?is_sequence>[<#list value as v><@renderValue v/><#if v_has_next>,</#if></#list>]</#if></#macro>

<#escape x as jsonUtils.encodeJSONString(x)>
[
<#list results as res>
    {
    	"nodeRef": <#if res.nodeRef?exists>"${res.nodeRef}"<#else>null</#if>,
    <#list fields as f>
        "${f}": <#if res.properties[f]?exists><@renderValue res.properties[f]/><#else>null</#if><#if f_has_next>,</#if>
    </#list>
    }<#if res_has_next>,</#if>
</#list>
]
</#escape>
