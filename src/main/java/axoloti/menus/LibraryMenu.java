/**
 * Copyright (C) 2013 - 2016 Johannes Taelman
 *
 * This file is part of Axoloti.
 *
 * Axoloti is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Axoloti is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Axoloti. If not, see <http://www.gnu.org/licenses/>.
 */
package axoloti.menus;

import axoloti.Axoloti;
import static axoloti.MainFrame.prefs;
import static axoloti.menus.PopulatePatchMenu.PopulatePatchMenu;
import axoloti.utils.AxolotiLibrary;
import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

/**
 *
 * @author jtaelman
 */
public class LibraryMenu extends JMenu {

    public LibraryMenu() {

        addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                String dir = "";
                AxolotiLibrary lib = prefs.getLibrary(AxolotiLibrary.FACTORY_ID);
                if (lib != null) {
                    dir = lib.getLocalLocation();
                } else {
                    dir = "/";
                }
                
                JMenu ptut = new JMenu("tutorials");
                PopulatePatchMenu(ptut, dir + "patches/tutorials", ".axp");
                add(ptut);
                JMenu pdemos = new JMenu("demos");
                PopulatePatchMenu(pdemos, dir + "patches/demos", ".axp");
                add(pdemos);
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                removeAll();
            }

            @Override
            public void menuCanceled(MenuEvent e) {
                removeAll();
            }
        });
    }

}