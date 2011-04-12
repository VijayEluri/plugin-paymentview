package org.creditsms.plugins.paymentview.ui.handler.importexport;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.frontlinesms.FrontlineSMSConstants;
import net.frontlinesms.csv.CsvExporter;
import net.frontlinesms.csv.CsvImporter;
import net.frontlinesms.csv.CsvParseException;
import net.frontlinesms.csv.CsvRowFormat;
import net.frontlinesms.ui.Icon;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.handler.importexport.ImportDialogHandler;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.creditsms.plugins.paymentview.csv.CsvUtils;
import org.creditsms.plugins.paymentview.data.dummy.DummyData;
import org.creditsms.plugins.paymentview.data.importexport.OutgoingPaymentCsvImporter;
import org.creditsms.plugins.paymentview.data.repository.AccountDao;
import org.creditsms.plugins.paymentview.data.repository.OutgoingPaymentDao;

public class OutgoingPaymentsImportHandler extends ImportDialogHandler {
	private static final String UI_FILE_OPTIONS_PANEL_CONTACT = "/ui/plugins/paymentview/importexport/pnOutgoingPaymentsDetails.xml";
	/** I18n Text Key: TODO document */
	private static final String MESSAGE_IMPORTING_SELECTED_CLIENTS = "Import Clients";
	/** i18n Text Key: "Active" */
	private static final String I18N_COMMON_ACTIVE = "common.active";
	private static final String COMPONENT_CB_PHONE_NUMBER = "cbPhoneNumber";
	private static final String COMPONENT_CB_ACCOUNT = "cbAccount";
	private static final String COMPONENT_CB_AMOUNT_PAID = "cbAmountPaid";
	private static final String COMPONENT_CB_TIME_PAID = "cbTimePaid"; 
	private static final String COMPONENT_CB_OUTGOING_NOTES = "cbNotes";
	private static final String COMPONENT_CB_OUTGOING_CONFIRMATION = "cbConfirmation";

	// > INSTANCE PROPERTIES
	private OutgoingPaymentCsvImporter importer;
	private int columnCount;
	private OutgoingPaymentDao outgoingPaymentDao = DummyData.INSTANCE.getOutgoingPaymentDao();
	private AccountDao accountDao = DummyData.INSTANCE.getAccountDao();

	public OutgoingPaymentsImportHandler(UiGeneratorController ui) {
		super(ui);
	}

	@Override
	protected String getWizardTitleI18nKey() {
		return MESSAGE_IMPORTING_SELECTED_CLIENTS;
	}

	@Override
	protected String getOptionsFilePath() {
		return UI_FILE_OPTIONS_PANEL_CONTACT;
	}

	@Override
	protected CsvImporter getImporter() {
		return this.importer;
	}

	@Override
	protected void setImporter(String filename) throws CsvParseException {
		this.importer = new OutgoingPaymentCsvImporter(new File(filename));
	}

	@Override
	protected void doSpecialImport(String dataPath) {
		CsvRowFormat rowFormat = getRowFormatForIncomingPayment();
		this.importer.importOutgoingPayments(this.outgoingPaymentDao,
				this.accountDao, rowFormat);
		// this.uiController.refreshContactsTab();
		this.uiController.infoMessage(InternationalisationUtils
				.getI18nString(I18N_IMPORT_SUCCESSFUL));
	}

	private CsvRowFormat getRowFormatForIncomingPayment() {
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

	@Override
	protected void appendPreviewHeaderItems(Object header) {
		int columnCount = 0;
		for (Object checkbox : getCheckboxes()) {
			if (this.uiController.isSelected(checkbox)) {
				String attributeName = this.uiController.getText(checkbox);
				if (this.uiController.getName(checkbox).equals(
						COMPONENT_CB_STATUS)) {
					attributeName = InternationalisationUtils
							.getI18nString(I18N_COMMON_ACTIVE);
				}
				this.uiController.add(header, this.uiController.createColumn(
						attributeName, attributeName));//
				++columnCount;
			}
		}
		this.columnCount = columnCount;
	}

	@Override
	protected Object[] getPreviewRows() {
		List<Object> previewRows = new ArrayList<Object>();
		for (String[] lineValues : this.importer.getRawValues()) {
			previewRows.add(getRow(lineValues));
		}
		return previewRows.toArray();
	}

	protected Object getRow(String[] lineValues) {
		Object row = this.uiController.createTableRow();
		addIncomingPaymentCells(row, lineValues);
		return row;
	}

	private void addIncomingPaymentCells(Object row, String[] lineValues) {
		Object iconCell = this.uiController.createTableCell("");
		this.uiController.setIcon(iconCell, Icon.CONTACT);
		this.uiController.add(row, iconCell);

		for (int i = 0; i < columnCount && i < lineValues.length; ++i) {
			Object cell = this.uiController.createTableCell(lineValues[i]
					.replace(CsvExporter.GROUPS_DELIMITER, ", "));

			if (lineValues[i].equals(InternationalisationUtils
					.getI18nString(FrontlineSMSConstants.COMMON_ACTIVE))) {
				lineValues[i] = lineValues[i].toLowerCase();
				if (!lineValues[i].equalsIgnoreCase("false")
						&& !lineValues[i].equals("dormant")) {
					this.uiController.setIcon(cell, Icon.CIRLCE_TICK);
				} else {
					this.uiController.setIcon(cell, Icon.CANCEL);
				}
			}

			this.uiController.add(row, cell);
		}
	}

	protected List<Object> getCheckboxes() {
		Object pnCheckboxes = this.uiController.find(this.wizardDialog,
				COMPONENT_PN_CHECKBOXES);
		return Arrays.asList(this.uiController.getItems(pnCheckboxes));
	}

}