package edu.iis.mto.testreactor.atm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import edu.iis.mto.testreactor.atm.bank.AccountException;
import edu.iis.mto.testreactor.atm.bank.AuthorizationException;
import edu.iis.mto.testreactor.atm.bank.Bank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {
	@Mock
	Bank bankMock;
	//valid amount for default settings
	private final int DEFAULT_VALID_AMOUNT_OF_MONEY = 2000;
	//just 10 more than default atm posses
	private final int DEFAULT_INVALID_AMOUNT_OF_MONEY = 8810;
	//atm cannot release this amount of money this should be int
	private final double INVALID_AMOUNT_FOR_ATM_WITHDRAWAL = 101.1;
	private final Withdrawal EXPECTED_WITHDRAWAL = Withdrawal.create(List.of(BanknotesPack.create(4, Banknote.PL_500)));
	/*
		Nie jestem pewien czy to poprawny zapis z kodu wynika, ze obslugujemy tylko polska walute ?(class Banknote)
		w przypadku uruchomienia tego polecenia z innym locale kod moglby sie "wysypac".
	*/
	Currency validCurrency = Currency.getInstance(Locale.getDefault());
	//Currency invalidCurrency;
	ATMachine testedATMMachine;
	PinCode nonsignificantPinCode;
	Card nonsignificantCard;

	Money validAmount;


	@BeforeEach
	void setUp() throws Exception {
		testedATMMachine = new ATMachine(bankMock, validCurrency);
		testedATMMachine.setDeposit(createDefaultDeposit(10));
		nonsignificantPinCode = PinCode.createPIN(1, 2, 3, 4);
		nonsignificantCard = Card.create("1");
		validAmount = new Money(DEFAULT_VALID_AMOUNT_OF_MONEY, validCurrency);
	}

	@Test
	void itCompiles() {
		assertThat(true, Matchers.equalTo(true));
	}

	@Test
	void withdrawingProperAmountExpectingSuccessWithNoErrors() throws ATMOperationException {
		Withdrawal withdrewMoney = testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, validAmount);
		Assertions.assertEquals(EXPECTED_WITHDRAWAL, withdrewMoney);
	}

	@Test
	void withdrawingProperAmountExpectingSuccessWithNoErrorsOneBanknoteOfEachType() throws ATMOperationException {
		int amountForOneOfEach = 500 + 200 + 100 + 50 + 20 + 10;
		Money amountToWithdraw = new Money(amountForOneOfEach, validCurrency);
		Withdrawal expectedOneOfEachType = Withdrawal.create(
				List.of(
						BanknotesPack.create(1, Banknote.PL_500),
						BanknotesPack.create(1, Banknote.PL_200),
						BanknotesPack.create(1, Banknote.PL_100),
						BanknotesPack.create(1, Banknote.PL_50),
						BanknotesPack.create(1, Banknote.PL_20),
						BanknotesPack.create(1, Banknote.PL_10)
				));
		Withdrawal withdrewMoney = testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, amountToWithdraw);
		Assertions.assertEquals(expectedOneOfEachType, withdrewMoney);
	}

	@Test
	void checkingIfATMDepositIsCorrectAfterWithdrawalExpectingNineBanknotesOfEachType() throws ATMOperationException {
		int amountForOneOfEach = 500 + 200 + 100 + 50 + 20 + 10;
		Money amountToWithdraw = new Money(amountForOneOfEach, validCurrency);
		MoneyDeposit expectedNineOfEachType = MoneyDeposit.create(validCurrency,
				List.of(
						BanknotesPack.create(9, Banknote.PL_500),
						BanknotesPack.create(9, Banknote.PL_200),
						BanknotesPack.create(9, Banknote.PL_100),
						BanknotesPack.create(9, Banknote.PL_50),
						BanknotesPack.create(9, Banknote.PL_20),
						BanknotesPack.create(9, Banknote.PL_10)
				));
		testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, amountToWithdraw);
		MoneyDeposit leftInDeposit = testedATMMachine.getCurrentDeposit();
		Assertions.assertEquals(expectedNineOfEachType, leftInDeposit);
	}

	@Test
	void withdrawingInvalidAmountNotEnoughMoneyInDepositExpectingATMOperationExceptionErrorCodeWrongAmount() throws ATMOperationException {
		Money invalidAmount = new Money(DEFAULT_INVALID_AMOUNT_OF_MONEY, validCurrency);
		ErrorCode errorCode = Assertions.assertThrows(ATMOperationException.class, () -> testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, invalidAmount)).getErrorCode();
		Assertions.assertEquals(ErrorCode.WRONG_AMOUNT, errorCode);
	}

	@Test
	void withdrawingInvalidAmountArithmeticErrorExpectingATMOperationExceptionErrorCodeWrongAmount() throws ATMOperationException {
		Money invalidAmount = new Money(INVALID_AMOUNT_FOR_ATM_WITHDRAWAL, validCurrency);
		ErrorCode errorCode = Assertions.assertThrows(ATMOperationException.class, () -> testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, invalidAmount)).getErrorCode();
		Assertions.assertEquals(ErrorCode.WRONG_AMOUNT, errorCode);
	}

	@Test
	void withdrawingInvalidAmountNotEnoughMoneyInAccountExpectingATMOperationExceptionErrorCodeNoFunds() throws ATMOperationException, AccountException {
		Mockito.doThrow(AccountException.class).when(bankMock).charge(any(), any());
		Money invalidAmount = new Money(DEFAULT_VALID_AMOUNT_OF_MONEY, validCurrency);
		ErrorCode errorCode = Assertions.assertThrows(ATMOperationException.class, () -> testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, invalidAmount)).getErrorCode();
		Assertions.assertEquals(ErrorCode.NO_FUNDS_ON_ACCOUNT, errorCode);
	}

	@Test
	void invalidAccountDataExpectingATMOperationExceptionErrorCodeAuthorization() throws ATMOperationException, AccountException, AuthorizationException {
		Mockito.doThrow(AuthorizationException.class).when(bankMock).authorize(any(), any());
		Money invalidAmount = new Money(DEFAULT_VALID_AMOUNT_OF_MONEY, validCurrency);
		ErrorCode errorCode = Assertions.assertThrows(ATMOperationException.class, () -> testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, invalidAmount)).getErrorCode();
		Assertions.assertEquals(ErrorCode.AUTHORIZATION, errorCode);
	}

	@Test
	void invalidCurrencyExpectingATMOperationExceptionErrorCodeWrongCurrency() throws ATMOperationException, AccountException, AuthorizationException {
		Currency invalidCurrency = Currency.getInstance("EUR");
		Money invalidAmount = new Money(DEFAULT_VALID_AMOUNT_OF_MONEY, invalidCurrency);
		ErrorCode errorCode = Assertions.assertThrows(ATMOperationException.class, () -> testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, invalidAmount)).getErrorCode();
		Assertions.assertEquals(ErrorCode.WRONG_CURRENCY, errorCode);
	}

	@Test
	void verifyingOrderOfOperationsDuringSuccessfulOperation() throws ATMOperationException, AccountException, AuthorizationException {
		InOrder order = Mockito.inOrder(bankMock);

		Withdrawal withdrewMoney = testedATMMachine.withdraw(nonsignificantPinCode, nonsignificantCard, validAmount);
		Assertions.assertEquals(EXPECTED_WITHDRAWAL, withdrewMoney);

		order.verify(bankMock).authorize(nonsignificantPinCode.getPIN(), nonsignificantCard.getNumber());
		order.verify(bankMock).charge(any(), eq(validAmount));
	}


	private MoneyDeposit createDefaultDeposit(int countForEachPack) {
		ArrayList<BanknotesPack> banknotesPacks = new ArrayList<>();
		for (Banknote banknote : Banknote.getDescFor(validCurrency))
			banknotesPacks.add(BanknotesPack.create(countForEachPack, banknote));
		return MoneyDeposit.create(validCurrency, banknotesPacks);
	}

}
