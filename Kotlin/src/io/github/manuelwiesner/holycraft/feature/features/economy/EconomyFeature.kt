package io.github.manuelwiesner.holycraft.feature.features.economy

import io.github.manuelwiesner.holycraft.feature.FeatureBase
import io.github.manuelwiesner.holycraft.feature._FeatureManager
import io.github.manuelwiesner.holycraft.player.View
import io.github.manuelwiesner.holycraft.store.Store
import io.github.manuelwiesner.holycraft.store.StoreConverter
import io.github.manuelwiesner.holycraft.yaml.Yaml
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class EconomyFeature(manager: _FeatureManager) : FeatureBase<Unit>(manager, "ECONOMY") {

    private val storedPlayerBalances: View<Int> = getHolyCraft().getPlayerManager()
        .getView("economy.balance", StoreConverter.NUMBER)

    private val playerBalances: ConcurrentHashMap<UUID, AtomicInteger> = ConcurrentHashMap()

    private val startingBalance: Yaml<Int> = getHolyCraft().getYamlManager()
        .getIntWrapper("feature.economy.startingBalance")

    private val storedTransaction: Store<UUID, MutableList<Transaction>> = getHolyCraft().getStoreManager()
        .getUUIDStore("transactions", StoreConverter.LIST(TransactionConverter))

    private val transactionCounter: Yaml<Int> = getHolyCraft().getYamlManager()
        .getIntWrapper("feature.economy.transactionId")

    private val transactionId = AtomicInteger(0)

    init {
        this.children += HolyCoinCmd(this)
    }

    override fun loadFeature() {
        this.playerBalances.clear()
        this.storedPlayerBalances.forEach { uuid, balance -> this.playerBalances[uuid] = AtomicInteger(balance) }
        this.transactionId.set(this.transactionCounter.get(0))
    }

    override fun unloadFeature() {
        saveToDisk()
        this.playerBalances.clear()
    }

    override fun saveToDisk() {
        this.playerBalances.forEach { (uid, balance) ->
            this.storedPlayerBalances[uid] = balance.get()
        }
        this.transactionCounter.set(this.transactionId.get())
    }

    private fun _getPlayerBalance(uid: UUID): AtomicInteger {
        return this.playerBalances.computeIfAbsent(uid) { AtomicInteger(this.startingBalance.get(0)) }
    }

    fun getPlayerBalance(uid: UUID): Int {
        return _getPlayerBalance(uid).get()
    }

    fun executeTransaction(sender: UUID, receiver: UUID, amount: Int, bookingInfo: String): Boolean {
        val transaction = UserTransaction(sender, receiver, bookingInfo, amount, this.transactionId.incrementAndGet())
        this.storedTransaction.computeIfAbsent(sender) { Collections.synchronizedList(arrayListOf()) } += transaction
        this.storedTransaction.computeIfAbsent(receiver) { Collections.synchronizedList(arrayListOf()) } += transaction
        return transaction.execute(_getPlayerBalance(sender), _getPlayerBalance(receiver))
    }

    fun executeTransaction(transactor: UUID, amount: Int, bookingInfo: String): Boolean {
        val transaction = BankTransaction(transactor, bookingInfo, amount, this.transactionId.incrementAndGet())
        this.storedTransaction.computeIfAbsent(transactor) { arrayListOf() } += transaction
        return transaction.execute(_getPlayerBalance(transactor))
    }
}