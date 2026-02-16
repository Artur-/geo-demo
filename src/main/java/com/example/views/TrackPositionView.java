package com.example.views;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.geolocation.Geolocation;
import com.vaadin.flow.component.geolocation.GeolocationCoordinates;
import com.vaadin.flow.component.geolocation.GeolocationError;
import com.vaadin.flow.component.geolocation.GeolocationOptions;
import com.vaadin.flow.component.geolocation.GeolocationPosition;
import com.vaadin.flow.component.geolocation.GeolocationState;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.ComponentEffect;

/**
 * Demonstrates continuous position tracking using
 * {@link Geolocation#track(com.vaadin.flow.component.Component)} with reactive
 * a reactive {@link GeolocationState} signal. The map follows the user's
 * position in real time. Tracking stops automatically when navigating away.
 */
@Route(value = "track", layout = MainLayout.class)
@PageTitle("Track Position")
public class TrackPositionView extends VerticalLayout {

    private int updateCount = 0;
    private MarkerFeature marker;

    public TrackPositionView() {
        H2 header = new H2("Track Position");
        Paragraph description = new Paragraph(
                "This view uses Geolocation.track() with reactive Signals. "
                        + "Position updates appear automatically on the map. "
                        + "Tracking stops when you navigate away.");

        // Status badge
        Span statusBadge = new Span("Waiting for position...");
        statusBadge.getElement().getThemeList().add("badge");

        // Coordinate display fields
        Span latField = new Span("--");
        Span lonField = new Span("--");
        Span accField = new Span("--");
        Span altField = new Span("--");
        Span headField = new Span("--");
        Span speedField = new Span("--");
        Span countField = new Span("0");

        FormLayout coords = new FormLayout();
        coords.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 2),
                new FormLayout.ResponsiveStep("500px", 4));
        coords.addFormItem(latField, "Latitude");
        coords.addFormItem(lonField, "Longitude");
        coords.addFormItem(accField, "Accuracy");
        coords.addFormItem(altField, "Altitude");
        coords.addFormItem(headField, "Heading");
        coords.addFormItem(speedField, "Speed");
        coords.addFormItem(countField, "Updates");

        // Map
        Map map = new Map();
        map.setHeight("400px");
        map.setWidthFull();
        map.setZoom(2);

        // Error display
        Div errorDisplay = new Div();
        errorDisplay.setVisible(false);
        errorDisplay.getStyle()
                .set("color", "var(--lumo-error-text-color)")
                .set("padding", "var(--lumo-space-m)")
                .set("background", "var(--lumo-error-color-10pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        add(header, description, statusBadge, map, coords, errorDisplay);
        setPadding(true);

        // #geolocation
        GeolocationOptions options = new GeolocationOptions(true, null, null);
        Geolocation geo = Geolocation.track(this, options);

        // Reactive effect: runs whenever geo.state() changes
        ComponentEffect.effect(this, () -> {
            switch (geo.state().get()) {
                case GeolocationState.Pending pending -> {
                    // Still waiting for the first position
                }
                case GeolocationPosition pos -> {
                    GeolocationCoordinates c = pos.coords();
                    updateCount++;

                    latField.setText(
                            String.format("%.6f\u00B0", c.latitude()));
                    lonField.setText(
                            String.format("%.6f\u00B0", c.longitude()));
                    accField.setText(
                            String.format("%.1f m", c.accuracy()));
                    altField.setText(c.altitude() != null
                            ? String.format("%.1f m", c.altitude())
                            : "N/A");
                    headField.setText(c.heading() != null
                            ? String.format("%.1f\u00B0", c.heading())
                            : "N/A");
                    speedField.setText(c.speed() != null
                            ? String.format("%.2f m/s", c.speed())
                            : "N/A");
                    countField.setText(String.valueOf(updateCount));

                    // Update map
                    Coordinate coord = new Coordinate(
                            c.longitude(), c.latitude());
                    if (marker != null) {
                        map.getFeatureLayer().removeFeature(marker);
                    }
                    marker = new MarkerFeature(coord);
                    marker.setText("You are here");
                    map.getFeatureLayer().addFeature(marker);
                    map.setCenter(coord);
                    if (updateCount == 1) {
                        map.setZoom(15);
                    }

                    statusBadge.setText("Tracking active (" + updateCount
                            + " updates)");
                    statusBadge.getElement().getThemeList().clear();
                    statusBadge.getElement().getThemeList().add("badge");
                    statusBadge.getElement().getThemeList().add("success");

                    errorDisplay.setVisible(false);
                }
                case GeolocationError err -> {
                    errorDisplay.setVisible(true);
                    errorDisplay.removeAll();
                    errorDisplay.add(new Span(
                            errorCodeToString(err.code()) + ": "
                                    + err.message()));
                    statusBadge.setText("Error");
                    statusBadge.getElement().getThemeList().clear();
                    statusBadge.getElement().getThemeList().add("badge");
                    statusBadge.getElement().getThemeList().add("error");
                }
            }
        });
    }

    private String errorCodeToString(int code) {
        return switch (code) {
            case GeolocationError.PERMISSION_DENIED -> "Permission Denied";
            case GeolocationError.POSITION_UNAVAILABLE ->
                "Position Unavailable";
            case GeolocationError.TIMEOUT -> "Timeout";
            default -> "Unknown Error (" + code + ")";
        };
    }
}
