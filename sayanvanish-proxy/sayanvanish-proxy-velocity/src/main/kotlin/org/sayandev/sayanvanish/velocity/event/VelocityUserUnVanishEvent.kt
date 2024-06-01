package org.sayandev.sayanvanish.velocity.event

import org.sayandev.sayanvanish.api.VanishOptions
import org.sayandev.sayanvanish.velocity.api.VelocityUser

class VelocityUserUnVanishEvent(val user: VelocityUser, val options: VanishOptions)