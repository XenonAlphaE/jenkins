class PipelineState implements Serializable {
    def changedRepos = []
    def missingCerts = []

    void addChangedRepo(String repo) {
        changedRepos << repo
    }

    boolean hasChangedRepo(String repo) {
        return changedRepos.contains(repo)
    }
}

return new PipelineState()
