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

def listPush(String key, String value) {
    def fullKey = "${keyPrefix()}:${key}"
    return redisCmd("lpush ${fullKey} '${value}'")
}

def listContains(String key, String value) {
    def fullKey = "${keyPrefix()}:${key}"
    def result = redisCmd("lrange ${fullKey} 0 -1 | grep -x '${value}' || true")
    return result ? true : false
}

def listGet(String key) {
    def fullKey = "${keyPrefix()}:${key}"
    return redisCmd("lrange ${fullKey} 0 -1").split("\n")
}

def listClear(String key) {
    def fullKey = "${keyPrefix()}:${key}"
    return redisCmd("del ${fullKey}")
}

// ---------------------------
// Convenience wrappers
// ---------------------------
def addMissingCert(String domain) {
    if (domain) {
        listPush("missingCerts", domain.toLowerCase().trim())
    }
}

def addChangedRepo(String repo) {
    if (repo) {
        listPush("changedRepos", repo.toLowerCase().trim())
    }
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
