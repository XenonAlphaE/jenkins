import redis.clients.jedis.Jedis

def call(String name = "test") {
    return "redisState loaded for ${name}"
}

class RedisState implements Serializable {
    String namespace
    
}