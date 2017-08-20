package com.minecraftlegend.inventoryapi.EventListeners;


import com.minecraftlegend.inventoryapi.Events.*;
import com.minecraftlegend.inventoryapi.GUIComponent;
import com.minecraftlegend.inventoryapi.GUIContainer;
import com.minecraftlegend.inventoryapi.GUIElement;
import com.minecraftlegend.inventoryapi.GUIEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Author Sauerbier | Jan
 * @Copyright 2016 by Jan Hof
 * All rights reserved.
 **/
public class McGuiListener implements Listener {

    private GUIContainer gui;
    private ArrayList<GUIEvent> globalEvents = new ArrayList<>();

    public McGuiListener(GUIContainer gui) {
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if(gui == null || gui.getPlayer() == null || !event.getWhoClicked().getUniqueId().equals(gui.getPlayer().getUniqueId())) return;

        globalEvents.forEach(e -> e.onClick(new ComponentClickEvent(gui, null, event.getCurrentItem(), event.getCursor(), event.getClickedInventory(), event.getSlot(), event.getRawSlot(), event.getWhoClicked(), event.getClick())));

        if (event.getInventory().equals(gui.getInventory())) {
            event.setCancelled(gui.isLocked());

            for(Iterator<GUIComponent> iterator = gui.getComponents().iterator(); iterator.hasNext(); ){
                GUIComponent guiComponent = iterator.next();
                if(guiComponent instanceof GUIElement){
                    if(((GUIElement) guiComponent).getPosition().toInventoryPosition() == event.getRawSlot()){
                        event.setCancelled(guiComponent.isLocked());
                        guiComponent.getEvents().forEach(e -> e.onClick(new ComponentClickEvent(gui, guiComponent, event.getCurrentItem(), event.getCursor(), event.getClickedInventory(), event.getSlot(),event.getRawSlot(), event.getWhoClicked(), event.getClick())));
                        //don't return because there could be multiple elements at the same slot e.g. GUIButton (includes GUILabel)g
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if(gui == null || gui.getPlayer() == null || !event.getWhoClicked().getUniqueId().equals(gui.getPlayer().getUniqueId())) return;

        globalEvents.forEach(e -> e.onDrag(new ComponentDragEvent(gui, null, event.getNewItems(), event.getWhoClicked(), event.getType())));

        if (event.getInventory().equals(gui.getInventory())) {
            event.setCancelled(gui.isLocked());
            for (GUIComponent guiComponent : gui.getComponents()) {
                if (guiComponent instanceof GUIElement) {
                    if (event.getRawSlots().contains(((GUIElement) guiComponent).getPosition().toInventoryPosition())) {
                        event.setCancelled(guiComponent.isLocked());
                        guiComponent.getEvents().forEach(e -> e.onDrag(new ComponentDragEvent(gui, guiComponent, event.getNewItems(), event.getWhoClicked(), event.getType())));
                        //don't return because there could be multiple elements at the same slot e.g. GUIButton (includes GUILabel)
                    }
                }
            }
        }
    }


    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event) {
        globalEvents.forEach(e -> e.onMove(new ComponentMoveEvent(gui, event.getItem())));

        if (event.getDestination().equals(gui.getInventory())) {
            event.setCancelled(gui.isLocked());
            for (GUIComponent guiComponent : gui.getComponents()) {
                if (guiComponent instanceof GUIElement) {
                    event.setCancelled(guiComponent.isLocked());
                    guiComponent.getEvents().forEach(e -> e.onMove(new ComponentMoveEvent(gui, event.getItem())));
                    return;
                }
            }
        }
    }


    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if(gui == null || gui.getPlayer() == null || !event.getPlayer().getUniqueId().equals(gui.getPlayer().getUniqueId())) return;

        globalEvents.forEach(e -> e.onOpen(new ContainerOpenEvent(gui, event.getPlayer())));
        if (event.getInventory().equals(gui.getInventory()))
            gui.getEvents().forEach(e -> e.onOpen(new ContainerOpenEvent(gui, event.getPlayer())));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if(gui == null || gui.getPlayer() == null || !event.getPlayer().getUniqueId().equals(gui.getPlayer().getUniqueId())) return;

        globalEvents.forEach(e -> e.onClose(new ContainerCloseEvent(gui, event.getPlayer())));
        if (event.getInventory().equals(gui.getInventory())) {
            gui.getEvents().forEach(e -> e.onClose(new ContainerCloseEvent(gui, event.getPlayer())));

            HandlerList.unregisterAll(this);
            gui.setNativeListenerRegistered(false);
        }
    }

    public void addGlobalHook(GUIEvent event) {
        globalEvents.add(event);
    }

    public void setGlobalHooks(ArrayList<GUIEvent> hooks) {
        this.globalEvents = hooks;
    }

    public void removeGlobalHook(GUIEvent event) {
        globalEvents.remove(event);
    }

    public List<GUIEvent> getGlobalHooks() {
        return globalEvents;
    }

}
