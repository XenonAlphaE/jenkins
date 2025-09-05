// ---------------------------
// Redis helpers
// ---------------------------
def redisCmd(String cmd) {
    def host = env.REDIS_HOST ?: "redis"
    def port = env.REDIS_PORT ?: "6379"
    def pass = ""
    def authPart = pass ? "-a ${pass}" : ""

    return sh(
        script: "redis-cli -h ${host} -p ${port} ${authPart} ${cmd}",
        returnStdout: true
    ).trim()
}

// Build a unique key prefix per pipeline/job
def keyPrefix() {
    def job = env.JOB_NAME ?: "default-job"
    def build = env.BUILD_NUMBER ?: "0"
    return "jenkins:${job}:${build}"
}

def redisListContains(String key, String value) {
    def fullKey = "${keyPrefix()}:${key}"
    def result = redisCmd("lrange ${fullKey} 0 -1 | grep -x '${value}' || true")
    return result ? true : false
}

def redisListPush(String key, String value) {
    def fullKey = "${keyPrefix()}:${key}"
    return redisCmd("lpush ${fullKey} '${value}'")
}

def redisListGet(String key) {
    def fullKey = "${keyPrefix()}:${key}"
    return redisCmd("lrange ${fullKey} 0 -1").split("\n")
}

def redisListClear(String key) {
    def fullKey = "${keyPrefix()}:${key}"
    return redisCmd("del ${fullKey}")
}

// ---------------------------
// API methods (same signatures)
// ---------------------------
def isMissingCert(String domain) {
    if (!domain) return false
    return redisListContains("missingCerts", domain.toLowerCase().trim())
}

def isNewCommit(String repo) {
    if (!repo) return false
    return redisListContains("changedRepos", repo.toLowerCase().trim())
}

// ---------------------------
// Clear all lists for this pipeline
// ---------------------------
def clearAll() {
    redisListClear("missingCerts")
    redisListClear("changedRepos")
}
