@file:Suppress("CovariantEquals")

package com.epam.drill.service

import com.epam.drill.xodus.*
import jetbrains.exodus.*
import jetbrains.exodus.bindings.*
import jetbrains.exodus.entitystore.*
import jetbrains.exodus.entitystore.iterate.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import java.io.*
import kotlin.reflect.full.*


class StoreOverloader(val x: PersistentStoreTransaction) : StoreTransaction by x, TxnGetterStrategy by x, TxnProvider by x {
    override fun newEntity(entityType: String): Entity {
        return x.newEntity(entityType)
    }

    fun newEntityX(entityType: String): Entity {
        try {
            val entityTypeId = x.store.getEntityTypeId(this, entityType, true)
            val entityLocalId = 123L
            x.store.getEntitiesTable(x, entityTypeId).putRight(
                x.environmentTransaction, LongBinding.longToCompressedEntry(entityLocalId), IntegerBinding.intToCompressedEntry(0)
            )
            val id = PersistentEntityId(entityTypeId, entityLocalId)
            // update iterables' cache
//            EntityAddedHandleCheckerImpl(this, id, mutableCache(), mutatedInTxn).updateCache()
            return PersistentEntity(x.store, id)
        } catch (e: Exception) {
            throw ExodusException.toEntityStoreException(e)
        }

    }
}

class StoreClient(store: PersistentEntityStoreImpl) : PersistentEntityStore by store {
    suspend inline fun <reified T : Any> store(any: T) = withContext(Dispatchers.IO) {
        executeInTransaction { txn ->
            val serializer = T::class.serializer()
            val idName = serializer.descriptor.idName()
            check(findExistsEntity(idName, any, txn) == null) { "Entity - '$any' already exists" }
//            val x = StoreOverloader(txn as PersistentStoreTransaction)
            val obj = txn.newEntity(any::class.simpleName.toString())
            XodusEncoder(txn, obj).encode(serializer, any)


        }
    }

    suspend inline fun <reified T : Any> update(any: T) = withContext(Dispatchers.IO) {
        executeInTransaction { txn ->
            val serializer = T::class.serializer()
            val idName = serializer.descriptor.idName()
            val obj = findExistsEntity(idName, any, txn)
            checkNotNull(obj) { "Can't find the entity by id - '$idName'" }
            XodusEncoder(txn, obj).encode(T::class.serializer(), any)
        }
    }

    inline fun <reified T : Any> findExistsEntity(
        idName: String,
        any: T,
        txn: StoreTransaction
    ): Entity? {

        val getter = (T::class.memberProperties.find { it.name == idName })?.getter?.invoke(any) ?: return null
        val find = txn.find(T::class.simpleName.toString(), idName, getter as Comparable<*>)
        return find.firstOrNull()
    }

    fun SerialDescriptor.idName() =
        (0 until this.elementsCount).filter { inx -> this.getElementAnnotations(inx).any { it is Id } }
            .map { idIndex -> this.getElementName(idIndex) }.first()

    suspend inline fun <reified T : Any> getAll(): Collection<T> = withContext(Dispatchers.IO) {
        computeInTransaction { txn ->
            txn.getAll(T::class.simpleName.toString()).map {
                @Suppress("UNCHECKED_CAST") val strategy = T::class.serializer() as KSerializer<Any>
                XodusDecoder(txn, it).decode(strategy) as T
            }
        }
    }

    inline fun <reified T : Any> find(crossinline expression: Expression<T>.() -> Unit) =
        runBlocking(Dispatchers.IO) { computeWithExpression(expression, Expression()) }


    inline fun <reified T : Any> computeWithExpression(
        crossinline expression: Expression<T>.() -> Unit, expr: Expression<T>
    ) = this.computeInTransaction { txn ->
        expression(expr)
        val entityIterable = expr.process(txn, T::class)
        entityIterable.map {
            @Suppress("UNCHECKED_CAST") val strategy = T::class.serializer() as KSerializer<Any>
            XodusDecoder(txn, it).decode(strategy) as T
        }
    }
}

class StoreManger(val baseLocation: File = File("./").resolve("agent")) {

    suspend fun agentStore(agentId: String, block: (suspend StoreClient.() -> Unit)? = null): StoreClient {
        val newInstance = PersistentEntityStores.newInstance(baseLocation.resolve(agentId))
        val storeClient = StoreClient(newInstance)
        return block?.let {
            newInstance.use { entityStore ->
                block(storeClient)
            }
            storeClient
        } ?: storeClient
    }
}

