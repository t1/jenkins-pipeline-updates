#! groovy

static Updates call(String mvnOut) {
    Updates updates = new Updates()
    String[] lines = mvnOut.split('\n')
    for (int i = 0; i < lines.length; i++) {
        def line = lines[i]
        scanForCurrentVersion(updates, line)
        scanForUpdate(updates, line)
    }
    calculateUpdateVersion(updates)

    return updates
}

private static void scanForCurrentVersion(Updates updates, String line) {
    if (updates.currentVersion)
        return
    def match = line =~ /\[INFO\] Building .* ([^ ]+)(-SNAPSHOT)?/
    if (match)
        updates.currentVersion = match.group(1)
}

private static void scanForUpdate(Updates updates, String line) {
    def match = line =~ /\[INFO\] Updated \$\{(?<name>.*)\} from (?<from>.*) to (?<to>.*)/
    if (match)
        updates.updates.add(new Updates.Update(name: match.group('name'), from: match.group('from'), to: match.group('to')))
}

private static void calculateUpdateVersion(Updates updates) {
    List<Integer> ints = numeric(updates.currentVersion)
    int biggestDiff = biggestDiff(updates)
    if (biggestDiff >= 0) {
        while (ints.size() <= biggestDiff)
            ints += 0
        if (biggestDiff + 1 < ints.size())
            ints[biggestDiff] += 1
        for (int i = biggestDiff + 1; i < ints.size(); i++)
            ints[i] = 0;
    }
    updates.updateVersion = ints.join('.')
}

private static int biggestDiff(Updates updates) {
    // updates.updates.collect { diff(numeric(it.from), numeric(it.to)) }.min()
    int min = 10
    for (int i = 0; i < updates.updates.size(); i++) {
        Updates.Update update = updates.updates.get(i)
        def diff = diff(numeric(update.from), numeric(update.to))
        if (diff < min)
            min = diff
    }
    return min
}

private static int diff(int[] from, int[] to) {
    for (int i = 0; i < from.length; i++) {
        if (from[i] > to[i])
            throw new IllegalArgumentException("invalid version update: $from -> $to")
        if (from[i] < to[i])
            return i
    }
    return (to.size() > from.size()) ? from.size() : Integer.MAX_VALUE
}

private static int[] numeric(String version) {
    // (version - ~'-SNAPSHOT$').split('\\.').collect { String it -> (it.isInteger()) ? it as Integer : -1 }
    String[] strings = (version - ~'-SNAPSHOT$').split('\\.');
    int[] ints = new int[strings.length]
    for (int i = 0; i < strings.length; i++) {
        String string = strings[i]
        ints[i] = (string.isInteger()) ? string as Integer : -1
    }
    return ints
}
