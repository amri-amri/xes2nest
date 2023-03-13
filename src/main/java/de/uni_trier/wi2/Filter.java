package de.uni_trier.wi2;

import org.deckfour.xes.model.XEvent;

interface Filter {
    boolean filter(XEvent xEvent);
}
