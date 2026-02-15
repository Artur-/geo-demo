package com.example.views;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
        super.showRouterLayoutContent(content);

        String source = readSource(content);
        if (source == null) {
            return;
        }

        String fileName = content.getClass().getSimpleName() + ".java";

        Button sourceButton = new Button("View Source",
                e -> showSourceDialog(fileName, source));
        sourceButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE,
                ButtonVariant.LUMO_SMALL);
        sourceButton.getStyle()
                .set("position", "absolute")
                .set("right", "16px")
                .set("top", "16px")
                .set("z-index", "1");

        content.getElement().getStyle().set("position", "relative");
        content.getElement().appendChild(sourceButton.getElement());
    }

    private void showSourceDialog(String fileName, String source) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(fileName);
        dialog.setWidth("min(90vw, 800px)");
        dialog.setHeight("80vh");

        Pre pre = new Pre(source);
        pre.getStyle()
                .set("font-family", "monospace")
                .set("font-size", "var(--aura-font-size-s)")
                .set("background", "#f5f5f5")
                .set("padding", "16px")
                .set("border-radius", "var(--aura-base-radius, 4px)")
                .set("overflow", "auto")
                .set("margin", "0")
                .set("height", "100%")
                .set("box-sizing", "border-box");

        dialog.add(pre);
        dialog.setCloseOnEsc(true);
        Button closeButton = new Button(VaadinIcon.CLOSE_SMALL.create(),
                e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(closeButton);
        dialog.open();

        // Scroll to #geolocation marker
        int markerLine = findMarkerLine(source);
        if (markerLine > 0) {
            pre.getElement().executeJs(
                    "setTimeout(() => { const lh = parseFloat(getComputedStyle(this).lineHeight) || 16;"
                            + " this.scrollTop = ($0 - 3) * lh; }, 0)",
                    markerLine);
        }
    }

    private int findMarkerLine(String source) {
        int line = 0;
        for (String s : source.split("\n")) {
            if (s.contains("// #geolocation")) {
                return line;
            }
            line++;
        }
        return -1;
    }

    private String readSource(HasElement content) {
        Class<?> viewClass = content.getClass();
        String classFile = viewClass.getName().replace('.', '/') + ".java";

        // Try filesystem first (dev mode)
        try {
            return Files.readString(Path.of("src/main/java", classFile));
        } catch (IOException ignored) {
        }

        // Fall back to classpath (production)
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("sources/" + classFile)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
        }

        return null;
    }
}
