package org.softeg.slartus.forpdaplus.common.impl.screens.fragments.bbcodes.models

class BbColor(var name: String, var htmlValue: String)

fun createBbColors(): List<BbColor> = listOf(
    BbColor("black", "#000000"),
    BbColor("white", "#FFFFFF"),
    BbColor("skyblue", "#82CEE8"),
    BbColor("royalblue", "#426AE6"),
    BbColor("blue", "#0000FF"),
    BbColor("darkblue", "#07008C"),
    BbColor("orange", "#FDA500"),
    BbColor("orangered", "#FF4300"),
    BbColor("crimson", "#E1133A"),
    BbColor("red", "#FF0000"),
    BbColor("darkred", "#8C0000"),
    BbColor("green", "#008000"),
    BbColor("limegreen", "#41A317"),
    BbColor("seagreen", "#4E8975"),
    BbColor("deeppink", "#F52887"),
    BbColor("tomato", "#FF6245"),
    BbColor("coral", "#F76541"),
    BbColor("purple", "#800080"),
    BbColor("indigo", "#440087"),
    BbColor("burlywood", "#E3B382"),
    BbColor("sandybrown", "#EE9A4D"),
    BbColor("sienna", "#C35817"),
    BbColor("chocolate", "#C85A17"),
    BbColor("teal", "#037F81"),
    BbColor("silver", "#C0C0C0"),
    BbColor("gray", "#808080"),
)