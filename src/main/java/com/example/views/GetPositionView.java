package com.example.views;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.geolocation.Geolocation;
import com.vaadin.flow.component.geolocation.GeolocationCoordinates;
import com.vaadin.flow.component.geolocation.GeolocationOptions;
import com.vaadin.flow.component.geolocation.GeolocationPosition;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Demonstrates one-shot position requests using
 * {@link Geolocation#get(GeolocationOptions)}.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Get Position")
public class GetPositionView extends VerticalLayout {

    private final Div resultArea = new Div();
    private final Map map = new Map();
    private MarkerFeature marker;

    public GetPositionView() {
        H2 header = new H2("Get Current Position");
        Paragraph description = new Paragraph(
                "Click the button to request your current position using "
                        + "Geolocation.get(). This makes a one-shot request "
                        + "that returns a CompletableFuture.");

        // Options
        Checkbox highAccuracy = new Checkbox("Enable high accuracy");
        IntegerField timeoutField = new IntegerField("Timeout (ms)");
        timeoutField.setPlaceholder("Browser default");
        timeoutField.setStepButtonsVisible(true);
        timeoutField.setStep(1000);
        timeoutField.setMin(0);
        IntegerField maxAgeField = new IntegerField("Maximum age (ms)");
        maxAgeField.setPlaceholder("Browser default");
        maxAgeField.setStepButtonsVisible(true);
        maxAgeField.setStep(1000);
        maxAgeField.setMin(0);

        FormLayout optionsForm = new FormLayout(highAccuracy, timeoutField,
                maxAgeField);
        optionsForm.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 3));

        // Button
        Button getButton = new Button("Get My Position",
                VaadinIcon.CROSSHAIRS.create());
        getButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Map
        map.setHeight("400px");
        map.setWidthFull();
        map.setZoom(2);

        // Result area
        resultArea.setText("Click the button to request your position.");
        resultArea.getStyle().set("padding", "var(--lumo-space-m)");

        getButton.addClickListener(e -> {
            resultArea.removeAll();
            resultArea.add(new Span("Requesting position..."));
            getButton.setEnabled(false);

            GeolocationOptions opts = new GeolocationOptions(
                    highAccuracy.getValue() ? true : null,
                    timeoutField.getValue(),
                    maxAgeField.getValue());

            Geolocation.get(opts).thenAccept(pos -> {
                resultArea.removeAll();
                resultArea.add(createPositionDetails(pos));
                updateMap(pos);
                getButton.setEnabled(true);
            }).exceptionally(ex -> {
                resultArea.removeAll();
                resultArea.add(createErrorDisplay(ex.getMessage()));
                getButton.setEnabled(true);
                return null;
            });
        });

        add(header, description, optionsForm, getButton, map, resultArea);
        setPadding(true);
    }

    private void updateMap(GeolocationPosition pos) {
        GeolocationCoordinates c = pos.coords();
        Coordinate coord = new Coordinate(c.longitude(), c.latitude());

        if (marker != null) {
            map.getFeatureLayer().removeFeature(marker);
        }
        marker = new MarkerFeature(coord);
        marker.setText("You are here");
        map.getFeatureLayer().addFeature(marker);
        map.setCenter(coord);
        map.setZoom(15);
    }

    private Component createPositionDetails(GeolocationPosition pos) {
        GeolocationCoordinates c = pos.coords();
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));

        form.addFormItem(
                new Span(String.format("%.6f\u00B0", c.latitude())),
                "Latitude");
        form.addFormItem(
                new Span(String.format("%.6f\u00B0", c.longitude())),
                "Longitude");
        form.addFormItem(
                new Span(String.format("%.1f m", c.accuracy())),
                "Accuracy");

        if (c.altitude() != null) {
            form.addFormItem(
                    new Span(String.format("%.1f m", c.altitude())),
                    "Altitude");
        }
        if (c.altitudeAccuracy() != null) {
            form.addFormItem(
                    new Span(String.format("%.1f m", c.altitudeAccuracy())),
                    "Altitude Accuracy");
        }
        if (c.heading() != null) {
            form.addFormItem(
                    new Span(String.format("%.1f\u00B0", c.heading())),
                    "Heading");
        }
        if (c.speed() != null) {
            form.addFormItem(
                    new Span(String.format("%.2f m/s", c.speed())),
                    "Speed");
        }

        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.ofEpochMilli(pos.timestamp()));
        form.addFormItem(new Span(time), "Timestamp");

        return form;
    }

    private Component createErrorDisplay(String message) {
        Div div = new Div();
        div.getStyle()
                .set("color", "var(--lumo-error-text-color)")
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-error-color-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");
        div.add(new Span("Error: " + message));
        return div;
    }
}
