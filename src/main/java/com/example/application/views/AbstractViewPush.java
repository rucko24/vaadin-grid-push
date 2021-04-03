package com.example.application.views;

import com.example.application.utils.Hour;
import com.example.application.utils.MemoryConsumtion;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
@SpringComponent
public abstract class AbstractViewPush<T> extends HorizontalLayout {

    private Grid<T> grid;
    private final VerticalLayout verticalLayout = new VerticalLayout();
    private final Label labelHour = new Label("Current server time: ");
    private final Label labelMemory = new Label();
    private Label labelCaptionGrid;
    private Button buttonStart = new Button("Init transactions!");
    private Button buttonStop = new Button("Stop transactions!");

    @Autowired
    private Hour hour;

    @Autowired
    private MemoryConsumtion memoryConsumtion;

    protected void initComponents(final Grid<T> grid, final Label labelCaptionGrid,
                                  Button buttonStart,Button buttonStop) {
        this.grid = grid;
        this.buttonStart = buttonStart;
        this.buttonStop = buttonStop;
        grid.setHeightByRows(true);
        grid.setWidthFull();
        grid.setMaxHeight("500px");
        this.labelCaptionGrid = labelCaptionGrid;
        grid.setHeightByRows(true);
        grid.setWidthFull();
        grid.setMaxHeight("500px");

        final HorizontalLayout rowButtons = new HorizontalLayout(buttonStart, buttonStop);
        rowButtons.setAlignItems(Alignment.END);
        rowButtons.setJustifyContentMode(JustifyContentMode.END);
        final HorizontalLayout header = new HorizontalLayout(labelHour, labelMemory, rowButtons);
        header.setWidthFull();
        buttonStop.addThemeVariants(ButtonVariant.LUMO_ERROR);
        header.setAlignItems(Alignment.CENTER);
        header.setFlexGrow(1, rowButtons);

        verticalLayout.add(header, labelCaptionGrid, grid);
        super.add(verticalLayout);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            getUI().ifPresent(ui -> hour.initHour(ui,labelHour));
            getUI().ifPresent(ui -> memoryConsumtion.showMemory(ui,labelMemory));
        }
    }
}
