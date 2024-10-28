let currentPage = 0;
const pageSize = 10;
const totalQuestions = 20;
const answers = {};
let allQuestions = {};

// 점수값과 인덱스 매핑을 위한 상수
const SCORE_VALUES = [-3, -2, -1, 1, 2, 3];

function fetchQuestions() {
    fetch(`/test/1?size=${totalQuestions}`)
        .then(response => response.json())
        .then(data => {
            if (data.data) {
                document.getElementById('test-title').textContent = data.data.name;
                document.getElementById('test-description').textContent = data.data.description;
                allQuestions = data.data.question;

                displayCurrentPage();
            }
        })
        .catch(error => console.error('Error:', error));
}

function displayCurrentPage() {
    const questionsDiv = document.getElementById('questions');
    questionsDiv.innerHTML = '';

    const start = currentPage * pageSize;
    const end = start + pageSize;
    const currentQuestions = Object.entries(allQuestions).slice(start, end);

    currentQuestions.forEach(([questionId, questionText]) => {
        const questionElement = document.createElement('div');
        questionElement.className = 'question';

        const questionTextDiv = document.createElement('div');
        questionTextDiv.className = 'question-text';
        questionTextDiv.textContent = questionText;
        questionElement.appendChild(questionTextDiv);

        const scoreContainer = document.createElement('div');
        scoreContainer.className = 'score-container';

        const scoreLabels = document.createElement('div');
        scoreLabels.className = 'score-labels';
        scoreLabels.innerHTML = '<span>매우 그렇다 않다</span><span>매우 그렇다</span>';
        scoreContainer.appendChild(scoreLabels);

        const scoreSelection = document.createElement('div');
        scoreSelection.className = 'score-selection';
        scoreSelection.setAttribute('data-question-id', questionId);

        for (let i = 1; i <= 6; i++) {
            const circle = document.createElement('button');
            circle.className = 'circle';
            // 저장된 점수값을 인덱스로 변환하여 선택 상태 표시
            const storedScore = answers[questionId];
            if (storedScore !== undefined) {
                const storedIndex = SCORE_VALUES.indexOf(storedScore) + 1;
                if (storedIndex === i) {
                    circle.classList.add('selected');
                }
            }
            circle.addEventListener('click', () => selectScore(questionId, i, circle));
            scoreSelection.appendChild(circle);
        }

        scoreContainer.appendChild(scoreSelection);
        questionElement.appendChild(scoreContainer);
        questionsDiv.appendChild(questionElement);
    });

    updateNavigation();
    updateProgress();
    window.scrollTo(0, 0);
}

function selectScore(questionId, index, selectedCircle) {
    const score = SCORE_VALUES[index - 1];

    const parentSelection = selectedCircle.parentElement;
    const circles = parentSelection.querySelectorAll('.circle');
    circles.forEach(circle => circle.classList.remove('selected'));
    selectedCircle.classList.add('selected');
    answers[questionId] = score;
    updateProgress();
    updateNavigation();
}

function updateNavigation() {
    const prevButton = document.getElementById('prev-button');
    const nextButton = document.getElementById('next-button');

    prevButton.disabled = currentPage === 0;

    if (currentPage === 1) {
        nextButton.textContent = '제출하기';
        const totalAnswered = Object.keys(answers).length;
        nextButton.disabled = totalAnswered < totalQuestions;
    } else {
        nextButton.textContent = '다음';
        nextButton.disabled = false;
    }

    document.getElementById('current-page').textContent = currentPage + 1;
}

function updateProgress() {
    const answeredCount = Object.keys(answers).length;
    const progressPercent = (answeredCount / totalQuestions) * 100;
    document.getElementById('progress').style.width = `${progressPercent}%`;
    document.getElementById('answered-count').textContent = answeredCount;
}

document.getElementById('prev-button').addEventListener('click', () => {
    if (currentPage > 0) {
        currentPage--;
        displayCurrentPage();
    }
});

document.getElementById('next-button').addEventListener('click', () => {
    if (currentPage === 1) {
        const answerList = Object.entries(answers).map(([questionId, score]) => ({
            [questionId]: score
        }));

        fetch('/test/1', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Child-Id': 1
            }
        }).catch(error => {
            console.error('Error:', error);
            alert('제출 중 오류가 발생했습니다. 다시 시도해주세요.');
        });

        fetch('/test/result/1', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Child-Id': 1
            },
            body: JSON.stringify({ answerList })
        })
            .then(response => response.json())
            .then(data => {
                alert('테스트가 완료되었습니다!');
                window.location.href = 'testResult.html';
            })
            .catch(error => {
                console.error('Error:', error);
                alert('제출 중 오류가 발생했습니다. 다시 시도해주세요.');
            });
    } else {
        currentPage++;
        displayCurrentPage();
    }
});

// 초기 질문 로드
fetchQuestions();