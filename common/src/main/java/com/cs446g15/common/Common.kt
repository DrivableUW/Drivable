package com.cs446g15.common

import java.security.MessageDigest

fun String.sha256()
        = MessageDigest.getInstance("SHA-256")
    .digest(toByteArray())
    .joinToString("") { "%02x".format(it) }
