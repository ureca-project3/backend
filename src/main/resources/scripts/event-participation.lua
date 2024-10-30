-- 키 정의
local participantKey = KEYS[1]    -- 참여자 Set
local counterKey = KEYS[2]        -- 카운터
local dataKey = KEYS[3]          -- 참여자 데이터

-- 파라미터
local userId = ARGV[1]
local maxCount = tonumber(ARGV[2])
local jsonData = ARGV[3]

-- 중복 참여 체크
if redis.call('SISMEMBER', participantKey, userId) == 1 then
    return -1
end

-- 참여자 수 확인 및 증가
local count = redis.call('INCR', counterKey)
if count > maxCount then
    redis.call('DECR', counterKey)
    return -2
end

-- 참여자 등록 및 데이터 저장
redis.call('SADD', participantKey, userId)
redis.call('SET', dataKey, jsonData)

return count