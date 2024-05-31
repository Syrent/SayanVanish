package org.sayandev.sayanvanish.api.exception

import org.sayandev.sayanvanish.api.Platform

class UnsupportedPlatformException(action: String?, message: String) : Exception(message) {
    constructor(action: String) : this(action, "This action is not supported on this platform yet. (platform: ${Platform.get().id}, action: ${action})")
    constructor() : this(null, "This platform is not supported yet. (platform: ${Platform.get().id})")
}