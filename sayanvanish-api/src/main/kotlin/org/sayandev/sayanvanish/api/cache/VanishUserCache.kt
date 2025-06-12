package org.sayandev.sayanvanish.api.cache

import org.sayandev.sayanvanish.api.VanishUser
import java.util.UUID

class VanishUserCache : Cache<UUID, VanishUser>("vanish_users")