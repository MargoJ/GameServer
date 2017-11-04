package pl.margoj.server.implementation.item.pipeline

import pl.margoj.server.implementation.item.pipeline.drag.ItemDragPipelineData
import pl.margoj.server.implementation.item.pipeline.drag.impl.SplittableFragment
import pl.margoj.server.implementation.item.pipeline.use.ItemUsePipelineData
import pl.margoj.server.implementation.item.pipeline.use.impl.*
import pl.margoj.server.implementation.pipeline.Pipeline

object ItemPipelines
{
    val ITEM_USE_PIPELINE = Pipeline<ItemUsePipelineData>()
    val ITEM_DRAG_PIPELINE = Pipeline<ItemDragPipelineData>()

    init
    {
        ITEM_USE_PIPELINE.addLast("MJ|Check", CheckFragment())
        ITEM_USE_PIPELINE.addLast("MJ|Requirements", RequirementsFragment())
        ITEM_USE_PIPELINE.addLast("MJ|CooldownCheck", CooldownCheckFragment())

        ITEM_USE_PIPELINE.addLast("MJ|Equipment", EquipmentFragment())
        ITEM_USE_PIPELINE.addLast("MJ|Heal", HealFragment())
        ITEM_USE_PIPELINE.addLast("MJ|RunScript", RunScriptFragment())

        ITEM_USE_PIPELINE.addLast("MJ|Binding", BindingFragment())
        ITEM_USE_PIPELINE.addLast("MJ|PutOnCooldown", PutOnCooldownFragment())
        ITEM_USE_PIPELINE.addLast("MJ|TakeFragment", TakeFragment())

        ITEM_DRAG_PIPELINE.addLast("MJ|Splittable", SplittableFragment())
    }
}