import redis.clients.jedis.Jedis

def call(String name = "test") {
    return "redisState loaded for ${name}"
}

class RedisState implements Serializable {
    String namespace

    RedisState(String jobName, String buildNum) {
        this.namespace = "jenkins:${jobName}:${buildNum}"
    }

    private withRedis(Closure block) {
        // 🔑 Jedis connection — adjust for your Redis host/port/password
        def jedis = new Jedis("redis", 6379)
        try {
            return block(jedis)
        } finally {
            jedis.close()
        }
    }

    // --- Changed repos ---
    void addChangedRepo(String repo) {
        if (repo) {
            withRedis { r ->
                r.sadd("${namespace}:changedRepos", repo.toLowerCase())
            }
        }
    }

    boolean hasChangedRepo(String repo) {
        if (!repo) return false
        withRedis { r ->
            return r.sismember("${namespace}:changedRepos", repo.toLowerCase())
        }
    }

    // --- Missing certs ---
    void addMissingCert(String domain) {
        if (domain) {
            withRedis { r ->
                r.sadd("${namespace}:missingCerts", domain.toLowerCase())
            }
        }
    }

    boolean hasMissingCert(String domain) {
        if (!domain) return false
        withRedis { r ->
            return r.sismember("${namespace}:missingCerts", domain.toLowerCase())
        }
    }

    // --- Debug dump ---
    void dump(script) {
        withRedis { r ->
            def repos = r.smembers("${namespace}:changedRepos") ?: []
            def certs = r.smembers("${namespace}:missingCerts") ?: []
            script.echo "📊 RedisState dump:"
            script.echo "  - Changed repos: ${repos}"
            script.echo "  - Missing certs: ${certs}"
        }
    }

    // --- Clear all state for this namespace ---
    void clearAll() {
        withRedis { r ->
            def keys = r.keys("${namespace}:*")
            if (keys) {
                r.del(keys as String[])
            }
        }
    }
}
