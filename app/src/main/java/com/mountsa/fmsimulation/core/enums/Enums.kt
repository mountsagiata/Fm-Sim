package com.mountsa.fmsimulation.core.enums

enum class PlayerStatus {
    FIT, INJURED, SUSPENDED, UNAVAILABLE
}

enum class PlayerHappiness {
    UNHAPPY, CONCERNED, CONTENT, HAPPY, DELIGHTED
}

enum class SquadRole {
    STAR, IMPORTANT, SQUAD_PLAYER, BACKUP, PROSPECT
}

enum class TransferStatus {
    PENDING, ACCEPTED, REJECTED, NEGOTIATING, CANCELLED
}

enum class ObjectivePriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

enum class Mentality {
    DEFENSIVE, CAUTIOUS, BALANCED, POSITIVE, ATTACKING
}

enum class ManagerPersonality {
    BALANCED, AGGRESSIVE, DEFENSIVE, YOUTH_FOCUSED, TRANSFER_SPENDER
}

enum class PlayerPersonality {
    PROFESSIONAL, AMBITIOUS, LOYAL, TEMPERAMENTAL, DETERMINED, BALANCED
}

enum class ScoutAssignmentType {
    NATION, LEAGUE, POSITION, YOUTH
}

enum class InboxCategory {
    BOARD, MEDICAL, TRANSFER, MATCH, PLAYER, MEDIA, CONTRACT, NEWS, SCOUTING
}

enum class MatchStage {
    LEAGUE, GROUP, ROUND_OF_16, QUARTER_FINAL, SEMI_FINAL, FINAL
}
