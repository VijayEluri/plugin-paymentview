package org.creditsms.plugins.paymentview.ui.handler.outgoingpayments;

import java.text.SimpleDateFormat;
import java.util.List;

import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.handler.BaseTabHandler;

import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment;
import org.creditsms.plugins.paymentview.data.dummy.DummyData;
import org.creditsms.plugins.paymentview.data.repository.ClientDao;
import org.creditsms.plugins.paymentview.data.repository.OutgoingPaymentDao;
import org.creditsms.plugins.paymentview.ui.handler.importexport.OutgoingPaymentsExportHandler;
import org.creditsms.plugins.paymentview.ui.handler.importexport.OutgoingPaymentsImportHandler;

public class SentPaymentsTabHandler extends BaseTabHandler {
	private static final String XML_SENTPAYMENTS_TAB = "/ui/plugins/paymentview/outgoingpayments/innertabs/sentpayments.xml";
	private static final String COMPONENT_CLIENT_TABLE = "tbl_clients";
	private Object sentPaymentsTab;
	private static final String TABBED_PANE_MAIN = "tabbedPaneMain";
	private static final String COMPONENT_INCOMING_PAYMENTS_TABLE = "tbl_clients";
	private OutgoingPaymentDao outgoingPaymentDao = DummyData.INSTANCE
			.getOutgoingPaymentDao();
	private ClientDao clientsDao = DummyData.INSTANCE.getClientDao();
	SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
	SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");

	public SentPaymentsTabHandler(UiGeneratorController ui) {
		super(ui);
		init();
	}

	@Override
	public void refresh() {
		populateOutgoingPaymentsTable();
	}

	@Override
	protected Object initialiseTab() {
		sentPaymentsTab = ui.loadComponentFromFile(XML_SENTPAYMENTS_TAB, this);
		return sentPaymentsTab;
	}

	// > PRIVATE HELPER METHODS
	private void populateOutgoingPaymentsTable() {
		Object table = find(COMPONENT_INCOMING_PAYMENTS_TABLE);
		List<OutgoingPayment> outgoingPayments = outgoingPaymentDao
				.getAllOutgoingPayments();
		for (OutgoingPayment o : outgoingPayments) {
			ui.add(table, createRow(o));
		}
	}

	

	private Object createRow(OutgoingPayment o) {
		Object row = ui.createTableRow();
		ui.add(row,
				ui.createTableCell(o.getAccount().getClient().getFirstName() + " "
						+ o.getAccount().getClient().getOtherName()));
		ui.add(row, ui.createTableCell(o.getPhoneNumber()));
		ui.add(row, ui.createTableCell(o.getAmountPaid().toString()));
		ui.add(row, ui.createTableCell(df.format(o.getTimePaid())));
		ui.add(row, ui.createTableCell(tf.format(o.getTimePaid())));
		ui.add(row, ui.createTableCell(Long.toString(o.getAccount().getAccountNumber())));
		ui.add(row, ui.createTableCell(o.getNotes()));
		return row;
	}
	
	public void exportPayments() {
		new OutgoingPaymentsExportHandler(ui).showWizard();
		this.refresh();
	}
	
	public void importPayments() {
		new OutgoingPaymentsImportHandler(ui).showWizard();
		this.refresh();
	}
}
