package org.sayandev.sayanvanish.api.database

enum class TransactionTypes(
    override val id: String,
    override val method: DatabaseMethod,
) : TransactionType {
    ADD_VANISH_USER("add_vanish_user", DatabaseMethod.SQL),
    HAS_VANISH_USER("has_vanish_user", DatabaseMethod.SQL),
    UPDATE_VANISH_USER("update_vanish_user", DatabaseMethod.SQL),
    REMOVE_VANISH_USER("remove_vanish_user", DatabaseMethod.SQL),
    GET_VANISH_USER("get_vanish_user", DatabaseMethod.SQL),
    GET_VANISH_USERS("get_vanish_users", DatabaseMethod.SQL),
    GET_USERS("get_users", DatabaseMethod.SQL),
    ADD_USER("add_user", DatabaseMethod.SQL),
    HAS_USER("has_user", DatabaseMethod.SQL),
    UPDATE_USER("update_user", DatabaseMethod.SQL),
    REMOVE_USER("remove_user", DatabaseMethod.SQL),
    ADD_TO_QUEUE("add_to_queue", DatabaseMethod.SQL),
    IS_IN_QUEUE("is_in_queue", DatabaseMethod.SQL),
    GET_FROM_QUEUE("get_from_queue", DatabaseMethod.SQL),
    REMOVE_FROM_QUEUE("remove_from_queue", DatabaseMethod.SQL),
}