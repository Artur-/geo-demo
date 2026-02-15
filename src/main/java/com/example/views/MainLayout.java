package com.example.views;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainLayout extends AppLayout {

    public MainLayout() {
        DrawerToggle toggle = new DrawerToggle();
        H2 title = new H2("Geolocation Demo");
        title.getStyle().set("margin", "0").set("font-size", "1.2em");
        addToNavbar(toggle, title);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Get Position",
                GetPositionView.class, VaadinIcon.MAP_MARKER.create()));
        nav.addItem(new SideNavItem("Track Position",
                TrackPositionView.class, VaadinIcon.CROSSHAIRS.create()));
        addToDrawer(nav);
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        Details sourceDetails = createSourceDetails(content);
        if (sourceDetails == null) {
            super.showRouterLayoutContent(content);
            return;
        }

        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setWidth("100%");
        wrapper.getStyle().set("flex-grow", "1");
        wrapper.setPadding(false);
        var viewComponent = content.getElement().getComponent().orElseThrow();
        wrapper.addAndExpand(viewComponent);
        viewComponent.getElement().getStyle().remove("height");
        wrapper.add(sourceDetails);
        super.showRouterLayoutContent(wrapper);
    }

    private Details createSourceDetails(HasElement content) {
        Class<?> viewClass = content.getClass();
        String classFile = viewClass.getName().replace('.', '/') + ".java";
        Path sourcePath = Path.of("src/main/java", classFile);

        try {
            String source = Files.readString(sourcePath);
            Pre pre = new Pre(source);
            pre.getStyle()
                    .set("font-family", "var(--lumo-font-family-monospace, monospace)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("padding", "var(--lumo-space-m)")
                    .set("border-radius", "var(--lumo-border-radius-m)")
                    .set("overflow-x", "auto")
                    .set("margin", "0");

            Details details = new Details(
                    "View Source: " + viewClass.getSimpleName() + ".java",
                    pre);
            details.setOpened(false);
            details.getStyle().set("width", "100%");
            return details;
        } catch (IOException e) {
            return null;
        }
    }
}
