package com.mountsa.fmsimulation.core.match.commentary

data class CommentaryTemplate(
    val id: String,
    val text: String,
    val type: String,
    val weight: Int = 1
)