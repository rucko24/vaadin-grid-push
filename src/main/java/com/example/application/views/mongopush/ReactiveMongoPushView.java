package com.example.application.views.mongopush;

import com.example.application.broadcaster.Broadcaster;
import com.example.application.reactivedatabase.model.Book;
import com.example.application.reactivedatabase.service.ReactiveBookService;
import com.example.application.task.RefreshReactiveDataTask;
import com.example.application.views.AbstractViewPush;
import com.example.application.views.main.MainView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@UIScope
@Log4j2
@Route(value = "mongo-push", layout = MainView.class)
@PageTitle("Reactive mongo push")
@CssImport("./views/vaadinflow14withgridpush/vaadin-flow14withgridpush-view.css")
public class ReactiveMongoPushView extends AbstractViewPush<Book> {

    private final Grid<Book> reactiveBookGrid = new Grid<>();
    private final Button buttonStart = new Button("refresh!", VaadinIcon.REFRESH.create());
    private final Button buttonStop = new Button("Stop refresh!", VaadinIcon.STOP.create());
    private final Label labelGridCaption = new Label("Documents: ");
    private Registration registration;

    private RefreshReactiveDataTask refreshReactiveDataTask;

    private ReactiveBookService reactiveBookService;

    @Autowired
    public ReactiveMongoPushView(final ReactiveBookService reactiveBookService
            , final RefreshReactiveDataTask refreshReactiveDataTask) {
        this.reactiveBookService = reactiveBookService;
        this.refreshReactiveDataTask = refreshReactiveDataTask;

        this.initGrid();
    }

    private void initGrid() {
        reactiveBookGrid.addColumn(Book::getId).setHeader("id");
        reactiveBookGrid.addColumn(Book::getTitle).setHeader("Title");
        reactiveBookGrid.addColumn(Book::getAuthor).setHeader("Author");
        super.initComponents(reactiveBookGrid, labelGridCaption, buttonStart, buttonStop);
    }

    /**
     * Lazy loading with DataProvider.fromCallBack
     *
     * @param ui
     */
    private void initData(final UI ui) {
        Flux.defer(() -> reactiveBookService.findAll())
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(listBooks -> {
                    ui.access(() -> {
                        labelGridCaption.setText("Documents: " + listBooks.size());
                        reactiveBookGrid.setDataProvider(DataProvider.fromCallbacks(
                                query -> {
                                    int offset = query.getOffset();
                                    return listBooks.subList(offset, query.getOffset() + query.getLimit()).stream();
                                }, query -> listBooks.size()));
                    });
                });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        registration.remove();
        registration = null;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        //required for show time and memmory consumption
        super.onAttach(attachEvent);
        if (attachEvent.isInitialAttach()) {
            this.initData(attachEvent.getUI());
            this.buttonStart.addClickListener(e -> {
                refreshReactiveDataTask.initUpdateGrid("Init refresh items from database");
            });
            this.buttonStop.addClickListener(e -> {
                refreshReactiveDataTask.stopUpdateGrid("Stop refresh items from database");
            });

            this.registration = Broadcaster.registerReactiveBooks(booksList -> {
                attachEvent.getUI().access(() -> {
                    labelGridCaption.setText("Documents: " + booksList.size());
                    reactiveBookGrid.setItems(booksList);
                });
            }, message -> {
                attachEvent.getUI().access(() -> {
                    Notification.show(message);
                });
            });

        }
    }

}
