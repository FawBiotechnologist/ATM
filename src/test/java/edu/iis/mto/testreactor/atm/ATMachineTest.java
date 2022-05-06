package edu.iis.mto.testreactor.atm;

import static org.hamcrest.MatcherAssert.assertThat;

import edu.iis.mto.testreactor.atm.bank.Bank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {
	@Mock
	Bank bankMock;
	/*
	Nie jestem pewien czy to poprawny zapis z kodu wynika, ze obslugujemy tylko polska walute ?(class Banknote)
	w przypadku uruchomienia tego polecenia z innym locale kod moglby sie wysypac.
	*/
	private final int DEFAULT_VALID_AMOUNT_OF_MONEY = 2000;
	private final Withdrawal EXPECTED_WITHDRAWAL = Withdrawal.create(List.of(BanknotesPack.create(4, Banknote.PL_500)));
	Currency validCurrency = Currency.getInstance(Locale.getDefault());
	//Currency invalidCurrency;
	ATMachine testedATMMachine;
	PinCode properPinCode;
	Card validCard;
	Money validAmount;


	@BeforeEach
	void setUp() throws Exception {
		testedATMMachine = new ATMachine(bankMock, validCurrency);
		testedATMMachine.setDeposit(createDefaultDeposit(10));
		properPinCode = PinCode.createPIN(1, 2, 3, 4);
		validCard = Card.create("1");
		validAmount = new Money(DEFAULT_VALID_AMOUNT_OF_MONEY, validCurrency);
	}

	@Test
	void itCompiles() {
		assertThat(true, Matchers.equalTo(true));
	}

	@Test
	void withdrawingProperAmountExpectingSuccessWithNoErrors() throws ATMOperationException {
		Withdrawal withdrewMoney = testedATMMachine.withdraw(properPinCode, validCard, validAmount);
		Assertions.assertEquals(EXPECTED_WITHDRAWAL, withdrewMoney);
	}

	private MoneyDeposit createDefaultDeposit(int countForEachPack) {
		ArrayList<BanknotesPack> banknotesPacks = new ArrayList<>();
		for (Banknote banknote : Banknote.getDescFor(validCurrency))
			banknotesPacks.add(BanknotesPack.create(countForEachPack, banknote));
		return MoneyDeposit.create(validCurrency, banknotesPacks);
	}

}
