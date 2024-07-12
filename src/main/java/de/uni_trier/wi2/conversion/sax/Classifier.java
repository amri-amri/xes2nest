package de.uni_trier.wi2.conversion.sax;

/**
 * A record class to represent a classifier element in an XES log.
 *
 * @param name  The name of the classifier.
 * @param scope The scope of the classifier (trace or event).
 * @param keys  The array of keys.
 */
public record Classifier(String name, Scope scope, String[] keys) {
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Classifier other)) return false;
        if (!(other.name.equals(this.name) && other.scope.equals(this.scope))) return false;
        if (other.keys.length != this.keys.length) return false;
        for (int i = 0; i < this.keys.length; i++) {
            if (other.keys[i] != this.keys[i]) return false;
        }
        return true;
    }
}
