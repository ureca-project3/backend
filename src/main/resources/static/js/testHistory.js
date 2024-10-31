// 페이지 로드 시 데이터 가져오기
document.addEventListener('DOMContentLoaded', function() {

    const childId = sessionStorage.getItem('currentChildId');

    if (childId) {
        fetchChildInfo(childId);
    } else {
        console.error('Child ID not provided in URL');
    }
});

// 자녀 정보 가져오기
async function fetchChildInfo(childId) {
    try {
        const response = await fetch(`/mypage/child-info/${childId}`);
        if (!response.ok) throw new Error('Network response was not ok');

        const result = await response.json();
        if (result.message === "Get MyChildInfo Success") {
            updateUI(result.data);
        }
    } catch (error) {
        console.error('Error fetching child info:', error);
    }
}

// UI 업데이트 함수
function updateUI(data) {
    // 프로필 정보 업데이트
    updateProfile(data);

    // MBTI 결과 업데이트
    updateMBTIResult(data.currentMbti);

    // MBTI 지표 업데이트
    updateIndicators(data.historyMbti);

    // 히스토리 날짜 업데이트
    updateHistoryDates(data.historyDate);
}

// 프로필 정보 업데이트
function updateProfile(data) {
    const profileInfo = document.getElementById('profileInfo');
    profileInfo.innerHTML = `
        <span>이름: ${data.name}</span>
        <span>생년월일: ${data.birthDate}</span>
        <span>나이: ${data.age}세</span>
        <span>성별: ${data.gender === 'F' ? '여아' : '남아'}</span>
    `;

    // 프로필 이미지 업데이트
    const profileImage = document.getElementById('profileImage');
    if (data.profileImage) {
        profileImage.innerHTML = `<img src="/images/${data.profileImage}" alt="프로필 사진">`;
    }
}

// MBTI 결과 업데이트
function updateMBTIResult(mbti) {
    const mbtiElement = document.getElementById('mbtiResult');
    const oldMbti = mbtiElement.textContent.trim();

    // MBTI 설정
    mbtiElement.textContent = mbti;

    // MBTI가 변경되었는지 확인
    if (oldMbti && oldMbti !== mbti) {
        // 변경 효과 적용
        mbtiElement.classList.add('changed');

        // 3초 후 효과 제거
        setTimeout(() => {
            mbtiElement.classList.remove('changed');
        }, 3000);
    }
}

// MBTI 지표 업데이트
function updateIndicators(mbtiScores) {
    document.getElementById('energyIndicator').style.left = `${mbtiScores['에너지방향']}%`;
    document.getElementById('perceptionIndicator').style.left = `${mbtiScores['인식기능']}%`;
    document.getElementById('judgmentIndicator').style.left = `${mbtiScores['판단기능']}%`;
    document.getElementById('lifestyleIndicator').style.left = `${mbtiScores['생활양식']}%`;
}

function updateHistoryDates(dates) {
    const historyContainer = document.getElementById('historyDates');
    const datesHtml = dates.map((date, index) => {
        const formattedDate = new Date(date).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
        // 첫 번째 날짜에 active 클래스 추가
        const activeClass = index === 0 ? ' active' : '';
        return `<div class="history-date${activeClass}" onclick="loadHistoryData('${date}'); setActiveDate(this)">${formattedDate}</div>`;
    }).join('');

    historyContainer.innerHTML = `<h3>성향 변경 히스토리</h3>${datesHtml}`;

    // 첫 번째 날짜 클릭 이벤트 호출 (선택적으로)
    if (dates.length > 0) {
        loadHistoryData(dates[0]); // 첫 번째 날짜에 대해 데이터 로드
    }
}

// 클릭한 날짜에 active 클래스 추가 및 다른 날짜에서 제거
function setActiveDate(clickedDate) {
    // 모든 날짜 요소에서 active 클래스 제거
    document.querySelectorAll('.history-date').forEach(elem => {
        elem.classList.remove('active');
    });
    // 클릭한 요소에 active 클래스 추가
    clickedDate.classList.add('active');
}


// 특정 날짜의 히스토리 데이터 로드
async function loadHistoryData(date) {
    const childId = sessionStorage.getItem('currentChildId');
    try {
        const response = await fetch(`/mypage/child-info/history/${childId}?date=${date}`);
        if (!response.ok) throw new Error('Network response was not ok');

        const result = await response.json();
        if (result.message === "Get MyChildHistory Success") {
            // MBTI 결과와 지표 업데이트
            updateMBTIResult(result.data.currentMbti);
            updateIndicators(result.data.historyMbti);
        }
    } catch (error) {
        console.error('Error loading history data:', error);
    }
}

// 상세 페이지로 이동
function goToDetailPage() {
    window.location.href = `/childDetail.html`;
}