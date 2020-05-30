/*
 *  Copyright (c) NOAMO Tech - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Noah Moritz Hölterhoff <noah.hoelterhoff@gmail.com>, 20.5.2020
 */

package de.noamo.util;

import javax.swing.*;
import java.io.File;

/**
 * @author Noah Moritz Hölterhoff
 * @version 11.03.2020
 */
public abstract class Page extends JPanel {
    private JFrame context = null;
    private boolean externalApp = true;
    private Page last;
    private boolean maximized;
    private JMenuItem[] menuItems;
    private CloseListener closeListener;
    private File appRoot=null;

    public Page() {
        super();
        last = null;
        maximized = false;
        menuItems = new JMenuItem[0];
    }

    @SuppressWarnings("unused")
    public Page(Page pLast, JMenuItem[] pMenuItems, boolean pMaximized) {
        super();
        last = pLast;
        menuItems = pMenuItems;
        maximized = pMaximized;
    }

    /**
     * Fragt den JFrame ab, in dem die Page ausgeführt wird
     *
     * @return Der JFrame, in dem die Page aktuell läuft
     */
    public JFrame getContext() {
        return context;
    }

    public void requestClose(){
        if(closeListener!=null) closeListener.closeRequested();
    }

    public void setCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    /**
     * Setzt den Context (JFrame, in dem die App läuft). Dies sollte nur von dem Launcher selbst erledigt werden und nicht
     * von einzelden Pages
     *
     * @param frame Der neue JFrame der dieser Page zugeordnet werden soll
     */
    public void setContext(JFrame frame) {
        context = frame;
    }

    public Page getLast() {
        return last;
    }

    public File getAppRoot(){
        return appRoot;
    }

    public void setAppRoot(File appRoot) {
        this.appRoot = appRoot;
    }

    public void setLast(Page last) {
        this.last = last;
    }

    /**
     * Gibt die Liste an MenuItems zurück, die für diese Page vorhanden sein sollen
     *
     * @return Ein Array aus allen gewünschten MenuItems
     */
    public JMenuItem[] getMenuItems() {
        return menuItems;
    }

    /**
     * Fügt dem Menü dieser Seite ein Array aus Menüs hinzu. Diese Methode sollte für MenuBars verwendet werden, da
     * nur so sichergestellt werden kann, dass die Launcherfunktionen weiterhin funktionieren. Diese Methode zeigt nur
     * im Konstruktor (bzw. bei der Erstellung des Objektes) wirkung.
     *
     * @param menuItems Die MenuItems, die dder MenuBar hinzugefügrt werden sollen.
     */
    public void setMenuItems(JMenuItem[] menuItems) {
        this.menuItems = menuItems;
    }

    public boolean isExternalApp() {
        return externalApp;
    }

    public void setExternalApp(boolean externalApp) {
        this.externalApp = externalApp;
    }

    /**
     * Fragt ab, ob die Page aktuell im maximierten Modus ist
     *
     * @return true=maximiert; false=nicht maximiert
     */
    public boolean isMaximized() {
        return maximized;
    }

    /**
     * Bestimmt, ob die Page in dem JFrame maximiert werden soll, oder nicht. Diese Methode muss im Konstruktor
     * (bzw bei der Erstellung des Objektes) aufgerufen werden, da Sie im Nachinein nicht mehr wirksam ist.
     * Der Standart-Wert ist, dass die App nicht maximiert läuft
     *
     * @param maximized true=maximiert; false=nicht maximiert
     */
    public void setMaximized(boolean maximized) {
        this.maximized = maximized;
    }

    /**
     * Wird aufgerufen, wenn die Page geschlossen wird
     */
    public void kill() {
    }

    /**
     * Wird aufgerufen, wenn die Page geöffnet wird. Kann nützlich sein, wenn auf die Page zurück gekehrt wird
     */
    public void update() {
    }

    public interface CloseListener{
        void closeRequested();
    }
}
