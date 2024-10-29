function setIndicatorPosition(score) {
    return `${score}%`;
}

function createTraitIndicator(trait) {
    const indicatorDiv = document.createElement('div');
    indicatorDiv.className = 'indicator';

    let labels;
    let colors;
    switch(trait.traitName) {
        case '에너지방향':
            labels = ['E', 'I'];
            colors = ['#FF99CC', '#FFCECE'];
            break;
        case '인식기능':
            labels = ['S', 'N'];
            colors = ['#FFE5A3', '#C8E6C9'];
            break;
        case '판단기능':
            labels = ['T', 'F'];
            colors = ['#B2DFDB', '#E8BBB8'];
            break;
        case '생활양식':
            labels = ['J', 'P'];
            colors = ['#FFCDD2', '#D1C4E9'];
            break;
    }

    indicatorDiv.innerHTML = `
                <div class="indicator-labels">
                    <span>${labels[0]}</span>
                    <span>${labels[1]}</span>
                </div>
                <div class="indicator-bar" style="background: linear-gradient(to right, ${colors[0]} 0%, ${colors[0]} 50%, ${colors[1]} 50%, ${colors[1]} 100%);">
                    <div class="indicator-circle" style="left: ${setIndicatorPosition(trait.traitScore)}"></div>
                </div>
                <div class="trait-description">${trait.traitDescription}</div>
            `;

    return indicatorDiv;
}

async function fetchTestResult() {
    try {
        const response = await fetch('/test/result', {
            headers: {
                'Child-Id': '1'
            }
        });
        const data = await response.json();
        return data.data;
    } catch (error) {
        console.error('Error fetching result:', error);
    }
}

async function initializeResult() {
    try {
        const result = await fetchTestResult();

        document.getElementById('resultTitle').textContent = `성향 진단 결과`;

        const resultCircle = document.getElementById('resultCircle');
        resultCircle.innerHTML = `<img src="/images/${result.mbtiImage}" alt="${result.mbtiName}">`;

        document.getElementById('mbtiType').textContent = result.mbtiName;
        document.getElementById('mbtiPhrase').textContent = result.mbtiPhrase;
        document.getElementById('mbtiDescription').textContent = result.mbtiDescription;

        const traitContainer = document.getElementById('traitContainer');
        result.traitDataResponseDtoList.forEach(trait => {
            traitContainer.appendChild(createTraitIndicator(trait));
        });

        const currentDate = new Date();
        document.getElementById('resultDate').textContent = `${currentDate.getFullYear()}년 ${currentDate.getMonth() + 1}월 ${currentDate.getDate()}일`;

    } catch (error) {
        console.error('Error initializing result:', error);
    }
}

window.addEventListener('load', initializeResult);