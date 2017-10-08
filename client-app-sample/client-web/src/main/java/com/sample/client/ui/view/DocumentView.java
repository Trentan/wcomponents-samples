package com.sample.client.ui.view;

import com.github.bordertech.wcomponents.Action;
import com.github.bordertech.wcomponents.ActionEvent;
import com.github.bordertech.wcomponents.MessageContainer;
import com.github.bordertech.wcomponents.RenderContext;
import com.github.bordertech.wcomponents.Request;
import com.github.bordertech.wcomponents.SimpleBeanBoundTableModel;
import com.github.bordertech.wcomponents.WButton;
import com.github.bordertech.wcomponents.WDateField;
import com.github.bordertech.wcomponents.WLink;
import com.github.bordertech.wcomponents.WMenu;
import com.github.bordertech.wcomponents.WMenuItem;
import com.github.bordertech.wcomponents.WMessages;
import com.github.bordertech.wcomponents.WPanel;
import com.github.bordertech.wcomponents.WPopup;
import com.github.bordertech.wcomponents.WSection;
import com.github.bordertech.wcomponents.WTable;
import com.github.bordertech.wcomponents.WTableColumn;
import com.github.bordertech.wcomponents.WText;
import com.sample.client.model.DocumentDetail;
import com.sample.client.services.ClientServicesHelper;
import com.sample.client.ui.application.ClientApp;
import com.sample.client.ui.common.ClientWMessages;
import com.sample.client.ui.common.Constants;
import com.sample.client.ui.util.ClientServicesHelperFactory;
import java.util.List;
import java.util.Set;

/**
 * Document view.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public class DocumentView extends WSection implements MessageContainer {

	private static final ClientServicesHelper CLIENT_SERVICES = ClientServicesHelperFactory.getInstance();

	private final ClientApp app;

	private final WMessages messages = new ClientWMessages();

	private final WMenu menu = new WMenu();

	private final WPanel ajaxPanel = new WPanel() {
		@Override
		protected void afterPaint(final RenderContext renderContext) {
			reset();
		}
	};

	private final WTable table = new WTable();

	/**
	 * @param app the client app.
	 */
	public DocumentView(final ClientApp app) {
		super("Documents");
		this.app = app;

		WPanel content = getContent();

		// Menu
		content.add(menu);
		setupMenu();

		// Messages
		content.add(messages);

		// Table
		content.add(table);
		table.setMargin(Constants.NORTH_MARGIN_LARGE);
		setupTable();

		// AJAX
		content.add(ajaxPanel);

		// Ids
		setIdName("docv");
		setNamingContext(true);

	}

	private void setupMenu() {
		WMenuItem item = new WMenuItem("Back");
		item.setAction(new Action() {
			@Override
			public void execute(final ActionEvent event) {
				app.showSearch();
			}
		});
		menu.add(item);

		item = new WMenuItem("Reset");
		item.setAction(new Action() {
			@Override
			public void execute(final ActionEvent event) {
				doReset();
			}
		});
		menu.add(item);

	}

	/**
	 * Setup the document table.
	 */
	private void setupTable() {
		table.setIdName("dtbl");
		table.setSearchAncestors(false);

		table.setMargin(Constants.SOUTH_MARGIN_LARGE);
		table.addColumn(new WTableColumn("ID", new WText()));
		table.addColumn(new WTableColumn("Description", new WText()));
		table.addColumn(new WTableColumn("Submitted", new WDateField()));
		table.addColumn(new WTableColumn("Link", new DocLink()));
		table.setStripingType(WTable.StripingType.ROWS);
		table.setNoDataMessage("No documents found.");
		table.setSelectAllMode(WTable.SelectAllType.CONTROL);
		table.setSelectMode(WTable.SelectMode.MULTIPLE);
		table.setSortMode(WTable.SortMode.DYNAMIC);

		SimpleBeanBoundTableModel model = new SimpleBeanBoundTableModel(new String[]{"documentId", "description", "submitDate", "."});
		model.setSelectable(true);
		model.setComparator(1, SimpleBeanBoundTableModel.COMPARABLE_COMPARATOR);
		model.setComparator(2, SimpleBeanBoundTableModel.COMPARABLE_COMPARATOR);
		table.setTableModel(model);

		// Select Documents
		WButton selButton = new WButton("View Selected Documents");
		selButton.setAction(new Action() {
			@Override
			public void execute(final ActionEvent event) {
				doHandleView();
			}
		});
		table.addAction(selButton);
		table.addActionConstraint(selButton, new WTable.ActionConstraint(1, 0, true, "Please select at least one document to view."));

		selButton.setAjaxTarget(ajaxPanel);
	}

	/**
	 * Refresh results. Remove results from the cache.
	 */
	public void doReset() {
		reset();
	}

	public void doHandleView() {
		ajaxPanel.reset();
		for (DocumentDetail selected : (Set<DocumentDetail>) table.getSelectedRows()) {
			WPopup popup = new WPopup(selected.getResourcePath());
			popup.setTargetWindow(selected.getDocumentId() + "-" + selected.getDescription());
			popup.setVisible(true);
			ajaxPanel.add(popup);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public WMessages getMessages() {
		return messages;
	}

	@Override
	protected void preparePaintComponent(final Request request) {
		super.preparePaintComponent(request);
		if (!isInitialised()) {
			try {
				List<DocumentDetail> docs = CLIENT_SERVICES.retrieveClientDocuments("dummyId");
				table.setBean(docs);
			} catch (Exception e) {
				messages.error("Could not load documents. " + e.getMessage());
			}
			setInitialised(true);
		}
	}

	public static class DocLink extends WLink {

		@Override
		protected void preparePaintComponent(final Request request) {
			super.preparePaintComponent(request);
			if (!isInitialised()) {
				DocumentDetail bean = (DocumentDetail) getBean();
				setText(bean.getResourcePath());
				setUrl(bean.getResourcePath());
				setTargetWindowName(bean.getDocumentId() + "-" + bean.getDescription());
				setInitialised(true);
			}
		}
	}

}
