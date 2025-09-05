def redisCmd(String cmd) {
    def host = env.REDIS_HOST ?: "redis"
    def port = env.REDIS_PORT ?: "6379"

    def pass = env.REDIS_PASSWORD ?: ""
    def authPart = pass ? "-a ${pass}" : ""
    return sh(
        script: "redis-cli -h ${host} -p ${port} ${authPart} ${cmd}",
        returnStdout: true
    ).trim()
}

def call(String action, Map args = [:]) {
    switch(action) {
        case "set":
            return redisCmd("set ${args.key} '${args.value}'")

        case "lpush":
            return redisCmd("lpush ${args.key} '${args.value}'")

        case "lcontains":
            def result = redisCmd("lrange ${args.key} 0 -1 | grep -x '${args.value}' || true")
            return result ? true : false

        case "get":
            return redisCmd("get ${args.key} || true")

        case "lrange":
            return redisCmd("lrange ${args.key} 0 -1").split("\n")

        default:
            error "Unknown action: ${action}"
    }
}
