package com.java.lichenhao

fun String.toArticle(): String =
    this.split('\n')
        .map { "\u3000\u3000$it" }
        .joinToString("\n\n")