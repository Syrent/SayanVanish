import org.sayandev.Module
import org.sayandev.applyDependencies
import org.sayandev.applyRepositories

repositories {
    applyRepositories(Module.API)
}

dependencies {
    applyDependencies(Module.API)
    stickynote.implementation("redis.clients:jedis:5.1.4")
}