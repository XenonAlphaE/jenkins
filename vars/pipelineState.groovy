// vars/pipelineState.groovy
class PipelineState implements Serializable {
    def changedRepos = []
    def missingCerts = []

    // Add changed repo
    void addChangedRepo(String repo) {
        if (repo) changedRepos << repo.toLowerCase()
    }

    boolean hasChangedRepo(String repo) {
        return changedRepos.contains(repo?.toLowerCase())
    }

    // Add missing cert
    void addMissingCert(String domain) {
        if (domain) missingCerts << domain.toLowerCase()
    }

    boolean hasMissingCert(String domain) {
        return missingCerts.contains(domain?.toLowerCase())
    }

    void dump(script) {
        script.echo "📊 State dump:"
        script.echo "  - Changed repos: ${changedRepos}"
        script.echo "  - Missing certs: ${missingCerts}"
    }
}

// 🚀 Entry point – Jenkins will expose `pipelineState` as a global var
def call() {
    return new PipelineState()
}
