namespace java im.cu.api.match_smart_cache.thrift

service CacheProxyService {
    CacheProxyAddRes add(1:CacheProxyAddReq req),
    CacheProxyFilterRes findContains(1:CacheProxyFilterReq req),
    CacheProxyFilterRes findNotContains(1:CacheProxyFilterReq req),
}

struct CacheProxyAddReq {
    1: required CacheKey key,
    2: required CacheType cacheType,
    3: required i32 value,
    4: required i64 date
}

struct CacheProxyAddRes {

}

struct CacheProxyFilterReq {
     1: CacheKey key,
     2: CacheConfig cacheConfig,
     3: list<i32> values
}

struct CacheProxyFilterRes {
    1: list<i32> values
}

enum CacheType {
    Full = 0,
    Limit = 1
}

struct CacheKey {
    1: string prefix,
    2: i32 userId
}

struct CacheConfig {
    1: CacheType cacheType,
    2: i32 days
}