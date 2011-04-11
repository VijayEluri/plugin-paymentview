package org.creditsms.plugins.paymentview.data.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @Author Roy
 * */

@Entity
@Table(name = IncomingPayment.TABLE_NAME)

public class IncomingPayment{
	public static final String TABLE_NAME = "IncomingPayment";
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="incomingPaymentId",
            nullable=false,
            unique=true)
	private long incomingPaymentId;
	
	@Column(name="paymentBy",
			nullable=false,
			unique=false)
	private String paymentBy;
	
	@Column(name="phoneNumber",
			nullable=false,
			unique=false)
	private String phoneNumber;
	
	@Column(name="amountPaid",
			nullable=false,
			unique=false)
	private BigDecimal amountPaid;
		
	@Column(name="timePaid",
			nullable=false,
			unique=false)
	private Date timePaid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accountId", 
			nullable = true)
	private Account account;
	
	//For Dummy Purposes...
	public IncomingPayment(){}
	
	public IncomingPayment(long incomingPaymentId, String paymentBy,
			String phoneNumber, BigDecimal amountPaid, long timePaid, Account account) {
		super();
		this.incomingPaymentId = incomingPaymentId;
		this.paymentBy = paymentBy;
		this.phoneNumber = phoneNumber;
		this.amountPaid = amountPaid;
		this.timePaid = new Date(timePaid);
		this.account = account;
	}

	public IncomingPayment(String paymentBy, String phoneNumber,
			BigDecimal amountPaid, long timePaid, Account account) {
		super();
		this.paymentBy = paymentBy;
		this.phoneNumber = phoneNumber;
		this.amountPaid = amountPaid;
		this.timePaid = new Date(timePaid);
		this.account = account;
	}
	
	public IncomingPayment(String paymentBy, String phoneNumber,
			BigDecimal amountPaid, Date timePaid, Account account) {
		super();
		this.paymentBy = paymentBy;
		this.phoneNumber = phoneNumber;
		this.amountPaid = amountPaid;
		this.timePaid = timePaid;
		this.account = account;
	}

	public long getIncomingPaymentId() {
		return incomingPaymentId;
	}

	public void setIncomingPaymentId(long incomingPaymentId) {
		this.incomingPaymentId = incomingPaymentId;
	}

	public String getPaymentBy() {
		return paymentBy;
	}

	public void setPaymentBy(String paymentBy) {
		this.paymentBy = paymentBy;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}

	public Date getTimePaid() {
		return timePaid;
	}

	public void setTimePaid(Date timePaid) {
		this.timePaid = timePaid;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}