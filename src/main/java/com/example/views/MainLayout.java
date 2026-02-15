package com.example.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

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
}
