package edu.iis.mto.testreactor.atm;

import static org.hamcrest.MatcherAssert.assertThat;

import edu.iis.mto.testreactor.atm.bank.Bank;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Currency;

@ExtendWith(MockitoExtension.class)
class ATMachineTest {
	@Mock
	Bank bankMock;
	Currency validCurrency;
	//Currency invalidCurrency;


	@BeforeEach
	void setUp() throws Exception {
	}

	@Test
	void itCompiles() {
		assertThat(true, Matchers.equalTo(true));
	}

}
