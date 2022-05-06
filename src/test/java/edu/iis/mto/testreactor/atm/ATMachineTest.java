package edu.iis.mto.testreactor.atm;

import static org.hamcrest.MatcherAssert.assertThat;

import edu.iis.mto.testreactor.atm.bank.Bank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {
	@Mock
	Bank bankMock;
	/*
	Nie jestem pewien czy to poprawny zapis z kodu wynika, ze obslugujemy tylko polska walute ?(class Banknote)
	w przypadku uruchomienia tego polecenia z innym locale kod moglby sie wysypac.
	*/
	Currency validCurrency = Currency.getInstance(Locale.getDefault());
	//Currency invalidCurrency;
	ATMachine testedATMMachine;
	PinCode properPinCode;
	Card validCard;
	Money validAmmount;


	@BeforeEach
	void setUp() throws Exception {
		testedATMMachine = new ATMachine(bankMock, validCurrency);
		testedATMMachine.setDeposit(createDefaultDeposit(10));
		properPinCode = PinCode.createPIN(1, 2, 3, 4);
		validCard = Card.create("1");
		validAmmount = new Money(2000, validCurrency);
	}

	@Test
	void itCompiles() {
		assertThat(true, Matchers.equalTo(true));
	}

	@Test
	void withrawingProperAmmountExpectingSuccesWithNoErrors() throws ATMOperationException {
		Withdrawal withdrewMoney = testedATMMachine.withdraw(properPinCode, validCard, validAmmount);
		System.out.println(withdrewMoney);

	}

	private MoneyDeposit createDefaultDeposit(int countForEachPack) {
		ArrayList<BanknotesPack> banknotesPacks = new ArrayList<>();
		for (Banknote banknote : Banknote.getDescFor(validCurrency))
			banknotesPacks.add(BanknotesPack.create(countForEachPack, banknote));
		return MoneyDeposit.create(validCurrency, banknotesPacks);
	}

}
