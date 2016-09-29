import Updates.Update
import org.junit.Test

import static scan.call

class UpdatesTest {
    public static Updates scan(String building, String... updates) throws Exception {
        String mvnOut = (''
                + 'Scanning for projects...\n'
                + '\n'
                + '------------------------------------------------------------------------\n'
                + building + '\n'
                + '------------------------------------------------------------------------\n'
                + '\n'
                + '--- versions-maven-plugin:2.3:update-properties (default-cli) @ deployer ---\n'
                + updates.join('\n') + '\n'
                + '------------------------------------------------------------------------\n'
                + 'BUILD SUCCESS\n'
                + '------------------------------------------------------------------------\n'
                + 'Total time: 1.143 s\n'
                + 'Finished at: 2016-09-22T04:41:02+02:00\n'
                + 'Final Memory: 14M/245M\n'
                + '------------------------------------------------------------------------\n').
                readLines().collect { line -> '[INFO] ' + line }.join('\n')

        return call(mvnOut)
    }

    @Test
    public void shouldParseVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT'

        assert '2.3.2-SNAPSHOT' == updates.currentVersion
    }

    @Test
    public void shouldParseWithNonNumericVersion() throws Exception {
        Updates updates = scan 'Building Deployer 1.0-beta3'

        assert '1.0-beta3' == updates.currentVersion
    }

    @Test
    public void shouldParseVersionWithTwoWordName() throws Exception {
        Updates updates = scan 'Building The Deployer 1.0'

        assert '1.0' == updates.currentVersion
    }

    @Test
    public void shouldParseVersionWithNumericWordName() throws Exception {
        Updates updates = scan 'Building The Deployer 2 1.0.0'

        assert '1.0.0' == updates.currentVersion
    }

    @Test
    public void shouldParseUpdateEmpty() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT'

        assert updates.isEmpty()
    }

    @Test
    public void shouldParseUpdateNotEmpty() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${jackson.version} from 2.7.5 to 2.8.1'

        assert !updates.isEmpty()
    }

    @Test
    public void shouldParseUpdate() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${jackson.version} from 2.7.5 to 2.8.1'

        assert [new Update(name: 'jackson.version', from: '2.7.5', to: '2.8.1')] == updates.updates
    }

    @Test
    public void shouldCalculateNextMicroVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${jackson.version} from 2.7.5 to 2.7.6'

        assert '2.3.2' == updates.updateVersion
    }

    @Test
    public void shouldCalculateNextMinorVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${jackson.version} from 2.7.5 to 2.8.1'

        assert '2.4.0' == updates.updateVersion
    }

    @Test
    public void shouldCalculateNextLongerMinorVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${jackson.version} from 2.7.5 to 2.8'

        assert '2.4.0' == updates.updateVersion
    }

    @Test
    public void shouldLengthenNextMinorVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2-SNAPSHOT', 'Updated ${jackson.version} from 2.7.5 to 2.8.1'

        assert '2.0' == updates.updateVersion
    }

    @Test
    public void shouldLengthenNextMicroVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2-SNAPSHOT', 'Updated ${jackson.version} from 2.7.5 to 2.7.6'

        assert '2.0.0' == updates.updateVersion
    }

    @Test
    public void shouldCalculateNextMajorVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${jackson.version} from 1.7.5 to 2.8.1'

        assert '3.0.0' == updates.updateVersion
    }

    @Test
    public void shouldCalculateNextVersionWithNonNumericSuffix() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${postgresql.version} from 9.4.1211 to 9.4.1211.jre7'

        assert '2.3.2.0' == updates.updateVersion
    }

    @Test
    public void shouldCalculateNextVersionWithNonNumeric() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT', 'Updated ${postgresql.version} from 9.4.1210 to 9.4.1211.jre7'

        assert '2.3.2' == updates.updateVersion
    }

    @Test
    public void shouldCalculateNextMinorAndMicroVersion() throws Exception {
        Updates updates = scan 'Building Deployer 2.3.2-SNAPSHOT',
                'Updated ${foo} from 2.7.5 to 2.7.6',
                'Updated ${bar} from 1.7 to 1.8'

        assert '2.4.0' == updates.updateVersion
    }
}
