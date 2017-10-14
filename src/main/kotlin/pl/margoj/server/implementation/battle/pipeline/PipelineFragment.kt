package pl.margoj.server.implementation.battle.pipeline

interface PipelineFragment<T>
{
    fun process(fragment: T)
}