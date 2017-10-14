package pl.margoj.server.implementation.utils

object MargoMath
{
    fun baseExpFromMob(level: Int): Long
    {
        // thanks to https://www.margonem.pl/?task=profile&id=6327190#tab2
        if (level <= 0)
        {
            return 0L
        }

        if (level <= 50)
        {
            return when (level)
            {
                1 -> 5
                2 -> 10
                3 -> 16
                4 -> 23
                5 -> 30
                6 -> 38
                7 -> 47
                8 -> 56
                9 -> 67
                10 -> 77
                11 -> 89
                12 -> 100
                13 -> 114
                14 -> 128
                15 -> 144
                16 -> 159
                17 -> 173
                18 -> 191
                19 -> 209
                20 -> 227
                21 -> 245
                22 -> 263
                23 -> 284
                24 -> 305
                25 -> 325
                26 -> 350
                27 -> 375
                28 -> 395
                29 -> 417
                30 -> 440
                31 -> 467
                32 -> 492
                33 -> 518
                34 -> 546
                35 -> 573
                36 -> 600
                37 -> 629
                38 -> 657
                39 -> 686
                40 -> 718
                41 -> 749
                42 -> 779
                43 -> 812
                44 -> 844
                45 -> 878
                46 -> 911
                47 -> 946
                48 -> 980
                49 -> 1016
                50 -> 1052
                else -> 0
            }
        }

        val c = when (level)
        {
            in 51..54 -> 1.778
            in 55..59 -> 1.777
            in 60..65 -> 1.776
            in 66..75 -> 1.775
            in 76..96 -> 1.774
            in 97..240 -> 1.773
            in 241..280 -> 1.774
            else -> 1.775
        }

        return Math.pow(level.toDouble(), c).toLong()
    }

    fun calculateDamageReduction(damage: Int, armor: Int): Int
    {
        if(damage == 0)
        {
            return 0
        }

        val ratio = armor.toDouble() / damage.toDouble()

        if(ratio <= 0.0)
        {
            return 0
        }
        else if(ratio > 1.6)
        {
            return damage
        }

        // TODO: Not sure if that's the correct formula
        return Math.min(damage, Math.floor(Math.sqrt(ratio) * 0.81f * damage).toInt())
    }
}