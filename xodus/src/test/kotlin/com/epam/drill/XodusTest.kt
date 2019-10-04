@file:Suppress("BlockingMethodInNonBlockingContext")

package com.epam.drill

import com.epam.drill.service.*
import com.epam.drill.xodus.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import org.junit.*
import org.junit.Test
import org.junit.rules.*
import kotlin.test.*

enum class EN {
    A, B, C
}

@Serializable
data class ComplexObject(
    @Id val st: String,
    val ch: Char,
    val blink: SubObject,
    val en: EN = EN.B,
    val nullString: String?
)

@Serializable
data class SubObject(val string: String, val int: Int, val lst: Last)

@Serializable
data class Last(val string: Byte)

class XsodusTest {
    @get:Rule
    val projectDir = TemporaryFolder()

    @Test
    fun storeAndRetrieve() = runBlocking {
        val agentId = "myAgent"

        val agentStore =
            StoreManger(projectDir.newFolder().resolve("agent")).agentStore(agentId)

        val last = Last(2.toByte())
        val blink = SubObject("subStr", 12, last)

        val any = ComplexObject("str", 'x', blink, EN.C, null)
        val any2 = ComplexObject("ws", 'x', blink, EN.C, null)
        agentStore.store(any)
        agentStore.store(any2)
        agentStore.update(any.copy(ch = 'y'))
        val all = agentStore.getAll<ComplexObject>()
        assertNotNull(all)
        assertEquals(2, all.size)

        assertEquals(agentStore.find<ComplexObject> { ComplexObject::st eq "str" }.first().ch, 'y')
        println(agentStore.find<SubObject> { SubObject::string startsWith "asd" })

    }
}