// Default expiry in seconds (e.g. 1 day = 86400)
def defaultTtl() {
    return (env.REDIS_TTL ?: "86400") as Integer
}

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

def keyPrefix() {
    def job = env.JOB_NAME ?: "default-job"
    def build = env.BUILD_NUMBER ?: "0"
    return "jenkins:${job}:${build}"
}

def listPush(String key, String value, Integer ttl = defaultTtl()) {
    def fullKey = "${keyPrefix()}:${key}"
    // Push value
    redisCmd("lpush ${fullKey} '${value}'")
    // Set expiry if > 0
    if (ttl > 0) {
        redisCmd("expire ${fullKey} ${ttl}")
    }
    echo "[redisState] Added '${value}' to ${fullKey} (ttl=${ttl}s)"
    return true
}

def listContains(String key, String value) {
    def fullKey = "${keyPrefix()}:${key}"
    def result = redisCmd("lrange ${fullKey} 0 -1 | grep -x '${value}' || true")
    return result ? true : false
}

def listGet(String key) {
    def fullKey = "${keyPrefix()}:${key}"
    def res = redisCmd("lrange ${fullKey} 0 -1")
    def list = res ? res.split("\n") : []
    echo "[redisState] Current values for ${fullKey} = ${list}"
    return list
}

def listClear(String key) {
    def fullKey = "${keyPrefix()}:${key}"
    echo "[redisState] Clearing list for ${fullKey}"
    return redisCmd("del ${fullKey}")
}

// ---------------------------
// Convenience wrappers
// ---------------------------
def addMissingCert(String domain, Integer ttl = defaultTtl()) {
    if (domain) {
        listPush("missingCerts", domain.toLowerCase().trim(), ttl)
    }
}

def addChangedRepo(String repo, Integer ttl = defaultTtl()) {
    if (repo) {
        listPush("changedRepos", repo.toLowerCase().trim(), ttl)
    }
}

def getMissingCerts() {
    return listGet("missingCerts")
}

def getChangedRepos() {
    return listGet("changedRepos")
}

// ---------------------------
// Existing API
// ---------------------------
def isMissingCert(String domain) {
    if (!domain) return false
    return listContains("missingCerts", domain.toLowerCase().trim())
}

def isNewCommit(String repo) {
    if (!repo) return false
    return listContains("changedRepos", repo.toLowerCase().trim())
}

def clearAll() {
    listClear("missingCerts")
    listClear("changedRepos")
}

def call() {
    return this
}
