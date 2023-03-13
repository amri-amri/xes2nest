package org.example;

import org.deckfour.xes.model.XEvent;

interface Filter {
    boolean filter(XEvent xEvent);
}
