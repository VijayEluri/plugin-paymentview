package org.creditsms.plugins.paymentview.data.importexport;

import java.io.File;
import java.math.BigDecimal;

import net.frontlinesms.csv.CsvImportReport;
import net.frontlinesms.csv.CsvImporter;
import net.frontlinesms.csv.CsvParseException;
import net.frontlinesms.csv.CsvRowFormat;
import net.frontlinesms.data.DuplicateKeyException;

import org.creditsms.plugins.paymentview.csv.PaymentViewCsvUtils;
import org.creditsms.plugins.paymentview.data.domain.Account;
import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment;
import org.creditsms.plugins.paymentview.data.repository.AccountDao;
import org.creditsms.plugins.paymentview.data.repository.ClientDao;
import org.creditsms.plugins.paymentview.data.repository.OutgoingPaymentDao;

/**
 * @author Ian Onesmus Mukewa <ian@frontlinesms.com>
 */
public class OutgoingPaymentCsvImporter extends CsvImporter {
	/** The delimiter to use between group names when they are exported. */
	protected static final String GROUPS_DELIMITER = "\\\\";

	// > INSTANCE PROPERTIES

	// > CONSTRUCTORS
	public OutgoingPaymentCsvImporter(File importFile) throws CsvParseException {
		super(importFile);
	}

	// > IMPORT METHODS
	/**
	 * Import Payments from a CSV file.
	 * 
	 * @param importFile
	 *            the file to import from
	 * @param contactDao
	 *            ; paymentDao
	 * @param rowFormat
	 * @throws IOException
	 *             If there was a problem accessing the file
	 * @throws CsvParseException
	 *             If there was a problem with the format of the file
	 */
	/**
	 * @param accountDao
	 * @param incomingPaymentDao
	 * @param rowFormat
	 */
	public CsvImportReport importOutgoingPayments(
			OutgoingPaymentDao outgoingPaymentDao, AccountDao accountDao, ClientDao clientDao,
			CsvRowFormat rowFormat) {
		log.trace("ENTER");

		for (String[] lineValues : this.getRawValues()) {
			String phoneNumber = rowFormat.getOptionalValue(lineValues,
					PaymentViewCsvUtils.MARKER_INCOMING_PHONE_NUMBER);
			String amountPaid = rowFormat.getOptionalValue(lineValues,
					PaymentViewCsvUtils.MARKER_INCOMING_AMOUNT_PAID);
			String account = rowFormat.getOptionalValue(lineValues,
					PaymentViewCsvUtils.MARKER_INCOMING_ACCOUNT);
			String notes = rowFormat.getOptionalValue(lineValues,
					PaymentViewCsvUtils.MARKER_OUTGOING_NOTES);

			//TODO - no client linked to the account????????????????????
			Account acc;
			acc = new Account(account,false);
			try {
				accountDao.saveAccount(acc);
			} catch (DuplicateKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

//TODO: last arguemtn --> paymetnID
			Client client = clientDao.getClientByPhoneNumber(phoneNumber);
			OutgoingPayment outgoingPayment = new OutgoingPayment(client,
					new BigDecimal(amountPaid), acc, notes);
			outgoingPaymentDao.saveOutgoingPayment(outgoingPayment);

		}

		log.trace("EXIT");

		return new CsvImportReport();

	}
}
