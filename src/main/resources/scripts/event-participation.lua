-- 키 정의
local participantKey = KEYS[1]    -- 참여자 Set
local dataKey = KEYS[2]          -- 참여자 상세 데이터
local startTimeKey = KEYS[3]      -- 이벤트 시작 시간
local endTimeKey = KEYS[4]        -- 이벤트 마감 시간

-- 파라미터
local userId = ARGV[1]
local currentTime = tonumber(ARGV[2])  -- 현재 시간 (timestamp)
local jsonData = ARGV[3]

-- 이벤트 시간 검증
local startTime = redis.call('GET', startTimeKey)
local endTime = redis.call('GET', endTimeKey)

if not startTime or not endTime then
    return -2  -- 이벤트 정보 없음
end

startTime = tonumber(startTime)
endTime = tonumber(endTime)

if currentTime < startTime then
    return -3  -- 이벤트 시작 전
end

if currentTime > endTime then
    return -4  -- 이벤트 종료됨
end

-- 중복 참여 체크
if redis.call('SISMEMBER', participantKey, userId) == 1 then
    return -1  -- 이미 참여함
end

-- 참여자 등록 (Set)
redis.call('SADD', participantKey, userId)

-- 참여자 상세 데이터 저장
redis.call('SET', dataKey, jsonData)

return 1  -- 성공