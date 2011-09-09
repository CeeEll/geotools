/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.swing.action;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import org.geotools.swing.MapPane;

/**
 *
 * @author michael
 */
public class NoToolAction extends MapAction {
    /** Name for this tool */
    public static final String TOOL_NAME = 
            ResourceBundle.getBundle("org/geotools/swing/Text").getString("tool_name_reset");
    
    /** Tool tip text */
    public static final String TOOL_TIP = 
            ResourceBundle.getBundle("org/geotools/swing/Text").getString("tool_tip_reset");
    
    /** Icon for the control */
    public static final String ICON_IMAGE = "/org/geotools/swing/icons/pointer.png";
    
    /**
     * Constructor. The associated control will be labelled with an icon.
     * 
     * @param mapPane the map pane being serviced by this action
     */
    public NoToolAction(MapPane mapPane) {
        this(mapPane, false);
    }

    /**
     * Constructor. The associated control will be labelled with an icon and,
     * optionally, the tool name.
     * 
     * @param mapPane the map pane being serviced by this action
     * @param showToolName set to true for the control to display the tool name
     */
    public NoToolAction(MapPane mapPane, boolean showToolName) {
        String toolName = showToolName ? TOOL_NAME : null;
        
        String iconImagePath = null;
        super.init(mapPane, toolName, TOOL_TIP, ICON_IMAGE);
    }
    
    /**
     * Called when the control is activated. Calls the map pane to reset the 
     * display 
     *
     * @param ev the event (not used)
     */
    @Override
    public void actionPerformed(ActionEvent ev) {
        getMapPane().setCursorTool(null);
    }

}