package pl.margoj.server.implementation.battle.pipeline.strike.impl

import pl.margoj.server.implementation.battle.buff.TestBuff
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineData
import pl.margoj.server.implementation.battle.pipeline.strike.StrikePipelineFragment

class TestBuffModifierFragment: StrikePipelineFragment
{
    override fun process(fragment: StrikePipelineData)
    {
        val buff = fragment.strikeAbility.battle.getDataOf(fragment.strikeAbility.user)!!.getBuff(TestBuff::class.java)

        if(buff != null)
        {
            fragment.log.footshot = true
        }
    }
}