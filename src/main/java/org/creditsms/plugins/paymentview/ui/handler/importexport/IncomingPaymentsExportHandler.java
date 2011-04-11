package org.creditsms.plugins.paymentview.ui.handler.importexport;

import java.io.File;
import java.io.IOException;
import java.util.List;

import net.frontlinesms.csv.CsvRowFormat;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.handler.importexport.ExportDialogHandler;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.creditsms.plugins.paymentview.csv.CsvUtils;
import org.creditsms.plugins.paymentview.data.domain.IncomingPayment;
import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment;
import org.creditsms.plugins.paymentview.data.dummy.DummyData;
import org.creditsms.plugins.paymentview.data.importexport.CsvExporter;
import org.creditsms.plugins.paymentview.data.repository.IncomingPaymentDao;

public class IncomingPaymentsExportHandler extends ExportDialogHandler<IncomingPayment> {

	/** I18n Text Key: TODO document */
	private static final String MESSAGE_EXPORTING_SELECTED_CONTACTS = "plugins.paymentview.message.exporting.selected.client";
	private static final String UI_FILE_OPTIONS_PANEL_CLIENT = "/ui/plugins/paymentview/importexport/pnClientDetails.xml";
	/** i18n Text Key: "Active" */
	private static final String COMPONENT_CB_PHONE_NUMBER = "cbPhoneNumber";
	private static final String COMPONENT_CB_ACCOUNT = "cbAccount";
	private static final String COMPONENT_CB_AMOUNT_PAID = "cbAmountPaid";
	private static final String COMPONENT_CB_TIME_PAID = "cbTimePaid";
	private static final String COMPONENT_CB_OUTGOING_NOTES = "cbNotes";
	private static final String COMPONENT_CB_OUTGOING_CONFIRMATION = "cbConfirmation";
	 
	private IncomingPaymentDao incomingPaymentDao;
	
	public IncomingPaymentsExportHandler(UiGeneratorController ui) {
		super(IncomingPayment.class, ui);
		incomingPaymentDao = DummyData.INSTANCE.getIncomingPaymentDao();
	}

	@Override
	protected String getWizardTitleI18nKey() {
		return MESSAGE_EXPORTING_SELECTED_CONTACTS;
	}
	
	@Override
	protected String getOptionsFilePath() {
		return UI_FILE_OPTIONS_PANEL_CLIENT;
	}
	
	@Override
	public void doSpecialExport(String dataPath) throws IOException {
		log.debug("Exporting all contacts..");
		exportIncomingPayment(this.incomingPaymentDao.getAllIncomingPayments(), dataPath); 
	}

	@Override
	public void doSpecialExport(String dataPath, List<IncomingPayment> selected) throws IOException {
		exportIncomingPayment(selected, dataPath);
	}
	
	/**
	 * Export the supplied contacts using settings set in {@link #wizardDialog}.
	 * @param incomingPayment The contacts to export
	 * @param filename The file to export the contacts to
	 * @throws IOException 
	 */
	private void exportIncomingPayment(List<IncomingPayment> incomingPayment, String filename) throws IOException { 
		CsvRowFormat rowFormat = getRowFormatForIncomingPayment();
		
		if (!rowFormat.hasMarkers()) {
			uiController.alert(InternationalisationUtils.getI18nString(MESSAGE_NO_FIELD_SELECTED));
			log.trace("EXIT");
			return;
		}
		
		log.debug("Row Format [" + rowFormat + "]");
		
		CsvExporter.exportIncomingPayment(new File(filename), incomingPayment, rowFormat);
		uiController.setStatus(InternationalisationUtils.getI18nString(MESSAGE_EXPORT_TASK_SUCCESSFUL));
		this.uiController.infoMessage(InternationalisationUtils.getI18nString(MESSAGE_EXPORT_TASK_SUCCESSFUL));
	}
	
	protected CsvRowFormat getRowFormatForIncomingPayment() { 
		CsvRowFormat rowFormat = new CsvRowFormat();
		addMarker(rowFormat, CsvUtils.MARKER_INCOMING_PHONE_NUMBER,
				COMPONENT_CB_PHONE_NUMBER);
		addMarker(rowFormat, CsvUtils.MARKER_INCOMING_ACCOUNT,
				COMPONENT_CB_ACCOUNT);
		addMarker(rowFormat, CsvUtils.MARKER_INCOMING_AMOUNT_PAID,
				COMPONENT_CB_AMOUNT_PAID);
		addMarker(rowFormat, CsvUtils.MARKER_INCOMING_TIME_PAID,
				COMPONENT_CB_TIME_PAID);
		addMarker(rowFormat, CsvUtils.MARKER_OUTGOING_NOTES,
				COMPONENT_CB_OUTGOING_NOTES);
		addMarker(rowFormat, CsvUtils.MARKER_OUTGOING_CONFIRMATION,
				COMPONENT_CB_OUTGOING_CONFIRMATION); 
		return rowFormat;
	}
}