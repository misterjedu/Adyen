package com.adyen.android.assignment

import com.adyen.android.assignment.money.Bill
import com.adyen.android.assignment.money.Change
import com.adyen.android.assignment.money.Coin
import com.adyen.android.assignment.money.MonetaryElement
import java.util.*
import kotlin.collections.ArrayList

/**
 * The CashRegister class holds the logic for performing transactions.
 *
 * @param change The change that the CashRegister is holding.
 */

fun main() {


    val change = Change()
        .add(Bill.FIVE_EURO, 3)

    val register = CashRegister(change)

    val amountPaid = Change()
        .add(Bill.FIVE_EURO, 7)
        .add(Coin.TWENTY_CENT, 2)

    register.performTransaction(35_40, amountPaid)

}

class CashRegister(private val change: Change) {
    /**
     * Performs a transaction for a product/products with a certain price and a given amount.
     *
     * @param price The price of the product(s).
     * @param amountPaid The amount paid by the shopper.
     *
     * @return The change for the transaction.
     *
     * @throws TransactionException If the transaction cannot be performed.
     */

    var elementsToReturn: ArrayList<MonetaryElement> = ArrayList()

    fun performTransaction(price: Long, amountPaid: Change): Change {

        var totalAmountPaid = amountPaid.total

        var amountToReturnToCustomer = totalAmountPaid - price

        println("Amunt to return $amountToReturnToCustomer")
        println("Amunt paid $totalAmountPaid")

        val allChangeLeft: MutableList<MonetaryElement> = mutableListOf()

        /**
         * Once a user has paid, the monies paid can be used as change too
         * Therefore, it's added to the list of potential change element to be returned
         */

        allChangeLeft.addAll(getAllElementsInChange(change))
        allChangeLeft.addAll(getAllElementsInChange(amountPaid))
        allChangeLeft.reverse()



        if (price > totalAmountPaid) {
            throw TransactionException("Not enough change to purchase item")
        } else {
            sum_up_recursive(allChangeLeft, amountToReturnToCustomer, ArrayList())
        }

        val changeToReturn = Change()
        for (item in elementsToReturn) {
            changeToReturn.add(item, 1)
        }

        if (amountToReturnToCustomer != 0L && changeToReturn.total == 0L) {
            throw TransactionException("Change required not available")
        }

        return changeToReturn
    }


    private fun sum_up_recursive(
        elements: List<MonetaryElement>,
        target: Long,
        partial: ArrayList<MonetaryElement>
    ) {

        var sum = 0L

        for (monetaryElement in partial) {
            sum += monetaryElement.minorValue
        }

        if (sum == target) {

            if (elementsToReturn.isEmpty()) {
                elementsToReturn = partial
            } else if (partial.size < elementsToReturn.size) {
                elementsToReturn = partial
            }

        }

        if (sum >= target) return

        for (i in elements.indices) {
            val remaining = mutableListOf<MonetaryElement>()

            val n = elements[i]

            for (j in i + 1 until elements.size) {
                remaining.add(elements[j])
            }

            val partial_rec = ArrayList(partial)

            partial_rec.add(n)

            sum_up_recursive(remaining, target, partial_rec)
        }
    }


    /**
     * Return all the monetary elements in a change.
     */
    private fun getAllElementsInChange(change: Change): MutableList<MonetaryElement> {
        val allElements: MutableList<MonetaryElement> = mutableListOf()

        for (element in change.getElements()) {
            for (item in 0 until change.getCount(element)) {
                allElements.add(element)
            }
        }
        return allElements
    }

    class TransactionException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

}
