package com.mountsa.fmsimulation.core.match.event


enum class EventType {

    MATCH_START,
    MATCH_END,
    HALFTIME,

    SHOT,
    SHOT_ON_TARGET,
    MISSED_CHANCE,

    GOAL,
    OWN_GOAL,
    PENALTY_GOAL,

    SAVE,
    BIG_SAVE,

    PASS,
    KEY_PASS,
    ASSIST,

    TACKLE,
    INTERCEPTION,

    FOUL,
    YELLOW_CARD,
    RED_CARD,

    OFFSIDE,
    CORNER,
    FREE_KICK,

    SUBSTITUTION,
    INJURY,

    POSSESSION,

    CROWD_REACTION
}