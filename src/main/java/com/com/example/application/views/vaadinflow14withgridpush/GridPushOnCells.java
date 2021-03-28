package com.com.example.application.views.vaadinflow14withgridpush;

import com.com.example.application.broadcaster.Broadcaster;
import com.com.example.application.data.Transaction;
import com.com.example.application.data.TransactionRepository;
import com.com.example.application.task.RefreshDataTask;
import com.com.example.application.utils.Hour;
import com.com.example.application.utils.MemoryConsumtion;
import com.com.example.application.views.main.MainView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UIScope
@Log4j2
@Route(value = "hello", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Grid push")
@CssImport("./views/vaadinflow14withgridpush/vaadin-flow14withgridpush-view.css")
public class GridPushOnCells extends HorizontalLayout {

    private final Grid<Transaction> grid = new Grid<>();
    private final VerticalLayout verticalLayout = new VerticalLayout();
    private ListDataProvider<Transaction> transactionListDataProvider;
    private List<Transaction> transactionList;
    private Registration broadcasterRegistration;
    private final Label labelHour = new Label("Current server time: ");
    private final Label labelMemory = new Label();
    private final Label preciousStones = new Label("Precious stones: ");
    private final Button buttonStart = new Button("Init transactions!");
    private final Button buttonStop = new Button("Stop transactions!");

    private Hour hour;
    private RefreshDataTask refreshDataTask;
    private MemoryConsumtion memoryConsumtion;

    @Autowired
    public GridPushOnCells(final Hour hour,
                           final RefreshDataTask refreshDataTask,
                           final MemoryConsumtion memoryConsumtion) {
        this.hour = hour;
        this.refreshDataTask = refreshDataTask;
        this.memoryConsumtion = memoryConsumtion;
        this.initComponents();
        this.iniData();

    }

    private void initComponents() {
        grid.setHeightByRows(true);
        grid.setWidthFull();
        grid.setMaxHeight("500px");
        grid.addColumn(Transaction::getName).setHeader("Name");
        grid.addColumn(Transaction::getAmount).setHeader("Amount");
        grid.addColumn(Transaction::formattedUpdateTime).setHeader("Updated");

        final HorizontalLayout rowButtons = new HorizontalLayout(buttonStart,buttonStop);
        rowButtons.setAlignItems(Alignment.END);
        rowButtons.setJustifyContentMode(JustifyContentMode.END);
        final HorizontalLayout header = new HorizontalLayout(labelHour,labelMemory, rowButtons);
        header.setWidthFull();
        buttonStop.addThemeVariants(ButtonVariant.LUMO_ERROR);
        header.setAlignItems(Alignment.CENTER);
        header.setFlexGrow(1,rowButtons);

        verticalLayout.add(header, preciousStones, grid);
        super.add(verticalLayout);
    }

    private void iniData() {
        transactionList = new ArrayList<>(TransactionRepository.getInstance().findAll());
        transactionListDataProvider = new ListDataProvider<>(transactionList);
        grid.setDataProvider(transactionListDataProvider);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getUI().ifPresent(ui -> hour.initHour(ui,labelHour));
        getUI().ifPresent(ui -> memoryConsumtion.showMemory(ui,labelMemory));

        if (attachEvent.isInitialAttach()) {
            buttonStart.addClickListener(e -> {
                Notification.show("Init transactions!");
                refreshDataTask.initUpdateGrid();
            });
            buttonStop.addClickListener(e -> {
                Notification.show("Stop transactions!");
                refreshDataTask.stopUpdateGrid();
            });
            broadcasterRegistration = Broadcaster.register(updatedTransactionIds -> {
                getUI().ifPresent(ui -> {
                    ui.access(() -> {
                        updatedTransactionIds.forEach(updateId -> {
                            final Transaction transaction = TransactionRepository.getInstance().find(updateId);
                            if (!transactionList.contains(transaction)) {
                                transactionList.add(transaction);
                                grid.getDataProvider().refreshAll();
                                log.info("Update caption " + transactionList.size());
                                preciousStones.setText("Precious stones: " + transactionList.size());
                            } else {
                                transactionListDataProvider.refreshItem(transaction);
                            }
                        });
                    });
                });
            });
        }
    }

}
