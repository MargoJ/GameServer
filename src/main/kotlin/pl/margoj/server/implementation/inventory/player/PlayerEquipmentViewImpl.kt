package pl.margoj.server.implementation.inventory.player

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.server.api.inventory.ItemStack
import pl.margoj.server.api.inventory.player.ItemRequirementsNotMetException
import pl.margoj.server.api.inventory.player.PlayerEquipment
import pl.margoj.server.api.player.Profession
import pl.margoj.server.implementation.inventory.WrappedInventory
import pl.margoj.server.implementation.item.ItemStackImpl

class PlayerEquipmentViewImpl(override val owner: PlayerInventoryImpl) : PlayerEquipment, WrappedInventory
{
    override val size: Int = 9

    override val allItems: List<ItemStackImpl?> by lazy { this.owner.allItems.subList(0, this.size - 1) }

    override fun use(item: ItemStack): ItemStack?
    {
        item as ItemStackImpl

        if (!item[ItemProperties.CATEGORY].usable)
        {
            return null
        }

        val levelRequirement = item[ItemProperties.LEVEL_REQUIREMENT]
        val levelRequirementMet = levelRequirement == 0 || this.owner.player.level >= levelRequirement

        val professionRequirement = item[ItemProperties.PROFESSION_REQUIREMENT]
        var professionRequirementMet = true

        if (professionRequirement.any)
        {
            professionRequirementMet = when (this.owner.player.stats.profession)
            {
                Profession.WARRIOR -> professionRequirement.warrior
                Profession.PALADIN -> professionRequirement.paladin
                Profession.BLADE_DANCER -> professionRequirement.bladedancer
                Profession.MAGE -> professionRequirement.mage
                Profession.HUNTER -> professionRequirement.hunter
                Profession.TRACKER -> professionRequirement.tracker
            }
        }

        if (!levelRequirementMet || !professionRequirementMet)
        {
            throw ItemRequirementsNotMetException(!levelRequirementMet, !professionRequirementMet)
        }

        var equipped = true

        val returnValue = when (item.item.margoItem.itemCategory)
        {
            ItemCategory.HELMET -> this.equipTo({ this.helmet }, { this.helmet = item })
            ItemCategory.RINGS -> this.equipTo({ this.ring }, { this.ring = item })
            ItemCategory.NECKLACES -> this.equipTo({ this.neckless }, { this.neckless = item })
            ItemCategory.GLOVES -> this.equipTo({ this.gloves }, { this.gloves = item })

            ItemCategory.ONE_HANDED_WEAPONS -> this.equipTo({ this.weapon }, { this.weapon = item })
            ItemCategory.TWO_HANDED_WEAPONS -> this.equipTo({ this.weapon }, { this.weapon = item })
            ItemCategory.HAND_AND_A_HALF_WEAPONS -> this.equipTo({ this.weapon }, { this.weapon = item })
            ItemCategory.RANGE_WEAPON -> this.equipTo({ this.weapon }, { this.weapon = item })
            ItemCategory.STAFF -> this.equipTo({ this.weapon }, { this.weapon = item })
            ItemCategory.WANDS -> this.equipTo({ this.weapon }, { this.weapon = item })
            ItemCategory.ARMOR -> this.equipTo({ this.armor }, { this.armor = item })

            ItemCategory.HELPERS -> this.equipTo({ this.helper }, { this.helper = item })
            ItemCategory.ARROWS -> this.equipTo({ this.helper }, { this.helper = item })
            ItemCategory.SHIELDS -> this.equipTo({ this.helper }, { this.helper = item })

            ItemCategory.BOOTS -> this.equipTo({ this.boots }, { this.boots = item })

            else ->
            {
                equipped = false
                null
            }
        }

        if(equipped && item[ItemProperties.BINDS])
        {
            item.setProperty(ItemProperties.BINDS, false)
            item.setProperty(ItemProperties.SOUL_BOUND, true)
        }

        return returnValue
    }

    override fun tryToPut(item: ItemStack): Boolean
    {
        val old = use(item) ?: return true
        this.use(old)
        return false
    }

    private fun equipTo(get: () -> ItemStack?, set: () -> Unit): ItemStack?
    {
        val previous = get()
        set()
        return previous
    }

    override var helmet: ItemStack?
        get() = this.owner[0]
        set(value)
        {
            this.owner[0] = value
        }

    override var ring: ItemStack?
        get() = this.owner[1]
        set(value)
        {
            this.owner[1] = value
        }

    override var neckless: ItemStack?
        get() = this.owner[2]
        set(value)
        {
            this.owner[2] = value
        }

    override var gloves: ItemStack?
        get() = this.owner[3]
        set(value)
        {
            this.owner[3] = value
        }

    override var weapon: ItemStack?
        get() = this.owner[4]
        set(value)
        {
            this.owner[4] = value
        }

    override var armor: ItemStack?
        get() = this.owner[5]
        set(value)
        {
            this.owner[5] = value
        }

    override var helper: ItemStack?
        get() = this.owner[6]
        set(value)
        {
            this.owner[6] = value
        }

    override var boots: ItemStack?
        get() = this.owner[7]
        set(value)
        {
            this.owner[7] = value
        }

    override var purse: ItemStack?
        get() = this.owner[8]
        set(value)
        {
            this.owner[8] = value
        }

    override fun get(index: Int): ItemStack?
    {
        if (index >= 9)
        {
            throw ArrayIndexOutOfBoundsException(index)
        }

        return this.owner[index]
    }

    override fun set(index: Int, item: ItemStack?): ItemStack?
    {
        if (index >= 9)
        {
            throw ArrayIndexOutOfBoundsException(index)
        }

        return this.owner.set(index, item)
    }
}