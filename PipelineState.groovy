class PipelineState implements Serializable {
    def changedRepos = []
    def missingCerts = []

    // --- Changed repos ---
    void addChangedRepo(String repo) {
        if (repo) {
            changedRepos << repo.toLowerCase()
        }
    }

    boolean hasChangedRepo(String repo) {
        return changedRepos.contains(repo?.toLowerCase())
    }

    // --- Missing certs ---
    void addMissingCert(String domain) {
        if (domain) {
            missingCerts << domain.toLowerCase()
        }
    }

    boolean hasMissingCert(String domain) {
        return missingCerts.contains(domain?.toLowerCase())
    }

    // --- Debugging helper ---
    void dump() {
        echo "📊 State dump:"
        echo "  - Changed repos: ${changedRepos}"
        echo "  - Missing certs: ${missingCerts}"
    }
}
return new PipelineState()
