package com.why.demo.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wanghy
 */
@Component
public class RedisClient {

    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);

//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * redis分页查询，获取满足匹配规则的key集合
     *
     * @param pattern  匹配规则
     * @param pageNum  页码，第几页
     * @param pageSize 条数，每页展示几条数据
     * @return key集合
     */
    public List<String> getKeysForPage(String pattern, int pageNum, int pageSize) {
        List<String> keys = new ArrayList<>(pageSize);
        int tmpIndex = 0;
        int startIndex = (pageNum - 1) * pageSize;
        int end = pageNum * pageSize;
        RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).build();
        try (Cursor<String> cursor = redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), keySerializer::deserialize))) {
            while (true) {
                assert cursor != null;
                if (!cursor.hasNext()) {
                    break;
                }
                //从第多少条开始获取数据
                if (tmpIndex >= startIndex && tmpIndex < end) {
                    keys.add(cursor.next());
                    tmpIndex++;
                    continue;
                }
                //数量满足之后，结束
                if (tmpIndex >= end) {
                    break;
                }
                tmpIndex++;
                cursor.next();
            }
        } catch (Exception e) {
            logger.error("扫描redis中的key时异常", e);
        }
        return keys;
    }

    /**
     * 扫描redis中String类型的key，取出匹配规则中指定数量的key
     *
     * @param pattern 匹配规则
     * @param count   取出的数量
     * @return key的集合
     */
    public List<String> scan(String pattern, long count) {
        List<String> keys = new ArrayList<>();
        RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(count).build();
        try (Cursor<String> cursor = redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), keySerializer::deserialize))) {
            while (true) {
                assert cursor != null;
                if (!cursor.hasNext()) {
                    break;
                }
                //数量满足之后，结束
                if (keys.size() >= count) {
                    break;
                }
                keys.add(cursor.next());
            }
        } catch (Exception e) {
            logger.error("扫描redis中的key时异常", e);
        }
        return keys;
    }

    /**
     * 移除key的过期时间，key将永久保存
     *
     * @param key key
     * @return
     */
    public boolean persist(String key) {
        Boolean success = redisTemplate.persist(key);
        return success == null ? false : success;
    }

    /**
     * 仅当 newKey不存在时，修改 key 的名称
     *
     * @param oldKey 旧key
     * @param newKey 新key
     */
    public boolean renameIfAbsent(String oldKey, String newKey) {
        Boolean success = redisTemplate.renameIfAbsent(oldKey, newKey);
        return success == null ? false : success;
    }

    /**
     * 修改 key 的名称
     *
     * @param oldKey 旧key
     * @param newKey 新key
     */
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 返回 key 所储存的值的类型
     *
     * @param key key
     * @return
     */
    public DataType type(String key) {
        return redisTemplate.type(key);
    }

    /**
     * 判断key是否存在
     *
     * @param key key
     * @return
     */
    public boolean exists(String key) {
        Boolean success = redisTemplate.hasKey(key);
        return success == null ? false : success;
    }

    /**
     * 设置过期时间
     *
     * @param key     key
     * @param timeout 过期时间
     * @param unit    过期时间的单位
     * @return
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean success = redisTemplate.expire(key, timeout, unit);
        return success == null ? false : success;
    }

    /**
     * 设置到期时间
     *
     * @param key  key
     * @param date 到期时间
     * @return
     */
    public boolean expireAt(String key, Date date) {
        Boolean success = redisTemplate.expireAt(key, date);
        return success == null ? false : success;
    }

    /**
     * 获取key的过期时间
     *
     * @param key key
     * @return 过期时间，返回 0代表为永久有效
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 返回 key 的剩余的过期时间
     *
     * @param key  key
     * @param unit 过期时间的单位
     * @return 过期时间，返回 0代表为永久有效
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 查找匹配的key值，返回某类缓存的全部key
     *
     * @param pattern 匹配的key
     * @return 匹配的key集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 删除key
     *
     * @param keys 可以传入一个值，或多个
     */
    public void delete(String... keys) {
        if (null != keys && keys.length > 0) {
            if (keys.length == 1) {
                redisTemplate.delete(keys[0]);
            } else {
                redisTemplate.delete(Arrays.asList(keys));
            }
        }
    }

    /* ------------------------String相关操作---------------------------- */

    /**
     * 永久存放
     *
     * @param key   key
     * @param value value
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 存放数据，并设置过期时间
     *
     * @param key     key
     * @param value   value
     * @param timeout 过期时间
     * @param unit    时间单位, 天:TimeUnit.DAYS 小时:TimeUnit.HOURS 分钟:TimeUnit.MINUTES
     *                秒:TimeUnit.SECONDS 毫秒:TimeUnit.MILLISECONDS
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 只有在 key 不存在时，设置 key 的值
     *
     * @param key   key
     * @param value value
     * @return
     */
    public boolean setIfAbsent(String key, String value) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value);
        return success == null ? false : success;
    }

    /**
     * 只有在 key 不存在时，设置 key 的值，并设置过期时间(秒)
     *
     * @param key   key
     * @param value value
     * @return
     */
    public boolean setIfAbsent(String key, String value, long timeout) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS);
        return success == null ? false : success;
    }

    /**
     * 只有在 key 不存在时，设置 key 的值，并设置过期时间
     *
     * @param key   key
     * @param value value
     * @return
     */
    public boolean setIfAbsent(String key, String value, long timeout, TimeUnit unit) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit);
        return success == null ? false : success;
    }

    /**
     * 批量添加
     *
     * @param maps
     */
    public void multiSet(Map<String, String> maps) {
        redisTemplate.opsForValue().multiSet(maps);
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     *
     * @param maps key-value键值对集合
     * @return
     */
    public boolean multiSetIfAbsent(Map<String, String> maps) {
        Boolean success = redisTemplate.opsForValue().multiSetIfAbsent(maps);
        return success == null ? false : success;
    }

    /**
     * 将key的值设置为value，并返回 key 的旧值(old value)
     *
     * @param key   key
     * @param value 新value
     * @return 旧value
     */
    public String getAndSet(String key, String value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 根据key获取value
     *
     * @param key key
     * @return value
     */
    public String get(String key) {
        return null == key ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 根据key获取value(字符串)的长度
     *
     * @param key key
     * @return
     */
    public Long size(String key) {
        return redisTemplate.opsForValue().size(key);
    }

    /**
     * 批量获取
     *
     * @param keys key的集合
     * @return
     */
    public List<String> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 增加(自增长), 负数则为自减
     *
     * @param key key
     * @return
     */
    public Long incrBy(String key, long increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    /* ------------------------hash相关操作---------------------------- */

    /**
     * 根据key获取hash表中某个hashKey的值
     *
     * @param key   key
     * @param field hash表中的key
     * @return
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取所有给定字段的值
     *
     * @param key key
     * @return
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取哈希表中所有值
     *
     * @param key
     * @return
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 获取key对应的所有哈希表中的key
     *
     * @param key key
     * @return hash表中的所有key的集合
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取key对应的哈希表中key的数量
     *
     * @param key key
     * @return hash表中的key的数量
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 根据key获取hash表中多个给定的hashKey的值
     *
     * @param key    key
     * @param fields hash表中的多个key的集合
     * @return
     */
    public List<Object> hMultiGet(String key, Collection<Object> fields) {
        return redisTemplate.opsForHash().multiGet(key, fields);
    }

    /**
     * 存放hash表
     *
     * @param key     key
     * @param hashKey hash表中的key
     * @param value   hash表中的value
     */
    public void hPut(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 存放hash表
     *
     * @param key  key
     * @param maps hash表中的key
     */
    public void hPutAll(String key, Map<String, String> maps) {
        redisTemplate.opsForHash().putAll(key, maps);
    }

    /**
     * 仅当hashKey不存在时，才设置hash表
     *
     * @param key     key
     * @param hashKey hash表中的key
     * @param value   hash表中的value
     * @return
     */
    public boolean hPutIfAbsent(String key, String hashKey, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    /**
     * 删除 key 对应的hash表中的一个或多个key
     *
     * @param key    key
     * @param fields hash表中的key
     * @return
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断key对应的哈希表中，指定的key是否存在
     *
     * @param key   key
     * @param field hash表中的key
     * @return
     */
    public boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     *
     * @param key       key
     * @param field     hash表中的key
     * @param increment 增加(自增长), 负数则为自减
     * @return
     */
    public Long hIncrBy(String key, Object field, long increment) {
        return redisTemplate.opsForHash().increment(key, field, increment);
    }


    /* ------------------------list相关操作---------------------------- */

    /**
     * 通过索引获取列表中的元素
     *
     * @param key
     * @param index
     * @return
     */
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 获取列表指定范围内的元素
     *
     * @param key
     * @param start 开始位置, 0是开始位置
     * @param end   结束位置, -1返回所有
     * @return
     */
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 存储在list头部
     *
     * @param key
     * @param value
     * @return
     */
    public Long lLeftPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public Long lLeftPushAll(String key, String... value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public Long lLeftPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 当list存在的时候才加入
     *
     * @param key
     * @param value
     * @return
     */
    public Long lLeftPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    /**
     * 如果pivot存在,再pivot前面添加
     *
     * @param key
     * @param pivot
     * @param value
     * @return
     */
    public Long lLeftPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public Long lRightPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public Long lRightPushAll(String key, String... value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * @param key
     * @param value
     * @return
     */
    public Long lRightPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 为已存在的列表添加值
     *
     * @param key
     * @param value
     * @return
     */
    public Long lRightPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    /**
     * 在pivot元素的右边添加值
     *
     * @param key
     * @param pivot
     * @param value
     * @return
     */
    public Long lRightPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    /**
     * 通过索引设置列表元素的值
     *
     * @param key
     * @param index 位置
     * @param value
     */
    public void lSet(String key, long index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 移出并获取列表的第一个元素
     *
     * @param key
     * @return 删除的元素
     */
    public String lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param key
     * @param timeout 等待时间
     * @param unit    时间单位
     * @return
     */
    public String lBLeftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    /**
     * 移除并获取列表最后一个元素
     *
     * @param key
     * @return 删除的元素
     */
    public String lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param key
     * @param timeout 等待时间
     * @param unit    时间单位
     * @return
     */
    public String lBRightPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPop(key, timeout, unit);
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     *
     * @param sourceKey
     * @param destinationKey
     * @return
     */
    public String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
                destinationKey);
    }

    /**
     * 从列表中弹出一个值，将弹出的元素插入到另外一个列表中并返回它； 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止
     *
     * @param sourceKey
     * @param destinationKey
     * @param timeout
     * @param unit
     * @return
     */
    public String lBRightPopAndLeftPush(String sourceKey, String destinationKey,
                                        long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey,
                destinationKey, timeout, unit);
    }

    /**
     * 删除集合中值等于value得元素
     *
     * @param key
     * @param index index=0, 删除所有值等于value的元素; index>0, 从头部开始删除第一个值等于value的元素;
     *              index<0, 从尾部开始删除第一个值等于value的元素;
     * @param value
     * @return
     */
    public Long lRemove(String key, long index, String value) {
        return redisTemplate.opsForList().remove(key, index, value);
    }

    /**
     * 裁剪list
     *
     * @param key
     * @param start
     * @param end
     */
    public void lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表长度
     *
     * @param key
     * @return
     */
    public Long lLen(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /** --------------------set相关操作-------------------------- */

    /**
     * set添加元素
     *
     * @param key
     * @param values
     * @return
     */
    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * set移除元素
     *
     * @param key
     * @param values
     * @return
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 移除并返回集合的一个随机元素
     *
     * @param key
     * @return
     */
    public String sPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    /**
     * 将元素value从一个集合移到另一个集合
     *
     * @param key
     * @param value
     * @param destKey
     * @return
     */
    public boolean sMove(String key, String value, String destKey) {
        Boolean success = redisTemplate.opsForSet().move(key, value, destKey);
        return success == null ? false : success;
    }

    /**
     * 获取集合的大小
     *
     * @param key
     * @return
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 判断集合是否包含value
     *
     * @param key
     * @param value
     * @return
     */
    public boolean sIsMember(String key, Object value) {
        Boolean success = redisTemplate.opsForSet().isMember(key, value);
        return success == null ? false : success;
    }

    /**
     * 获取两个集合的交集
     *
     * @param key
     * @param otherKey
     * @return
     */
    public Set<String> sIntersect(String key, String otherKey) {
        return redisTemplate.opsForSet().intersect(key, otherKey);
    }

    /**
     * 获取key集合与多个集合的交集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    public Set<String> sIntersect(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().intersect(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的交集存储到destKey集合中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    public Long sIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKey,
                destKey);
    }

    /**
     * key集合与多个集合的交集存储到destKey集合中
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    public Long sIntersectAndStore(String key, Collection<String> otherKeys,
                                   String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKeys,
                destKey);
    }

    /**
     * 获取两个集合的并集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    public Set<String> sUnion(String key, String otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * 获取key集合与多个集合的并集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    public Set<String> sUnion(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的并集存储到destKey中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    public Long sUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * key集合与多个集合的并集存储到destKey中
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    public Long sUnionAndStore(String key, Collection<String> otherKeys,
                               String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的差集
     *
     * @param key
     * @param otherKey
     * @return
     */
    public Set<String> sDifference(String key, String otherKey) {
        return redisTemplate.opsForSet().difference(key, otherKey);
    }

    /**
     * 获取key集合与多个集合的差集
     *
     * @param key
     * @param otherKeys
     * @return
     */
    public Set<String> sDifference(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().difference(key, otherKeys);
    }

    /**
     * key集合与otherKey集合的差集存储到destKey中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    public Long sDifference(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKey,
                destKey);
    }

    /**
     * key集合与多个集合的差集存储到destKey中
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    public Long sDifference(String key, Collection<String> otherKeys,
                            String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKeys,
                destKey);
    }

    /**
     * 获取集合所有元素
     *
     * @param key
     * @return
     */
    public Set<String> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 随机获取集合中的一个元素
     *
     * @param key
     * @return
     */
    public String sRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 随机获取集合中count个元素
     *
     * @param key
     * @param count
     * @return
     */
    public List<String> sRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }

    /**
     * 随机获取集合中count个元素并且去除重复的
     *
     * @param key
     * @param count
     * @return
     */
    public Set<String> sDistinctRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    /**
     * @param key
     * @param options
     * @return
     */
    public Cursor<String> sScan(String key, ScanOptions options) {
        return redisTemplate.opsForSet().scan(key, options);
    }

    /*------------------zSet相关操作--------------------------------*/

    /**
     * 添加元素,有序集合是按照元素的score值由小到大排列
     *
     * @param key
     * @param value
     * @param score
     * @return
     */
    public boolean zAdd(String key, String value, double score) {
        Boolean success = redisTemplate.opsForZSet().add(key, value, score);
        return success == null ? false : success;
    }

    /**
     * @param key
     * @param values
     * @return
     */
    public Long zAdd(String key, Set<ZSetOperations.TypedTuple<String>> values) {
        return redisTemplate.opsForZSet().add(key, values);
    }

    /**
     * @param key
     * @param values
     * @return
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 增加元素的score值，并返回增加后的值
     *
     * @param key
     * @param value
     * @param delta
     * @return
     */
    public Double zIncrementScore(String key, String value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 返回元素在集合的排名,有序集合是按照元素的score值由小到大排列
     *
     * @param key
     * @param value
     * @return 0表示第一位
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 返回元素在集合的排名,按元素的score值由大到小排列
     *
     * @param key
     * @param value
     * @return
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 获取集合的元素, 从小到大排序
     *
     * @param key
     * @param start 开始位置
     * @param end   结束位置, -1查询所有
     * @return
     */
    public Set<String> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取集合元素, 并且把score值也获取
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeWithScores(String key, long start,
                                                                   long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 根据Score值查询集合元素
     *
     * @param key
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 根据Score值查询集合元素, 从小到大排序
     *
     * @param key
     * @param min 最小值
     * @param max 最大值
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores(String key,
                                                                          double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    /**
     * @param key
     * @param min
     * @param max
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores(String key,
                                                                          double min, double max, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max,
                start, end);
    }

    /**
     * 获取集合的元素, 从大到小排序
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取集合的元素, 从大到小排序, 并返回score值
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeWithScores(String key,
                                                                          long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start,
                end);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<String> zReverseRangeByScore(String key, double min,
                                            double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    /**
     * 根据Score值查询集合元素, 从大到小排序
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores(
            String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key,
                min, max);
    }

    /**
     * @param key
     * @param min
     * @param max
     * @param start
     * @param end
     * @return
     */
    public Set<String> zReverseRangeByScore(String key, double min,
                                            double max, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max,
                start, end);
    }

    /**
     * 根据score值获取集合元素数量
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取集合大小
     *
     * @param key
     * @return
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取集合大小
     *
     * @param key
     * @return
     */
    public Long zZCard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取集合中value元素的score值
     *
     * @param key
     * @param value
     * @return
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 移除指定索引位置的成员
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 根据指定的score值的范围来移除成员
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     * 获取key和otherKey的并集并存储在destKey中
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    public Long zUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    public Long zUnionAndStore(String key, Collection<String> otherKeys,
                               String destKey) {
        return redisTemplate.opsForZSet()
                .unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 交集
     *
     * @param key
     * @param otherKey
     * @param destKey
     * @return
     */
    public Long zIntersectAndStore(String key, String otherKey,
                                   String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKey,
                destKey);
    }

    /**
     * 交集
     *
     * @param key
     * @param otherKeys
     * @param destKey
     * @return
     */
    public Long zIntersectAndStore(String key, Collection<String> otherKeys,
                                   String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys,
                destKey);
    }

    /**
     * @param key
     * @param options
     * @return
     */
    public Cursor<ZSetOperations.TypedTuple<String>> zScan(String key, ScanOptions options) {
        return redisTemplate.opsForZSet().scan(key, options);
    }
}
