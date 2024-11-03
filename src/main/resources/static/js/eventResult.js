function fetchEventResult() {
    // URL에서 이벤트 ID 가져오기
    const eventId = new URLSearchParams(window.location.search).get('eventId') || 1;

    fetch(`/event/api/result/${eventId}`)
        .then(response => response.json())
        .then(response => {
            if (response.data) {
                displayEventResult(response.data);
            } else {
                throw new Error('데이터가 없습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('결과를 불러오는 중 오류가 발생했습니다. 다시 시도해주세요.');
        });
}

function displayEventResult(eventData) {
    // 이벤트 제목 설정
    document.getElementById('event-title').textContent = eventData.eventName;

    // 이벤트 정보 설정
    document.getElementById('start-time').textContent = formatDateTime(eventData.startTime);
    document.getElementById('end-time').textContent = formatDateTime(eventData.endTime);
    document.getElementById('announce-time').textContent = formatDateTime(eventData.announceTime);
    document.getElementById('winner-count').textContent = `${eventData.winnerCnt}명`;

    // 당첨자 목록 표시
    const winnersListElement = document.getElementById('winners-list');
    winnersListElement.innerHTML = '';

    eventData.winnerList.forEach(winner => {
        const winnerElement = document.createElement('div');
        winnerElement.className = 'winner-item';

        const winnerInfo = document.createElement('div');
        winnerInfo.className = 'winner-info';

        // 이름 마스킹 처리
        const nameElement = document.createElement('div');
        nameElement.className = 'winner-name';
        nameElement.textContent = maskName(winner.winnerName);

        // 전화번호 마스킹 처리
        const phoneElement = document.createElement('div');
        phoneElement.className = 'winner-phone';
        phoneElement.textContent = maskPhoneNumber(winner.phoneNumber);

        winnerInfo.appendChild(nameElement);
        winnerInfo.appendChild(phoneElement);
        winnerElement.appendChild(winnerInfo);
        winnersListElement.appendChild(winnerElement);
    });
}

function formatDateTime(dateTimeString) {
    const dateTime = new Date(dateTimeString);
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
    }).format(dateTime);
}

function maskName(name) {
    if (name.length <= 2) {
        return name.charAt(0) + '*';
    }
    return name.charAt(0) + '*'.repeat(name.length - 2) + name.charAt(name.length - 1);
}

function maskPhoneNumber(phoneNumber) {
    return phoneNumber.replace(/(\d{3})(\d{4})(\d{4})/, '$1-****-$3');
}

// 페이지 로드 시 결과 데이터 가져오기
document.addEventListener('DOMContentLoaded', fetchEventResult);