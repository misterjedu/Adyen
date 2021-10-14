package com.adyen.android.assignment

import com.adyen.android.assignment.money.Change
import com.adyen.android.assignment.money.MonetaryElement

/**
 * The CashRegister class holds the logic for performing transactions.
 *
 * @param change The change that the CashRegister is holding.
 */

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

    private var monetaryElements = change.getElements().toList().asReversed();
    var monetaryElementsCount = change.getElements().size

    fun performTransaction(price: Long, amountPaid: Change): Change {

        val totalAmountPaid = amountPaid.total
        val balance = totalAmountPaid - price

        if (balance == 0L) {
            return Change.none();
        }
        if (balance < 0 || balance > change.total)
            throw TransactionException("Insufficient amount!")

        var remainingTotal: Long = balance


        val moniesToRemove: MutableList<Long> = arrayListOf()
        repeat(monetaryElementsCount) { num -> moniesToRemove.add(num, 0) }

        var biggestMoneyElementIndex = -1
        var counter = 0


        while (counter < monetaryElementsCount) {

            val monetaryElements = change.getElements().toList().reversed()
            val currentMonetaryElement = monetaryElements[counter]

            if (biggestMoneyElementIndex == counter) {

                var quantityOfMonetaryElement = moniesToRemove[counter]
                if (quantityOfMonetaryElement == 0L) {
                    biggestMoneyElementIndex = -1
                } else {
                    quantityOfMonetaryElement -= 1
                    remainingTotal += currentMonetaryElement.minorValue * 1
                }

                moniesToRemove[counter] = quantityOfMonetaryElement

            } else {

                var amountOfMonetaryElement = remainingTotal.div(currentMonetaryElement.minorValue)
                if (amountOfMonetaryElement > change.getCount(currentMonetaryElement)) {
                    amountOfMonetaryElement = 0
                }
                if (amountOfMonetaryElement > 0) {
                    if (biggestMoneyElementIndex == -1) {
                        biggestMoneyElementIndex = counter
                    }
                    remainingTotal =
                        remainingTotal.minus(currentMonetaryElement.minorValue * amountOfMonetaryElement)
                }
                moniesToRemove[counter] = amountOfMonetaryElement

            }

            if (counter == monetaryElements.size - 1) {
                if (remainingTotal != 0L && biggestMoneyElementIndex != -1 && biggestMoneyElementIndex != counter) {
                    counter = biggestMoneyElementIndex - 1
                }
            }

            counter += 1

        }

        if (moniesToRemove.sum() == 0L) {
            throw TransactionException("Insufficient bills in register!")
        }
        removeQuantities(moniesToRemove)

        return createChange(monetaryElements, moniesToRemove)
    }

    private fun removeQuantities(quantities: List<Long>) {
        if (quantities.size != monetaryElements.size) {
            throw TransactionException("Invalid set of quantities to remove.")
        }
        for ((index, value) in quantities.withIndex()) {
            val monetaryElement: MonetaryElement = monetaryElements.elementAt(index)
            if (change.getCount(monetaryElement) < value) {
                throw TransactionException("Not enough \$${monetaryElement.minorValue} bills to remove")
            }
            change.remove(monetaryElement, value.toInt())
        }
    }

    private fun createChange(
        monetaryElements: List<MonetaryElement>,
        moniesToRemove: MutableList<Long>
    ): Change {
        val change = Change()
        for (i in monetaryElements.indices) {
            change.add(monetaryElements[i], moniesToRemove[i].toInt())
        }
        return change
    }

    class TransactionException(message: String, cause: Throwable? = null) :
        Exception(message, cause)

}
