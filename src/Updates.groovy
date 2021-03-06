#! groovy
import groovy.transform.Canonical

@Canonical
class Updates implements Serializable {
    @Canonical
    static class Update implements Serializable {
        String name, from, to

        @Override
        String toString() { "$name: $from -> $to" }
    }

    List<Update> updates = []
    String currentVersion
    String updateVersion


    public boolean isEmpty() { updates.isEmpty() }

    @Override
    String toString() { "Updates in ${currentVersion}: ${updates.toString()} => ${updateVersion}" }
}
