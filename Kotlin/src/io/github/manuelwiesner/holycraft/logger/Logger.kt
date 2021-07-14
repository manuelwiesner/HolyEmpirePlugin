package io.github.manuelwiesner.holycraft.logger

/**
 * The logging interface each logger uses.
 */
interface Logger {
    /**
     * Logs a message and/or a Throwable at MONITOR level.
     */
    fun monitor(msg: String, t: Throwable? = null): Unit = log(Level.MONITOR, msg, t)

    /**
     * Logs a message and/or a Throwable at TRACE level.
     */
    fun trace(msg: String, t: Throwable? = null): Unit = log(Level.TRACE, msg, t)

    /**
     * Logs a message and/or a Throwable at DEBUG level.
     */
    fun debug(msg: String, t: Throwable? = null): Unit = log(Level.DEBUG, msg, t)

    /**
     * Logs a message and/or a Throwable at INFO level.
     */
    fun info(msg: String, t: Throwable? = null): Unit = log(Level.INFO, msg, t)

    /**
     * Logs a message and/or a Throwable at WARN level.
     */
    fun warn(msg: String, t: Throwable? = null): Unit = log(Level.WARN, msg, t)

    /**
     * Logs a message and/or a Throwable at ERROR level.
     */
    fun error(msg: String, t: Throwable? = null): Unit = log(Level.ERROR, msg, t)

    /**
     * Logs a message and/or a Throwable at the given level.
     */
    fun log(lvl: Level, msg: String, t: Throwable? = null)

    companion object {
        fun getLogger(name: String, delegate: java.util.logging.Logger): Logger {
            return object : Logger {
                override fun log(lvl: Level, msg: String, t: Throwable?) {
                    delegate.log(
                        when (lvl) {
                            Level.MONITOR -> java.util.logging.Level.FINEST
                            Level.TRACE -> java.util.logging.Level.FINER
                            Level.DEBUG -> java.util.logging.Level.FINE
                            Level.INFO -> java.util.logging.Level.INFO
                            Level.WARN -> java.util.logging.Level.WARNING
                            Level.ERROR -> java.util.logging.Level.SEVERE
                        }, "[${name}] $msg", t
                    )
                }
            }
        }
    }
}