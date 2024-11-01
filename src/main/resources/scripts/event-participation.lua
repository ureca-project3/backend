-- 키 정의
local participantKey = KEYS[1]    -- 참여자 Set
local dataKey = KEYS[2]          -- 참여자 상세 데이터
local startTimeKey = KEYS[3]      -- 이벤트 시작 시간
local endTimeKey = KEYS[4]        -- 이벤트 마감 시간

-- 파라미터
local userId = ARGV[1]
local currentTime = tonumber(ARGV[2])  -- 현재 시간 (timestamp)
local jsonData = ARGV[3]

-- 디버깅을 위한 로그
redis.log(redis.LOG_WARNING, "startTimeKey: " .. startTimeKey)
redis.log(redis.LOG_WARNING, "endTimeKey: " .. endTimeKey)

-- 이벤트 시간 검증
local startTime = redis.call('GET', startTimeKey)
local endTime = redis.call('GET', endTimeKey)

redis.log(redis.LOG_WARNING, "startTime from Redis: " .. tostring(startTime))
redis.log(redis.LOG_WARNING, "endTime from Redis: " .. tostring(endTime))

if not startTime or not endTime then
    return -2  -- 이벤트 정보 없음
end

-- 문자열을 숫자로 변환 시도
local startTimeNum = tonumber(startTime)
local endTimeNum = tonumber(endTime)

if not startTimeNum or not endTimeNum then
    return -2  -- 시간 형식 오류
end

redis.log(redis.LOG_WARNING, "currentTime: " .. tostring(currentTime))
redis.log(redis.LOG_WARNING, "startTimeNum: " .. tostring(startTimeNum))
redis.log(redis.LOG_WARNING, "endTimeNum: " .. tostring(endTimeNum))

if currentTime < startTimeNum then
    return -3  -- 이벤트 시작 전
end

if currentTime > endTimeNum then
    return -4  -- 이벤트 종료됨
end

-- 중복 참여 체크
if redis.call('SISMEMBER', participantKey, userId) == 1 then
    return -1  -- 이미 참여함
end

-- 참여자 등록
redis.call('SADD', participantKey, userId)

-- 참여자 상세 데이터 저장
redis.call('SET', dataKey, jsonData)

return 1  -- 성공