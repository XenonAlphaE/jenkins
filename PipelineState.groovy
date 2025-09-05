class PipelineState implements Serializable {
    List<String> changedRepos = []
    List<String> missingCerts = []

    void addChangedRepo(String repo) {
        if (repo) changedRepos << repo.toLowerCase()
    }
    boolean hasChangedRepo(String repo) {
        return changedRepos.contains(repo?.toLowerCase())
    }

    void addMissingCert(String domain) {
        if (domain) missingCerts << domain.toLowerCase()
    }
    boolean hasMissingCert(String domain) {
        return missingCerts.contains(domain?.toLowerCase())
    }
}

// 👇 Return a factory method so Jenkins can use it
return [
    newState: { -> new PipelineState() }
]
