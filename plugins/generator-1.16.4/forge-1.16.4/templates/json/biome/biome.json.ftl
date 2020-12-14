<#-- @formatter:off -->
<#include "../../biome.ftl">
{
    "scale": ${data.heightVariation},
    "depth": ${data.baseHeight},
    "precipitation": <#if (data.rainingPossibility > 0)><#if (data.temperature > 0.15)>"rain"<#else>"snow"</#if><#else>"none"</#if>,
    "temperature": ${data.temperature},
    "downfall": ${data.rainingPossibility},
    "category": "${data.biomeCategory?replace("THEEND", "THE_END")?lower_case}",
	"surface_builder": "${modid}:${registryname}",
	"spawn_costs": {},
    "player_spawn_friendly": true,
	<#if data.parent?? && data.parent.getUnmappedValue() != "No parent">
	"parent": "${data.parent}",
	</#if>
    "effects": {
    	"foliage_color": ${data.foliageColor?has_content?then(data.foliageColor.getRGB(), 10387789)},
    	"grass_color": ${data.grassColor?has_content?then(data.grassColor.getRGB(), 9470285)},
    	"sky_color": ${data.airColor?has_content?then(data.airColor.getRGB(), 7972607)},
    	"fog_color": ${data.airColor?has_content?then(data.airColor.getRGB(), 12638463)},
    	"water_color": ${data.waterColor?has_content?then(data.waterColor.getRGB(), 4159204)},
    	"water_fog_color": ${data.waterFogColor?has_content?then(data.waterFogColor.getRGB(), 329011)}
    },
	"spawners": {
		"monster": [<@generateEntityList data.spawnEntries "monster"/>],
		"creature": [<@generateEntityList data.spawnEntries "creature"/>],
		"ambient": [<@generateEntityList data.spawnEntries "ambient"/>],
		"water_creature": [<@generateEntityList data.spawnEntries "waterCreature"/>],
		"water_ambient": [],
		"misc": []
	},
    "carvers": {
		<#if data.defaultFeatures?contains("Caves")>
    	"air": [
            "minecraft:cave",
            "minecraft:canyon"
    	]
		</#if>
    },
    "features": [
    	<#--RAW_GENERATION-->[],
		<#--LAKES-->[
		<#if data.defaultFeatures?contains("Lakes")>
			"minecraft:lake_water",
			"minecraft:lake_lava"
		</#if>
    	],
		<#--LOCAL_MODIFICATIONS-->[],
		<#--UNDERGROUND_STRUCTURES-->[
		<#if data.defaultFeatures?contains("MonsterRooms")>
			"minecraft:monster_room"
		</#if>
    	],
		<#--SURFACE_STRUCTURES-->[],
		<#--STRONGHOLDS-->[],
		<#--UNDERGROUND_ORES-->[
		<#if data.defaultFeatures?contains("Ores")>
			"minecraft:ore_dirt",
			"minecraft:ore_gravel",
			"minecraft:ore_granite",
			"minecraft:ore_diorite",
			"minecraft:ore_andesite",
			"minecraft:ore_coal",
			"minecraft:ore_iron",
			"minecraft:ore_gold",
			"minecraft:ore_redstone",
			"minecraft:ore_diamond",
			"minecraft:ore_lapis"
		</#if>
    	],
		<#--UNDERGROUND_DECORATION-->[],
		<#--VEGETAL_DECORATION-->[],
		<#--TOP_LAYER_MODIFICATION-->[
			"minecraft:freeze_top_layer"
    	]
    ],
    "starts": [
    	<#if data.spawnWoodlandMansion>
    	"minecraft:mansion",
    	</#if>
    	<#if data.spawnMineshaft>
    	"minecraft:mineshaft" <#if (data.spawnStronghold) || (data.spawnPillagerOutpost) || (data.spawnShipwreck) || (data.oceanRuinType != "NONE") || (data.spawnOceanMonument) || (data.spawnDesertPyramid) || (data.spawnJungleTemple) || (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.spawnStronghold>
    	"minecraft:stronghold" <#if (data.spawnPillagerOutpost) || (data.spawnShipwreck) || (data.oceanRuinType != "NONE") || (data.spawnOceanMonument) || (data.spawnDesertPyramid) || (data.spawnJungleTemple) || (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.spawnPillagerOutpost>
    	"minecraft:pillager_outpost" <#if (data.spawnShipwreck) || (data.oceanRuinType != "NONE") || (data.spawnOceanMonument) || (data.spawnDesertPyramid) || (data.spawnJungleTemple) || (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.spawnShipwreck>
    	"minecraft:shipwreck" <#if (data.oceanRuinType != "NONE") || (data.spawnOceanMonument) || (data.spawnDesertPyramid) || (data.spawnJungleTemple) || (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.oceanRuinType != "NONE">
    	"minecraft:ocean_ruin_${Pdata.oceanRuinType?lower_case}" <#if (data.spawnOceanMonument) || (data.spawnDesertPyramid) || (data.spawnJungleTemple) || (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.spawnOceanMonument>
    	"minecraft:monument" <#if (data.spawnDesertPyramid) || (data.spawnJungleTemple) || (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.spawnDesertPyramid>
    	"minecraft:desert_pyramid" <#if (data.spawnJungleTemple) || (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.spawnJungleTemple>
    	"minecraft:jungle_pyramid" <#if (data.spawnIgloo) || (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.spawnIgloo>
    	"minecraft:igloo" <#if (data.villageType != "none")> , </#if>
    	</#if>
    	<#if data.villageType != "none">
    	"minecraft:village_${data.villageType}"
    	</#if>
    ]
}
<#-- @formatter:on -->
