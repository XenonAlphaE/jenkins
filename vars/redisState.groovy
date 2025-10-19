// =============================
// redisState.groovy
// Safe shared Redis state for Jenkins pipelines
// =============================

// Default expiry in seconds (1 day)
def defaultTtl() {
    return (env.REDIS_TTL ?: "86400") as Integer
}

// Execute Redis command via redis-cli
def redisCmd(String cmd) {
    def host = env.REDIS_HOST ?: "redis"
    def port = env.REDIS_PORT ?: "6379"
    def pass = env.REDIS_PASS ?: ""
    def authPart = pass ? "-a ${pass}" : ""

    return sh(
        script: "redis-cli -h ${host} -p ${port} ${authPart} ${cmd}",
        returnStdout: true
    ).trim()
}

// ---------------------------
// 🔑 Namespace Management
// ---------------------------

// Compute the effective Redis prefix for this pipeline
def keyPrefix(String sharedNamespace = null) {
    def job = env.JOB_NAME ?: "default-job"
    def build = env.BUILD_NUMBER ?: "0"

    // If a shared namespace is explicitly provided
    if (sharedNamespace?.trim()) {
        // Shared prefix but still traceable to the job (optional)
        return sharedNamespace?.trim()
    }

    // Default: isolated per job+build
    return "jenkins:${job}:${build}"
}

// ---------------------------
// List operations
// ---------------------------
def listPush(String key, String value, Integer ttl = defaultTtl(), String sharedNamespace = null) {
    def fullKey = "${keyPrefix(sharedNamespace)}:${key}"
    redisCmd("lpush ${fullKey} '${value}'")
    if (ttl > 0) {
        redisCmd("expire ${fullKey} ${ttl}")
    }
    echo "[redisState] Added '${value}' to ${fullKey} (ttl=${ttl}s)"
    return true
}

def listContains(String key, String value, String sharedNamespace = null) {
    def fullKey = "${keyPrefix(sharedNamespace)}:${key}"
    def result = redisCmd("lrange ${fullKey} 0 -1 | grep -x '${value}' || true")
    return result ? true : false
}

def listGet(String key, String sharedNamespace = null) {
    def fullKey = "${keyPrefix(sharedNamespace)}:${key}"
    def res = redisCmd("lrange ${fullKey} 0 -1")
    def list = res ? res.split("\n") : []
    echo "[redisState] Current values for ${fullKey} = ${list}"
    return list
}

def listClear(String key, String sharedNamespace = null) {
    def fullKey = "${keyPrefix(sharedNamespace)}:${key}"
    echo "[redisState] Clearing list for ${fullKey}"
    return redisCmd("del ${fullKey}")
}

// ---------------------------
// Convenience wrappers
// ---------------------------
def addMissingCert(String domain, Integer ttl = defaultTtl(), String sharedNamespace = null) {
    if (domain) {
        listPush("missingCerts", domain.toLowerCase().trim(), ttl, sharedNamespace)
    }
}

def addChangedRepo(String repo, Integer ttl = defaultTtl(), String sharedNamespace = null) {
    if (repo) {
        listPush("changedRepos", repo.toLowerCase().trim(), ttl, sharedNamespace)
    }
}

def getMissingCerts(String sharedNamespace = null) {
    return listGet("missingCerts", sharedNamespace)
}

def getChangedRepos(String sharedNamespace = null) {
    return listGet("changedRepos", sharedNamespace)
}

def isMissingCert(String domain, String sharedNamespace = null) {
    if (!domain) return false
    return listContains("missingCerts", domain.toLowerCase().trim(), sharedNamespace)
}

def isNewCommit(String repo, String sharedNamespace = null) {
    if (!repo) return false
    return listContains("changedRepos", repo.toLowerCase().trim(), sharedNamespace)
}

def clearAll(String sharedNamespace = null) {
    listClear("missingCerts", sharedNamespace)
    listClear("changedRepos", sharedNamespace)
}

def call() {
    return this
}
