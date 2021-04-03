package com.example.application.views.vaadinflow14withgridpush;

import com.example.application.broadcaster.Broadcaster;
import com.example.application.data.Transaction;
import com.example.application.data.TransactionRepository;
import com.example.application.task.RefreshDataTask;
import com.example.application.views.AbstractViewPush;
import com.example.application.views.main.MainView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.shared.Registration;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

//@UIScope
@Log4j2
@Route(value = "hello", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Grid push")
@CssImport("./views/vaadinflow14withgridpush/vaadin-flow14withgridpush-view.css")
public class GridPushOnRows extends AbstractViewPush<Transaction> {

    private final Grid<Transaction> grid = new Grid<>();
    private ListDataProvider<Transaction> transactionListDataProvider;
    private List<Transaction> transactionList;
    private Registration broadcasterRegistration;
    private final Label labelGridCaption = new Label("Precious stones: ");
    private final Button buttonStart = new Button("Init transactions!", VaadinIcon.REFRESH.create());
    private final Button buttonStop = new Button("Stop transactions!", VaadinIcon.STOP.create());

    private RefreshDataTask refreshDataTask;

    @Autowired
    public GridPushOnRows(final RefreshDataTask refreshDataTask) {
        this.refreshDataTask = refreshDataTask;
        this.initComponents();
        this.iniData();

    }

    private void initComponents() {
        grid.addColumn(Transaction::getName).setHeader("Name");
        grid.addColumn(Transaction::getAmount).setHeader("Amount");
        grid.addColumn(Transaction::formattedUpdateTime).setHeader("Updated");

        super.initComponents(grid, labelGridCaption, buttonStart, buttonStop);
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
        //super.onAttach(attachEvent);
        if (attachEvent.isInitialAttach()) {
            buttonStart.addClickListener(e -> {
                refreshDataTask.initUpdateGrid("Init transactions!");
            });
            buttonStop.addClickListener(e -> {
                refreshDataTask.stopUpdateGrid("Stop transactions!");
            });

            this.broadcasterRegistration = Broadcaster.register(updatedTransactionIds -> {
                updatedTransactionIds.forEach(transactionName -> {
                    getUI().ifPresent(ui -> {
                        ui.access(() -> {
                            final Transaction transaction = TransactionRepository.getInstance().find(transactionName);
                            if (!transactionList.contains(transaction)) {
                                transactionList.add(transaction);
                                grid.getDataProvider().refreshAll();
                                System.out.println("Update caption {} " + transactionList.size());
                                labelGridCaption.setText("Precious stones: " + transactionList.size());
                            } else {
                                transactionListDataProvider.refreshItem(transaction);
                            }
                        });
                    });
                });
            }, message -> { //Notifications for all UIs
                getUI().ifPresent(ui -> {
                    ui.access(() -> {
                        Notification.show(message);
                    });
                });
            });

        }
    }

}
