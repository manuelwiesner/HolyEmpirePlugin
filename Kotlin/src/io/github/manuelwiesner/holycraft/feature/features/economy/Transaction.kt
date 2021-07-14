package io.github.manuelwiesner.holycraft.feature.features.economy


import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.manuelwiesner.holycraft.store.*
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

abstract class Transaction(val bookingInfo: String, val amount: Int, val id: Int, val timestamp: Instant, protected var succeeded: Boolean) {
    fun hasSucceeded(): Boolean = this.succeeded
}

class UserTransaction(val sender: UUID, val receiver: UUID, bookingInfo: String, amount: Int, id: Int, timestamp: Instant = Instant.now(),
                      succeeded: Boolean = false, private val isExecuted: AtomicBoolean = AtomicBoolean(false))
    : Transaction(bookingInfo, amount, id, timestamp, succeeded) {

    fun execute(senderBalance: AtomicInteger, receiverBalance: AtomicInteger): Boolean {
        if (!this.isExecuted.compareAndSet(false, true)) return false

        if (senderBalance.addAndGet(-this.amount) < 0) {
            senderBalance.getAndAdd(this.amount)
            this.succeeded = false
            return false
        }
        receiverBalance.getAndAdd(this.amount)
        this.succeeded = true
        return true
    }
}


class BankTransaction(val transactor: UUID, bookingInfo: String, amount: Int, id: Int, timestamp: Instant = Instant.now(),
                      succeeded: Boolean = false, private val isExecuted: AtomicBoolean = AtomicBoolean(false))
    : Transaction(bookingInfo, amount, id, timestamp, succeeded) {

    fun execute(transactorBalance: AtomicInteger): Boolean {
        if (!this.isExecuted.compareAndSet(false, true)) return false

        if (transactorBalance.addAndGet(this.amount) < 0) {
            transactorBalance.addAndGet(-this.amount)
            this.succeeded = false
            return false
        }
        this.succeeded = true
        return true
    }
}

object TransactionConverter : _StoreConverter<Transaction>() {
    override fun fromJson(json: JsonReader): Transaction {
        json.beginObject()
        val timestamp = json.getInstant("timestamp")
        val amount = json.getInt("amount")
        val id = json.getInt("id")
        val succeeded = json.getBoolean("succeeded")
        val bookingInfo = json.getString("booking-info")

        return when (json.getString("type")) {
            "user" -> {
                val sender = json.getUUID("sender")
                val receiver = json.getUUID("receiver")
                json.endObject()
                UserTransaction(sender, receiver, bookingInfo, amount, id, timestamp, succeeded, AtomicBoolean(true))
            }
            "bank" -> {
                val transactor = json.getUUID("transactor")
                json.endObject()
                BankTransaction(transactor, bookingInfo, amount, id, timestamp, succeeded, AtomicBoolean(true))
            }
            else -> throw IllegalArgumentException("Type is not user or bank")
        }
    }

    override fun toJson(json: JsonWriter, value: Transaction) {
        json.beginObject()
        json.setInstant("timestamp", value.timestamp)
        json.setInt("amount", value.amount)
        json.setInt("id", value.id)
        json.setBoolean("succeeded", value.hasSucceeded())
        json.setString("booking-info", value.bookingInfo)

        when (value) {
            is UserTransaction -> {
                json.setString("type", "user")
                json.setUUID("sender", value.sender)
                json.setUUID("receiver", value.receiver)
            }
            is BankTransaction -> {
                json.setString("type", "bank")
                json.setUUID("transactor", value.transactor)
            }
            else -> throw IllegalArgumentException("Value is not a user or bank transaction: $value")
        }

        json.endObject()
    }
}