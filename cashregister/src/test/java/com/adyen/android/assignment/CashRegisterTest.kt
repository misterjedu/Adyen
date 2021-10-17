package com.adyen.android.assignment

import com.adyen.android.assignment.CashRegister.*
import com.adyen.android.assignment.money.Bill
import com.adyen.android.assignment.money.Change
import com.adyen.android.assignment.money.Coin
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CashRegisterTest {

    @Test
    fun successful_transaction() {

        val change = Change()
            .add(Bill.FIVE_EURO, 3)

        val register = CashRegister(change)

        val amountPaid = Change()
            .add(Bill.FIVE_EURO, 10)
            .add(Coin.ONE_CENT, 3)
            .add(Coin.TWO_CENT, 4)
            .add(Coin.FIVE_CENT, 10)

        val actualChange = register.performTransaction(35_40, amountPaid)

        val expectedChange = Change()
            .add(Bill.FIVE_EURO, 3)
            .add(Coin.ONE_CENT, 1)
            .add(Coin.FIVE_CENT, 4)

        assertEquals(expectedChange, actualChange)
    }


    @Test
    fun successful_transaction_no_change_received() {

        val change = Change()
            .add(Bill.FIVE_EURO, 3)

        val register = CashRegister(change)

        val amountPaid = Change()
            .add(Bill.FIVE_EURO, 7)
            .add(Coin.TWENTY_CENT, 2)

        val actualChange = register.performTransaction(35_40, amountPaid)

        val expectedChange = Change()

        assertEquals(expectedChange, actualChange)
    }

    @Test
    fun transaction_with_insufficient_amount_paid_throws_exception() {
        val change = Change()
            .add(Bill.FIVE_EURO, 1)

        val cashRegister = CashRegister(change)

        val amountPaid = Change()
            .add(Bill.ONE_HUNDRED_EURO, 1)
            .add(Bill.TWENTY_EURO, 1)
            .add(Bill.FIVE_HUNDRED_EURO, 1)

        assertThrows(TransactionException("Not enough change to purchase item").javaClass) {
            cashRegister.performTransaction(1000_00, amountPaid)
        }
    }

    @Test
    fun transaction_with_unavailable_change_throws_exception() {
        val change = Change()
            .add(Bill.FIVE_EURO, 1)

        val cashRegister = CashRegister(change)

        val amountPaid = Change()
            .add(Bill.FIVE_HUNDRED_EURO, 1)

        assertThrows(TransactionException("Change required not available").javaClass) {
            cashRegister.performTransaction(50_00, amountPaid)
        }
    }


}
