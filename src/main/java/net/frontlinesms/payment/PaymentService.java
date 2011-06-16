package net.frontlinesms.payment;

import java.math.BigDecimal;

import org.creditsms.plugins.paymentview.data.domain.Account;
import org.creditsms.plugins.paymentview.data.domain.Client;

public interface PaymentService {
	String getPin();
	void setPin(String pin);
	
//	void makePayment(Account account, BigDecimal amount) throws PaymentServiceException;
	void makePayment(Client client, BigDecimal amount) throws PaymentServiceException;
	void checkBalance() throws PaymentServiceException;
}
