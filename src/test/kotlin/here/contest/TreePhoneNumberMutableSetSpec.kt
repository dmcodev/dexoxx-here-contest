package here.contest

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Suppress("BlockingMethodInNonBlockingContext")
class TreePhoneNumberMutableSetSpec : StringSpec ({

    "Should create empty set and return zero size" {
        TreePhoneNumberMutableSet().size() shouldBe 0
    }

    "Should add phone number and check its existence" {
        val phoneNumber = generateRandomPhoneNumber()
        TreePhoneNumberMutableSet().apply {
            add(phoneNumber)
            contains(phoneNumber) shouldBe true
            contains(phoneNumber.next()) shouldBe false
            size() shouldBe 1
        }
    }

    "Should add same phone number twice" {
        val phoneNumber = generateRandomPhoneNumber()
        TreePhoneNumberMutableSet().apply {
            add(phoneNumber)
            add(phoneNumber)
            contains(phoneNumber) shouldBe true
            size() shouldBe 1
        }
    }

    "Should add different phone numbers" {
        val phoneNumber = generateRandomPhoneNumber()
        TreePhoneNumberMutableSet().apply {
            add(phoneNumber)
            add(phoneNumber.next())
            size() shouldBe 2
        }
    }

    "Should add and check phone number concurrently" {
        val numberOfAddWorkers = 10
        val numberOfCheckWorkers = 10
        val numberOfPhoneNumbers = 100_000

        val phoneNumbers = generateRandomPhoneNumbers(numberOfPhoneNumbers)
            // make sure there is at least small percent of duplicates
            .let { it + generateSequence(it::random).take((it.size * 0.1).toInt()) }
        val phoneNumbersQueue = LinkedBlockingQueue(phoneNumbers)
        val uniquePhoneNumbers = phoneNumbers.distinct()
        val uniquePhoneNumbersQueue = LinkedBlockingQueue(uniquePhoneNumbers)
        val phoneNumberSet = TreePhoneNumberMutableSet()

        val testCoroutineScope = CoroutineScope(Dispatchers.IO)
        val workersInputPreparationBarrier = CyclicBarrier(numberOfAddWorkers + numberOfCheckWorkers)
        val workersStartBarrier = CyclicBarrier(numberOfAddWorkers + numberOfCheckWorkers)
        val workersCompletionBarrier = CyclicBarrier(numberOfAddWorkers + numberOfCheckWorkers)

        repeat(numberOfAddWorkers) {
            testCoroutineScope.launch {
                workersInputPreparationBarrier.await10s()
                val input = generateSequence { phoneNumbersQueue.poll() }.toList()
                workersStartBarrier.await10s()
                input.forEach(phoneNumberSet::add)
                workersCompletionBarrier.await10s()
            }
        }

        repeat(numberOfCheckWorkers) {
            testCoroutineScope.launch {
                workersInputPreparationBarrier.await10s()
                val input = generateSequence { uniquePhoneNumbersQueue.poll() }.toList()
                workersStartBarrier.await10s()
                input.forEach { phoneNumber ->
                    withTimeout(10_000) {
                        while (!phoneNumberSet.contains(phoneNumber)) {
                            delay(10)
                        }
                    }
                }
                workersCompletionBarrier.await10s()
            }
        }

        workersCompletionBarrier.await10s()
        phoneNumberSet.size() shouldBe uniquePhoneNumbers.size

        testCoroutineScope.cancel()
    }
})

private const val MAX_NUMERIC_PHONE_NUMBER_VALUE = 999_999_999
private const val MAX_PHONE_NUMBER_LENGTH = 9

private fun generateRandomPhoneNumbers(count: Int): List<PhoneNumber> =
    generateSequence(::generateRandomPhoneNumber)
        .take(count)
        .toList()

private fun generateRandomPhoneNumber(): PhoneNumber =
    Random.nextInt(MAX_NUMERIC_PHONE_NUMBER_VALUE + 1).toString()
        .padStart(MAX_PHONE_NUMBER_LENGTH, '0')
        .let(::PhoneNumber)

private fun CyclicBarrier.await10s() = await(10, TimeUnit.SECONDS)

private fun PhoneNumber.next(): PhoneNumber {
    val currentValue = (value().trimStart('0').takeIf { it.isNotBlank() } ?: "0")
        .toInt()
    val nextValue = (currentValue + 1)
        .let { if (it > MAX_NUMERIC_PHONE_NUMBER_VALUE) 0 else it }
    return PhoneNumber(nextValue.toString().padStart(MAX_PHONE_NUMBER_LENGTH, '0'))
}

