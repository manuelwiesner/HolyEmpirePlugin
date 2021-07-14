package io.github.manuelwiesner.holycraft.logger

/**
 * Represents the different logging levels.
 */
enum class Level {
    /**
     * Info that is probably useless and just spams the console but may in some cases be used for debugging.
     */
    MONITOR,

    /**
     * Same as debug but at a very fine, mostly obsolete level.
     */
    TRACE,

    /**
     * Any outputs which are not relevant for the end-user but can be used for debugging.
     */
    DEBUG,

    /**
     * General information about the program state which are relevant to the user.
     */
    INFO,

    /**
     * Any error/unexpected behaviour which is recoverable. (e.g. notify the user that some file couldn't be saved)
     */
    WARN,

    /**
     * Fatal/unrecoverable errors or any error with very high significance.
     */
    ERROR
}