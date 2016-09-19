package Hack.Utilities;

import java.io.IOException;
import java.util.Properties;

public class GitRepositoryState {
    private static GitRepositoryState gitRepositoryState;
    private final String tags;
    private final String branch;
    private final String dirty;
    private final String remoteOriginUrl;
    private final String commitId;
    private final String commitIdAbbrev;
    private final String describe;
    private final String describeShort;
    private final String commitUserName;
    private final String commitUserEmail;
    private final String commitMessageFull;
    private final String commitMessageShort;
    private final String commitTime;
    private final String closestTagName;
    private final String closestTagCommitCount;
    private final String buildUserName;
    private final String buildUserEmail;
    private final String buildTime;
    private final String buildHost;
    private final String buildVersion;

    public static GitRepositoryState getGitRepositoryState() throws IOException {
        if (gitRepositoryState == null) {
            Properties properties = new Properties();
            properties.load(GitRepositoryState.class.getClassLoader().getResourceAsStream("git.properties"));

            gitRepositoryState = new GitRepositoryState(properties);
        }
        return gitRepositoryState;
    }

    private GitRepositoryState(Properties properties) {
        this.tags = String.valueOf(properties.get("git.tags"));
        this.branch = String.valueOf(properties.get("git.branch"));
        this.dirty = String.valueOf(properties.get("git.dirty"));
        this.remoteOriginUrl = String.valueOf(properties.get("git.remote.origin.url"));

        this.commitId = String.valueOf(properties.get("git.commit.id.full")); // OR properties.get("git.commit.id") depending on your configuration
        this.commitIdAbbrev = String.valueOf(properties.get("git.commit.id.abbrev"));
        this.describe = String.valueOf(properties.get("git.commit.id.describe"));
        this.describeShort = String.valueOf(properties.get("git.commit.id.describe-short"));
        this.commitUserName = String.valueOf(properties.get("git.commit.user.name"));
        this.commitUserEmail = String.valueOf(properties.get("git.commit.user.email"));
        this.commitMessageFull = String.valueOf(properties.get("git.commit.message.full"));
        this.commitMessageShort = String.valueOf(properties.get("git.commit.message.short"));
        this.commitTime = String.valueOf(properties.get("git.commit.time"));
        this.closestTagName = String.valueOf(properties.get("git.closest.tag.name"));
        this.closestTagCommitCount = String.valueOf(properties.get("git.closest.tag.commit.count"));

        this.buildUserName = String.valueOf(properties.get("git.build.user.name"));
        this.buildUserEmail = String.valueOf(properties.get("git.build.user.email"));
        this.buildTime = String.valueOf(properties.get("git.build.time"));
        this.buildHost = String.valueOf(properties.get("git.build.host"));
        this.buildVersion = String.valueOf(properties.get("git.build.version"));
    }

    public String getTags() {
        return tags;
    }

    public String getBranch() {
        return branch;
    }

    public String getDirty() {
        return dirty;
    }

    public String getRemoteOriginUrl() {
        return remoteOriginUrl;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getCommitIdAbbrev() {
        return commitIdAbbrev;
    }

    public String getDescribe() {
        return describe;
    }

    public String getDescribeShort() {
        return describeShort;
    }

    public String getCommitUserName() {
        return commitUserName;
    }

    public String getCommitUserEmail() {
        return commitUserEmail;
    }

    public String getCommitMessageFull() {
        return commitMessageFull;
    }

    public String getCommitMessageShort() {
        return commitMessageShort;
    }

    public String getCommitTime() {
        return commitTime;
    }

    public String getClosestTagName() {
        return closestTagName;
    }

    public String getClosestTagCommitCount() {
        return closestTagCommitCount;
    }

    public String getBuildUserName() {
        return buildUserName;
    }

    public String getBuildUserEmail() {
        return buildUserEmail;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public String getBuildHost() {
        return buildHost;
    }

    public String getBuildVersion() {
        return buildVersion;
    }
}
