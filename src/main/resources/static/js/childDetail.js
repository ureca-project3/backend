const CHILD_ID = 1;

function setIndicatorPosition(score) {
    const clampedScore = Math.max(0, Math.min(100, score));
    return `${clampedScore}%`;
}

function updateIndicator(barId, score) {
    const barElement = document.getElementById(barId);
    if (!barElement) return;

    const colors = {
        'energy-bar': ['#FF99CC', '#FFCECE'],
        'perception-bar': ['#FFE5A3', '#C8E6C9'],
        'judgment-bar': ['#B2DFDB', '#E8BBB8'],
        'lifestyle-bar': ['#FFCDD2', '#D1C4E9']
    };

    barElement.innerHTML = `
            <div style="position: absolute; left: 0; width: 50%; height: 100%; background-color: ${colors[barId][0]}; border-radius: 12px 0 0 12px;"></div>
            <div style="position: absolute; right: 0; width: 50%; height: 100%; background-color: ${colors[barId][1]}; border-radius: 0 12px 12px 0;"></div>
            <div style="position: absolute; left: 50%; width: 2px; height: 100%; background-color: white; transform: translateX(-50%);"></div>
            <div class="indicator-circle" style="left: ${setIndicatorPosition(score)}; transition: left 0.3s ease"></div>
        `;
}

function updateHistoryDates(dates) {
    const container = document.getElementById('date-buttons');
    container.innerHTML = '<h3>진단 기록</h3>';

    dates.forEach((date, index) => {
        const button = document.createElement('div'); // div로 변경하여 스타일 적용
        button.className = 'history-date';
        if (index === 0) button.classList.add('active'); // 첫 번째 버튼에 active 클래스 추가

        const formattedDate = new Date(date).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
        button.textContent = formattedDate;

        // 클릭 이벤트 핸들러
        button.onclick = () => {
            // 모든 버튼에서 active 클래스 제거
            document.querySelectorAll('.history-date').forEach(btn =>
                btn.classList.remove('active')
            );
            // 클릭한 버튼에 active 클래스 추가
            button.classList.add('active');
            // 날짜 데이터 로드
            loadDateData(date);
        };

        // 컨테이너에 버튼 추가
        container.appendChild(button);
    });
}


async function loadInitialData() {
    try {
        const response = await fetch(`/mypage/child-info/result/${CHILD_ID}`);
        const data = await response.json();

        if (data.message === "Get MyChildTestHistory Success") {
            // 처음 한 번만 description 설정
            data.data.historyMbti.forEach(trait => {
                let descriptionId;
                switch (trait.traitName) {
                    case "에너지방향":
                        descriptionId = 'energy-description';
                        break;
                    case "인식기능":
                        descriptionId = 'perception-description';
                        break;
                    case "판단기능":
                        descriptionId = 'judgment-description';
                        break;
                    case "생활양식":
                        descriptionId = 'lifestyle-description';
                        break;
                    default:
                        return;
                }
                document.getElementById(descriptionId).textContent = trait.traitDescription;
            });

            updateDisplay(data.data);
            updateHistoryDates(data.data.historyDate);
        }
    } catch (error) {
        console.error('Error loading initial data:', error);
    }
}

function updateDisplay(data) {
    document.getElementById('personality-code').textContent = data.currentMbti;
    document.getElementById('personality-description').textContent = data.mbtiPhrase;
    document.getElementById('mbti-full-description').textContent = data.mbtiDescription;
    document.getElementById('mbti-image').src = `/images/${data.mbtiImage}`;

    data.historyMbti.forEach(trait => {
        const score = trait.traitScore;
        let barId;

        switch (trait.traitName) {
            case "에너지방향":
                barId = 'energy-bar';
                break;
            case "인식기능":
                barId = 'perception-bar';
                break;
            case "판단기능":
                barId = 'judgment-bar';
                break;
            case "생활양식":
                barId = 'lifestyle-bar';
                break;
            default:
                return;
        }

        updateIndicator(barId, score);
    });

    document.getElementById('delete-button').onclick = () => deleteHistory(data.historyId);
}

async function loadDateData(date) {
    try {
        const response = await fetch(`/mypage/child-info/result/history/${CHILD_ID}?date=${date}`);
        const data = await response.json();

        if (data.message === "Get MyChildTestHistory Success") {
            const historyData = data.data;

            document.getElementById('personality-code').textContent = historyData.currentMbti;
            document.getElementById('personality-description').textContent = historyData.mbtiPhrase;
            document.getElementById('mbti-full-description').textContent = historyData.mbtiDescription;
            document.getElementById('mbti-image').src = `/images/${historyData.mbtiImage}`;

            const historyMbti = historyData.historyMbti;
            updateIndicator('energy-bar', historyMbti["에너지방향"]);
            updateIndicator('perception-bar', historyMbti["인식기능"]);
            updateIndicator('judgment-bar', historyMbti["판단기능"]);
            updateIndicator('lifestyle-bar', historyMbti["생활양식"]);
        }
    } catch (error) {
        console.error('Error loading date data:', error);
    }
}

async function deleteHistory(historyId) {
    const isConfirmed = confirm("정말 삭제하시겠습니까?");

    if (!isConfirmed) {
        return; // If user cancels, exit the function
    }
    try {
        const response = await fetch(`/mypage/child-info/${historyId}`, {
            method: 'PATCH',
            headers: {
                'Child-Id': CHILD_ID.toString()
            }
        });
        const data = await response.json();

        if (data.message === "Delete MyChildHistory Success") {
            loadInitialData();
        }
    } catch (error) {
        console.error('Error deleting history:', error);
    }
}

// Initialize the page
loadInitialData();