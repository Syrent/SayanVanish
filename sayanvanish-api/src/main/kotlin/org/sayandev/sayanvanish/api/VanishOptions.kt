package org.sayandev.sayanvanish.api

data class VanishOptions(
    var sendMessage: Boolean = true,
    var notifyStatusChangeToOthers: Boolean = true,
    var notifyJoinQuitVanished: Boolean = true,
    var isOnJoin: Boolean = false,
    var isOnQuit: Boolean = false,
) {

    class Builder {
        private var sendMessage = true
        private var notifyStatusChangeToOthers = true
        private var notifyJoinQuitVanished = true
        private var isOnJoin = false
        private var isOnQuit = false

        fun sendMessage(sendMessage: Boolean): Builder {
            this.sendMessage = sendMessage
            return this
        }

        fun notifyStatusChangeToOthers(notifyStatusChangeToOthers: Boolean): Builder {
            this.notifyStatusChangeToOthers = notifyStatusChangeToOthers
            return this
        }

        fun notifyJoinQuitVanished(notifyJoinQuitVanished: Boolean): Builder {
            this.notifyJoinQuitVanished = notifyJoinQuitVanished
            return this
        }

        fun isOnJoin(isOnJoin: Boolean): Builder {
            this.isOnJoin = isOnJoin
            return this
        }

        fun isOnQuit(isOnQuit: Boolean): Builder {
            this.isOnQuit = isOnQuit
            return this
        }

        fun build(): VanishOptions {
            return VanishOptions(sendMessage, notifyStatusChangeToOthers, notifyJoinQuitVanished, isOnJoin)
        }
    }

    companion object {
        @JvmStatic
        fun defaultOptions(): VanishOptions {
            return VanishOptions()
        }
    }
}