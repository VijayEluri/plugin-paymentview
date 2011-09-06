package org.creditsms.plugins.paymentview.ui.handler.tabincomingpayments;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.events.DatabaseEntityNotification;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.events.FrontlineUiUpateJob;
import net.frontlinesms.ui.handler.BaseTabHandler;
import net.frontlinesms.ui.handler.ComponentPagingHandler;
import net.frontlinesms.ui.handler.PagedComponentItemProvider;
import net.frontlinesms.ui.handler.PagedListDetails;
import net.frontlinesms.ui.i18n.InternationalisationUtils;

import org.creditsms.plugins.paymentview.PaymentViewPluginController;
import org.creditsms.plugins.paymentview.analytics.TargetAnalytics;
import org.creditsms.plugins.paymentview.data.domain.Account;
import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.domain.IncomingPayment;
import org.creditsms.plugins.paymentview.data.domain.LogMessage;
import org.creditsms.plugins.paymentview.data.domain.Target;
import org.creditsms.plugins.paymentview.data.repository.AccountDao;
import org.creditsms.plugins.paymentview.data.repository.ClientDao;
import org.creditsms.plugins.paymentview.data.repository.IncomingPaymentDao;
import org.creditsms.plugins.paymentview.data.repository.LogMessageDao;
import org.creditsms.plugins.paymentview.data.repository.TargetDao;
import org.creditsms.plugins.paymentview.ui.handler.AuthorisationCodeHandler;
import org.creditsms.plugins.paymentview.ui.handler.importexport.IncomingPaymentsExportHandler;
import org.creditsms.plugins.paymentview.ui.handler.tabincomingpayments.dialogs.AutoReplyPaymentsDialogHandler;
import org.creditsms.plugins.paymentview.ui.handler.tabincomingpayments.dialogs.EditIncomingPaymentDialogHandler;
import org.creditsms.plugins.paymentview.ui.handler.tabincomingpayments.dialogs.FormatterMarkerType;
import org.creditsms.plugins.paymentview.userhomepropeties.incomingpayments.AutoReplyProperties;
import org.creditsms.plugins.paymentview.utils.PaymentPluginConstants;
import org.creditsms.plugins.paymentview.utils.PvUtils;

public class IncomingPaymentsTabHandler extends BaseTabHandler implements
		PagedComponentItemProvider, EventObserver{
	private static final String CONFIRM_DIALOG = "confirmDialog";
	private static final String INVALID_DATE = "Please enter a correct starting date.";
	private static final String ENABLE_AUTOREPLY = "Enable autoreply";
	private static final String TXT_END_DATE = "txt_endDate";
	private static final String TXT_START_DATE = "txt_startDate";
	private static final String DISABLE_AUTOREPLY = "Disable autoreply";
	private static final String COMPONENT_INCOMING_PAYMENTS_TABLE = "tbl_clients";
	private static final String COMPONENT_PANEL_INCOMING_PAYMENTS_TABLE = "pnl_clients";
	private static final String XML_INCOMING_PAYMENTS_TAB = "/ui/plugins/paymentview/incomingpayments/tabincomingpayments.xml";
	private static final String CONFIRM_DELETE_INCOMING = "message.confirm.delete.incoming";
	
	private AutoReplyProperties autoReplyProperties = AutoReplyProperties.getInstance();
	
	private static final String ICON_STATUS_TRUE = "/icons/led_green.png";
	private static final String STATUS_LABEL_COMPONENT = "status";
	private static final String ICON_STATUS_FALSE = "/icons/led_red.png";
	private Object status_label;
	
	protected IncomingPaymentDao incomingPaymentDao;
	private LogMessageDao logMessageDao;
	
	private Object incomingPaymentsTab;

	protected Object incomingPaymentsTableComponent;
	protected ComponentPagingHandler incomingPaymentsTablePager;
	private Object pnlIncomingPaymentsTableComponent;
	private PaymentViewPluginController pluginController;
	private Object dialogConfirmation;
	private Object fldStartDate;
	private Object fldEndDate;
	private Object dialogAutoReplyPayments;
	private Date startDate;
	private Date endDate;
	protected int totalItemCount = 0;
	private ClientDao clientDao;
	private FrontlineSMS frontlineController;
	private TargetAnalytics targetAnalytics;
	private AccountDao accountDao;
	private TargetDao targetDao;


	public IncomingPaymentsTabHandler(UiGeneratorController ui,
			PaymentViewPluginController pluginController) {
		super(ui);
		this.incomingPaymentDao = pluginController.getIncomingPaymentDao();
		this.clientDao = pluginController.getClientDao();
		this.logMessageDao = pluginController.getLogMessageDao();
		this.pluginController = pluginController;
		this.frontlineController = ui.getFrontlineController();
		this.frontlineController.getEventBus().registerObserver(this);
		this.targetAnalytics = new TargetAnalytics();
		this.targetAnalytics.setIncomingPaymentDao(pluginController.getIncomingPaymentDao());
		this.targetDao = pluginController.getTargetDao();
		this.targetAnalytics.setTargetDao(targetDao);
		this.accountDao = pluginController.getAccountDao();
		init();
	}
	
	@Override
	protected Object initialiseTab() {
		incomingPaymentsTab = ui.loadComponentFromFile(getXMLFile(), this);
		fldStartDate = ui.find(incomingPaymentsTab, TXT_START_DATE);
		fldEndDate = ui.find(incomingPaymentsTab, TXT_END_DATE);
		status_label = ui.find(incomingPaymentsTab, STATUS_LABEL_COMPONENT);
		incomingPaymentsTableComponent = ui.find(incomingPaymentsTab, COMPONENT_INCOMING_PAYMENTS_TABLE);
		incomingPaymentsTablePager = new ComponentPagingHandler(ui, this, incomingPaymentsTableComponent);
		pnlIncomingPaymentsTableComponent = ui.find(incomingPaymentsTab, COMPONENT_PANEL_INCOMING_PAYMENTS_TABLE);
		this.ui.add(pnlIncomingPaymentsTableComponent, this.incomingPaymentsTablePager.getPanel());
		return incomingPaymentsTab;
	}
	
	public void tryToggleAutoReply(){
		if (!autoReplyProperties.isAutoReplyOn()){
			ui.showConfirmationDialog("toggleAutoReplyOn", this, PaymentPluginConstants.AUTO_REPLY_CONFIRMATION);
		}else{
			toggleAutoReplyOn();
		}
	}

	public void toggleAutoReplyOn() {
		boolean was_selected = autoReplyProperties.isAutoReplyOn(); 
		autoReplyProperties.toggleAutoReply();
		boolean autoReplyOn = autoReplyProperties.isAutoReplyOn();
		ui.setIcon(status_label, autoReplyOn ? ICON_STATUS_TRUE : ICON_STATUS_FALSE);
		ui.setText(status_label, (autoReplyOn ? DISABLE_AUTOREPLY : ENABLE_AUTOREPLY));
		ui.setSelected(status_label, autoReplyOn);
		
		ui.removeDialog(ui.find(CONFIRM_DIALOG));
	}

	protected String getXMLFile() {
		return XML_INCOMING_PAYMENTS_TAB;
	}

	public Object getRow(IncomingPayment incomingPayment) {
		Object row = ui.createTableRow(incomingPayment);
		
		ui.add(row, ui.createTableCell(incomingPayment.getConfirmationCode()));
		ui.add(row, ui.createTableCell(clientDao.getClientByPhoneNumber(incomingPayment.getPhoneNumber()).getFullName()));
		ui.add(row, ui.createTableCell(incomingPayment.getPhoneNumber()));
		ui.add(row, ui.createTableCell(incomingPayment.getAmountPaid().toPlainString()));
		ui.add(row, ui.createTableCell(InternationalisationUtils.getDatetimeFormat().format(new Date(incomingPayment.getTimePaid()))));
		ui.add(row, ui.createTableCell(incomingPayment.getPaymentId()));
		ui.add(row, ui.createTableCell(incomingPayment.getNotes()));
		return row;
	}

	@Override
	public void refresh() {
		this.updateIncomingPaymentsList();
	}

	public void editIncoming() {
		Object[] selectedIncomings = this.ui.getSelectedItems(incomingPaymentsTableComponent);
		for (Object selectedIncoming : selectedIncomings) {
			IncomingPayment ip = (IncomingPayment) ui.getAttachedObject(selectedIncoming);
			ui.add(new EditIncomingPaymentDialogHandler(ui,pluginController,ip).getDialog());
		}
	}

	
//>PAGING METHODS
	protected PagedListDetails getIncomingPaymentsListDetails(int startIndex,
			int limit) {
		List<IncomingPayment> incomingPayments = new ArrayList<IncomingPayment>();
		incomingPayments = getIncomingPaymentsForUI(startIndex, limit);
		Object[] listItems = toThinletComponents(incomingPayments);

		return new PagedListDetails(totalItemCount, listItems);
	}

	protected List<IncomingPayment> getIncomingPaymentsForUI(int startIndex, int limit) {
		//Just a hack...
		toggleAutoReplyOn();
		
		List<IncomingPayment> incomingPayments;
		String strStartDate = ui.getText(fldStartDate);
		String strEndDate = ui.getText(fldEndDate);
		
		if (!strStartDate.isEmpty()){
			try {
				startDate = InternationalisationUtils.getDateFormat().parse(strStartDate);
			} catch (ParseException e) {
				ui.infoMessage(INVALID_DATE);
				return Collections.emptyList();
			}
		}

		if (!strEndDate.isEmpty()){
			try {
				endDate = InternationalisationUtils.getDateFormat().parse(strEndDate);
			} catch (ParseException e) {
				ui.infoMessage("Please enter a correct ending date.");
				return Collections.emptyList();
			}
		}

			
		if (strStartDate.isEmpty() && strEndDate.isEmpty()) {
			totalItemCount = this.incomingPaymentDao.getActiveIncomingPayments().size();
			incomingPayments = this.incomingPaymentDao.getActiveIncomingPayments(startIndex, limit);
		} else {
			if (strStartDate.isEmpty() && endDate != null){
				totalItemCount = this.incomingPaymentDao.getIncomingPaymentsByEndDate(endDate).size();
				incomingPayments = this.incomingPaymentDao.getIncomingPaymentsByEndDate(endDate, startIndex, limit);
				
			} else {
				if (strEndDate.isEmpty() && startDate != null){
					totalItemCount = this.incomingPaymentDao.getIncomingPaymentsByStartDate(startDate).size();
					incomingPayments = this.incomingPaymentDao.getIncomingPaymentsByStartDate(startDate, startIndex, limit);
				} else {
					totalItemCount = this.incomingPaymentDao.getIncomingPaymentsByDateRange(startDate, endDate).size();
					incomingPayments = this.incomingPaymentDao.getIncomingPaymentsByDateRange(startDate, endDate, startIndex, limit);
				}
			}
		}
		return incomingPayments;

	}

	public PagedListDetails getListDetails(Object list, int startIndex,
			int limit) {
		if (list == this.incomingPaymentsTableComponent) {
			return getIncomingPaymentsListDetails(startIndex, limit);
		} else {
			throw new IllegalStateException();
		}
	}

	private Object[] toThinletComponents(List<IncomingPayment> incomingPayments) {
		Object[] components = new Object[incomingPayments.size()];
		for (int i = 0; i < components.length; i++) {
			IncomingPayment in = incomingPayments.get(i);
			components[i] = getRow(in);
		}
		return components;
	}

	public void updateIncomingPaymentsList() {
		this.incomingPaymentsTablePager.setCurrentPage(0);
		this.incomingPaymentsTablePager.refresh();
	}
	
//> INCOMING PAYMENT DELETION
	public void deleteIncomingPayment() {
		Object[] selectedIncomings = this.ui.getSelectedItems(incomingPaymentsTableComponent);
		if (selectedIncomings.length == 0){
			ui.infoMessage("Please select incoming payment(s).");	
		} else {
			for (Object selectedIncoming : selectedIncomings) {
				IncomingPayment attachedIncoming = ui.getAttachedObject(selectedIncoming, IncomingPayment.class);
				attachedIncoming.setActive(false);
				incomingPaymentDao.updateIncomingPayment(attachedIncoming);
				logMessageDao.saveLogMessage(
						new LogMessage(LogMessage.LogLevel.INFO,
							   	"Delete Incoming Payment",
							   	attachedIncoming.toStringForLogs()));
			}
			ui.infoMessage("You have successfully deleted the selected incoming payment(s).");	
		}		
	}
	
	// > EXPORTS...
	public void exportIncomingPayments() {
		Object[] selectedItems = ui.getSelectedItems(incomingPaymentsTableComponent);
		if (selectedItems.length <= 0){
			exportIncomingPayments(getIncomingPaymentsForExport());
		}else{
			List<IncomingPayment> incomingPayments = new ArrayList<IncomingPayment>(selectedItems.length);
			for (Object o : selectedItems) {
				incomingPayments.add(ui.getAttachedObject(o, IncomingPayment.class));
			}
			exportIncomingPayments(incomingPayments);
		}
	}

	protected List<IncomingPayment> getIncomingPaymentsForExport() {
		List<IncomingPayment> incomingPayments;
		String strStartDate = ui.getText(fldStartDate);
		String strEndDate = ui.getText(fldEndDate);
		try {
			startDate = InternationalisationUtils.getDateFormat().parse(strStartDate);
		} catch (ParseException e) {
		}
		try {
			endDate = InternationalisationUtils.getDateFormat().parse(strEndDate);
		} catch (ParseException e) {
		}
			
		if (strStartDate.isEmpty() && strEndDate.isEmpty()) {
			incomingPayments = this.incomingPaymentDao.getActiveIncomingPayments();
		} else {
			if (strStartDate.isEmpty()){
				incomingPayments = this.incomingPaymentDao.getIncomingPaymentsByEndDate(endDate);
				
			} else {
				if (strEndDate.isEmpty()){
					incomingPayments = this.incomingPaymentDao.getIncomingPaymentsByStartDate(startDate);
				} else {
					incomingPayments = this.incomingPaymentDao.getIncomingPaymentsByDateRange(startDate, endDate);
				}
			}
		}
		return incomingPayments;
	}
	
	public void exportIncomingPayments(List<IncomingPayment> incomingPayments) {
		new IncomingPaymentsExportHandler(ui, pluginController, incomingPayments).showWizard();
		this.refresh();
	}
	
	public void showAuthCode() {
		ui.remove(dialogConfirmation);
		new AuthorisationCodeHandler(ui).showAuthorizationCodeDialog("deleteIncomingPayment", this);
	}
	
	public final void showDeleteConfirmationDialog(String methodToBeCalled){
		dialogConfirmation = this.ui.showConfirmationDialog(methodToBeCalled, this, CONFIRM_DELETE_INCOMING);
	}
	
	public final void showAutoReplyDialog(){
		ui.add(new AutoReplyPaymentsDialogHandler(ui, pluginController).getDialog());
	}
	
	public void showDateSelecter(Object textField) {
		((UiGeneratorController) ui).showDateSelecter(textField);
	}
	
//> INCOMING PAYMENT NOTIFICATION...
	@SuppressWarnings("rawtypes")
	public void notify(final FrontlineEventNotification notification) {
		new FrontlineUiUpateJob() {
			public void run() {
				if (!(notification instanceof DatabaseEntityNotification)) {
					return;
				}
		
				Object entity = ((DatabaseEntityNotification) notification).getDatabaseEntity();
				if (entity instanceof IncomingPayment) {
					IncomingPaymentsTabHandler.this.refresh();
					if(autoReplyProperties.isAutoReplyOn()){
						IncomingPaymentsTabHandler.this.replyToPayment((IncomingPayment) entity);
					}
				}
			}
		}.execute();
	}

	protected void replyToPayment(IncomingPayment incomingPayment) {
		String message = replaceFormats(incomingPayment, autoReplyProperties.getMessage());
		frontlineController.sendTextMessage(incomingPayment.getPhoneNumber(), message);
	}
	
	Account getAccount(String phoneNumber) {
		Client client = clientDao.getClientByPhoneNumber(phoneNumber);
		if (client != null) {
			List<Account> activeNonGenericAccountsByClientId = accountDao.getActiveNonGenericAccountsByClientId(client.getId());
			if(!activeNonGenericAccountsByClientId.isEmpty()){
				return activeNonGenericAccountsByClientId.get(0);
			} else {
				return accountDao.getGenericAccountsByClientId(client.getId());
			}
		}
		return null;
	}
	
	private String replaceFormats(IncomingPayment incomingPayment, String message) {
		FormatterMarkerType[] formatEnums = FormatterMarkerType.values();
		final Target tgt = targetDao.getActiveTargetByAccount(
			getAccount(incomingPayment.getPhoneNumber()).getAccountNumber()
		);
		
		for (FormatterMarkerType fe : formatEnums) {
			if(message.contains(fe.getMarker())){
				switch (fe) {
			      case CLIENT_NAME:
			    	message.replace(fe.getMarker(), incomingPayment.getPaymentBy());
			        break;
			      case AMOUNT_PAID:
			    	message.replace(fe.getMarker(), incomingPayment.getAmountPaid().toString());
			        break;
			      case AMOUNT_REMAINING:
			    	message.replace(fe.getMarker(), targetAnalytics.getLastAmountPaid(tgt.getId()).toString());
			        break;
			      case DATE_PAID:
			    	  //message.replace(fe.getMarker(), PvUtils.formatDate(targetAnalytics.getDateLastPaid(tgt.getId())));
			        break;
			      case DAYS_REMAINING:
			    	  message.replace(fe.getMarker(), targetAnalytics.getDaysRemaining(tgt.getId()).toString());
			        break;
			      case MONTHLY_DUE:
			    	message.replace(fe.getMarker(), targetAnalytics.getMonthlyAmountDue().toString());
			        break;
			      case END_MONTH_INTERVAL:
			    	message.replace(fe.getMarker(), PvUtils.formatDate(targetAnalytics.getEndMonthInterval()));
			        break;
			      case MONTHLY_SAVINGS:
			    	  message.replace(fe.getMarker(), targetAnalytics.getMonthlyTarget().toString());
			        break;
			      case RECEPIENT_NAME:
			    	message.replace(fe.getMarker(), incomingPayment.getPaymentBy());
			        break;
			      case TARGET_ENDDATE:
			    	  message.replace(fe.getMarker(), PvUtils.formatDate(targetAnalytics.getEndMonthInterval()));
			        break;
			    }
			}
		}
		return message;
	}
}
