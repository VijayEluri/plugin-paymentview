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
@Table(name = OutgoingPayment.TABLE_NAME)
public class OutgoingPayment {
	public static final String TABLE_NAME = "OutGoingpayment";
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "outgoingPaymentId", nullable = false, unique = true)
	private long outgoingPaymentId;

	@Column(name = "phoneNumber", nullable = false, unique = false)
	private String phoneNumber;

	@Column(name = "amountPaid", nullable = false, unique = false)
	private BigDecimal amountPaid;

	@Column(name = "timePaid", nullable = false, unique = false)
	private Date timePaid;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "accountId", nullable = true)
	private Account account;

	@Column(name = "notes", nullable = false, unique = false)
	private String notes;

	@Column(name = "confirmation", nullable = false, unique = false)
	private boolean confirmation;

	public OutgoingPayment(String phoneNumber, BigDecimal amountPaid,
			long timePaid, Account account, String notes, boolean confirmation) {
		this.phoneNumber = phoneNumber;
		this.amountPaid = amountPaid;
		this.timePaid = new Date(timePaid);
		this.account = account;
		this.notes = notes;
		this.confirmation = confirmation;
	}

	public OutgoingPayment(String phoneNumber, BigDecimal amountPaid,
			Date timePaid, Account account, String notes, boolean confirmation) {
		super();
		this.phoneNumber = phoneNumber;
		this.amountPaid = amountPaid;
		this.timePaid = timePaid;
		this.account = account;
		this.notes = notes;
		this.confirmation = confirmation;
	}

	//For Dummy Data... @ian, Remove Later
	public OutgoingPayment() {
	}

	public long getOutgoingPaymentId() {
		return outgoingPaymentId;
	}

	public void setOutgoingPaymentId(long outgoingPaymentId) {
		this.outgoingPaymentId = outgoingPaymentId;
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

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public boolean isConfirmation() {
		return confirmation;
	}

	public void setConfirmation(boolean confirmation) {
		this.confirmation = confirmation;
	}

}
