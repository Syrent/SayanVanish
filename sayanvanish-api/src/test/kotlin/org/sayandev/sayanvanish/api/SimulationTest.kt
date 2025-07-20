package org.sayandev.sayanvanish.api

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.sayandev.sayanvanish.api.storage.storageConfig
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.logging.Logger

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
class SimulationTest {

    private val testDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    companion object {
        val uniqueId = UUID.fromString("6f7e7d15-89ee-4e09-afe0-ff316c66999f")
        val testUser1 = User.Generic(uniqueId, "test-user-1", true, Platform.get().serverId)
        val testUser2 = User.Generic(UUID.fromString("7f7e7d15-89ee-4e09-afe0-ff316c66999f"), "test-user-2", false, Platform.get().serverId)

        @BeforeAll
        @JvmStatic
        fun setPlatform() {
            Platform.setAndRegister(
                Platform(
                    "sayanvanish-test-suite",
                    "SayanVanish-Test",
                    Logger.getGlobal(),
                    File("./sayanvanish-test"),
                    "test-server",
                    APIPlatformAdapter()
                )
            )
            storageConfig.sql.threadCount = 1
            SayanVanishAPI.initialize()
        }
    }

    @BeforeEach
    fun setup() {
        runBlocking(testDispatcher) {
            VanishAPI.get().getDatabase().purgeAllTables().await()
        }
    }

    @Test
    @Order(1)
    fun `purgeUsers() removes all users`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        db.saveUser(testUser1).await()
        db.saveUser(testUser2).await()
        val purgeResult = db.purgeUsers().await()
        val users = db.getUsers().await()
        assert(purgeResult) { "purgeUsers should return true" }
        assert(users.isEmpty()) { "All users should be removed after purgeUsers, found: ${users.map { it.username }}" }
    }

    @Test
    @Order(2)
    fun `saveUser() and getUser() returns the saved user`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val saveResult = db.saveUser(testUser1).await()
        val loaded = db.getUser(testUser1.uniqueId).await()
        assert(saveResult) { "saveUser should return true" }
        assert(loaded != null) { "getUser should return the saved user" }
        assert(loaded == testUser1) { "Loaded user should match saved user" }
    }

    @Test
    @Order(3)
    fun `getUsers() returns all users`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        db.saveUser(testUser1).await()
        db.saveUser(testUser2).await()
        val users = db.getUsers().await()
        assert(users.size == 2) { "getUsers should return all users, expected 2, got ${users.size}" }
        assert(users.any { it.uniqueId == testUser1.uniqueId }) { "testUser1 should be present in users" }
        assert(users.any { it.uniqueId == testUser2.uniqueId }) { "testUser2 should be present in users" }
    }

    @Test
    @Order(4)
    fun `hasUser() returns correctly for existing and non-existing users`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        db.saveUser(testUser1).await()
        val hasUser1 = db.hasUser(testUser1.uniqueId).await()
        val hasUser2 = db.hasUser(testUser2.uniqueId).await()
        assert(hasUser1) { "hasUser should return true for existing user" }
        assert(!hasUser2) { "hasUser should return false for non-existing user" }
    }

    @Test
    @Order(5)
    fun `updateUser() updates user data correctly`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        db.saveUser(testUser1).await()
        val updated = testUser1.copy(username = "updated-user")
        val updateResult = db.updateUser(updated).await()
        val loaded = db.getUser(updated.uniqueId).await()
        assert(updateResult) { "updateUser should return true" }
        assert(loaded != null && loaded.username == "updated-user") { "User should be updated in database" }
    }

    @Test
    @Order(6)
    fun `removeUser() deletes the specified user`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        db.saveUser(testUser1).await()
        val removeResult = db.removeUser(testUser1.uniqueId).await()
        val loaded = db.getUser(testUser1.uniqueId).await()
        assert(removeResult) { "removeUser should return true" }
        assert(loaded == null) { "User should be removed from database" }
    }

    @Test
    @Order(7)
    fun `purgeAllTables() removes all data from all tables`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        db.saveUser(testUser1).await()
        db.saveUser(testUser2).await()
        val purgeResult = db.purgeAllTables().await()
        val users = db.getUsers().await()
        assert(purgeResult) { "purgeAllTables should return true" }
        assert(users.isEmpty()) { "All users should be removed after purgeAllTables" }
    }

    @Test
    @Order(8)
    fun `purgeUsers(serverId) removes only users matching the serverId`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val otherServerId = "other-server"
        val userOtherServer = User.Generic(UUID.randomUUID(), "other", false, otherServerId)
        db.saveUser(testUser1).await()
        db.saveUser(userOtherServer).await()
        val purgeResult = db.purgeUsers(otherServerId).await()
        val users = db.getUsers().await()
        assert(purgeResult) { "purgeUsers(serverId) should return true" }
        assert(users.any { it.uniqueId == testUser1.uniqueId }) { "User from different server should not be purged" }
        assert(users.none { it.uniqueId == userOtherServer.uniqueId }) { "User from purged server should be removed" }
    }

    // VanishUser related tests
    @Test
    @Order(9)
    fun `saveVanishUser() and getVanishUser() returns the saved vanish user`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val vanishUser = VanishUser.Generic(testUser1.uniqueId, testUser1.username, testUser1.serverId)
        val saveResult = db.saveVanishUser(vanishUser).await()
        val loaded = db.getVanishUser(vanishUser.uniqueId).await()
        assert(saveResult) { "saveVanishUser should return true" }
        assert(loaded != null) { "getVanishUser should return the saved vanish user" }
        assert(loaded!!.uniqueId == vanishUser.uniqueId) { "Loaded vanish user should match saved uniqueId" }
    }

    @Test
    @Order(10)
    fun `getVanishUsers() returns all vanish users`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val vanishUser1 = VanishUser.Generic(testUser1.uniqueId, testUser1.username, testUser1.serverId)
        val vanishUser2 = VanishUser.Generic(testUser2.uniqueId, testUser2.username, testUser2.serverId)
        db.saveVanishUser(vanishUser1).await()
        db.saveVanishUser(vanishUser2).await()
        val vanishUsers = db.getVanishUsers().await()
        assert(vanishUsers.size == 2) { "getVanishUsers should return all vanish users, expected 2, got ${vanishUsers.size}" }
    }

    @Test
    @Order(11)
    fun `hasVanishUser() returns correctly for existing and non-existing vanish users`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val vanishUser = VanishUser.Generic(testUser1.uniqueId, testUser1.username, testUser1.serverId)
        db.saveVanishUser(vanishUser).await()
        val hasVanish = db.hasVanishUser(vanishUser.uniqueId).await()
        val hasNotVanish = db.hasVanishUser(testUser2.uniqueId).await()
        assert(hasVanish) { "hasVanishUser should return true for existing vanish user" }
        assert(!hasNotVanish) { "hasVanishUser should return false for non-existing vanish user" }
    }

    @Test
    @Order(12)
    fun `updateVanishUser() updates vanish user data correctly`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val vanishUser = VanishUser.Generic(testUser1.uniqueId, testUser1.username, testUser1.serverId, isVanished = false, vanishLevel = 1)
        db.saveVanishUser(vanishUser).await()
        val updated = vanishUser.copy(isVanished = true, vanishLevel = 5)
        val updateResult = db.updateVanishUser(updated).await()
        val loaded = db.getVanishUser(updated.uniqueId).await()
        assert(updateResult) { "updateVanishUser should return true" }
        assert(loaded != null && loaded.isVanished && loaded.vanishLevel == 5) { "VanishUser should be updated in database" }
    }

    @Test
    @Order(13)
    fun `removeVanishUser() deletes the specified vanish user`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val vanishUser = VanishUser.Generic(testUser1.uniqueId, testUser1.username, testUser1.serverId)
        db.saveVanishUser(vanishUser).await()
        val removeResult = db.removeVanishUser(vanishUser.uniqueId).await()
        val loaded = db.getVanishUser(vanishUser.uniqueId).await()
        assert(removeResult) { "removeVanishUser should return true" }
        assert(loaded == null) { "VanishUser should be removed from database" }
    }

    @Test
    @Order(14)
    fun `syncUser() updates the cache`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val cache = VanishAPI.get().getCacheService()
        val messaging = VanishAPI.get().getMessagingService()

        db.saveUser(testUser1).await()
        messaging.syncUser(testUser1).await()

        val cachedUser = cache.getUsers()[testUser1.uniqueId]
        assert(cachedUser != null) { "User should be in cache after sync" }
        assert(cachedUser == testUser1) { "Cached user should match synced user" }
    }

    @Test
    @Order(15)
    fun `syncVanishUser() updates the cache`() = runBlocking(testDispatcher) {
        val db = VanishAPI.get().getDatabase()
        val cache = VanishAPI.get().getCacheService()
        val messaging = VanishAPI.get().getMessagingService()
        val vanishUser = VanishUser.Generic(testUser1.uniqueId, testUser1.username, testUser1.serverId)

        db.saveVanishUser(vanishUser).await()
        messaging.syncVanishUser(vanishUser).await()

        val cachedVanishUser = cache.getVanishUsers()[vanishUser.uniqueId]
        assert(cachedVanishUser != null) { "VanishUser should be in cache after sync" }
        assert(cachedVanishUser!!.uniqueId == vanishUser.uniqueId) { "Cached vanish user should match synced user" }
    }
}