package org.sayandev.sayanvanish.velocity.event

import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.velocity.api.VelocityVanishUser

class VelocityUserUnVanishEvent(val user: VelocityVanishUser, val options: VanishOptions)