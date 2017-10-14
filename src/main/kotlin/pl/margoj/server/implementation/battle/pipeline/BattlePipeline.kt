package pl.margoj.server.implementation.battle.pipeline

import java.util.LinkedList

open class BattlePipeline<DataT>
{
    private val elements = LinkedList<InternalPipelineData<DataT>>()

    fun addFirst(name: String, fragment: PipelineFragment<DataT>)
    {
        this.elements.addFirst(InternalPipelineData(name, fragment))
    }

    fun addLast(name: String, fragment: PipelineFragment<DataT>)
    {
        this.elements.addLast(InternalPipelineData(name, fragment))
    }

    fun addBefore(before: String, name: String, fragment: PipelineFragment<DataT>)
    {
        var beforeElement: InternalPipelineData<DataT>
        var index = 0

        for ((i, element) in this.elements.withIndex())
        {
            beforeElement = element
            index = i

            if (beforeElement.name == before)
            {
                break
            }
        }

        this.elements.add(index, InternalPipelineData(name, fragment))
    }

    fun addAfter(after: String, name: String, fragment: PipelineFragment<DataT>)
    {
        var afterElement: InternalPipelineData<DataT>
        var index = 0

        for ((i, element) in this.elements.withIndex())
        {
            afterElement = element
            index = i + 1

            if (afterElement.name == after)
            {
                break
            }
        }

        this.elements.add(index, InternalPipelineData(name, fragment))
    }

    fun process(data: DataT)
    {
        for (element in elements)
        {
            element.fragment.process(data)
        }
    }

    override fun toString(): String
    {
        val builder = StringBuilder(super.toString())

        for (element in this.elements)
        {
            builder.append("\n").append(element.name)
        }

        return builder.toString()
    }

    private class InternalPipelineData<DataT>(val name: String, val fragment: PipelineFragment<DataT>)
}