package org.techtown.comptoir

data class Message(
    var message: String?,
    var sendId: String?,
) {
    constructor():this("", "")
}