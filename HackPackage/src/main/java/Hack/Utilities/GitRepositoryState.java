package Hack.Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

class GitRepositoryState {
    private static GitRepositoryState gitRepositoryState;
    private final String describeShort;
    private final String buildVersion;

    static GitRepositoryState getGitRepositoryState() throws IOException {
        if (gitRepositoryState == null) {
            final URL gitProperties = GitRepositoryState.class.getClassLoader().getResource("git.properties");
            final Properties properties = new Properties();
            if (gitProperties != null) {
                try (InputStream is = gitProperties.openStream()) {
                    properties.load(is);
                }
            } else {
                properties.setProperty("git.commit.id.describe-short", "unknown");
                properties.setProperty("git.build.version", "unknown");
            }

            gitRepositoryState = new GitRepositoryState(properties);
        }
        return gitRepositoryState;
    }

    private GitRepositoryState(Properties properties) {
        this.describeShort = String.valueOf(properties.get("git.commit.id.describe-short"));
        this.buildVersion = String.valueOf(properties.get("git.build.version"));
    }

    String getDescribeShort() {
        return describeShort;
    }

    String getBuildVersion() {
        return buildVersion;
    }
}
