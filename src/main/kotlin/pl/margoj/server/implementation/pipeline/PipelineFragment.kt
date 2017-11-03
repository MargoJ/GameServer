package pl.margoj.server.implementation.pipeline

interface PipelineFragment<T>
{
    fun process(fragment: T)
}