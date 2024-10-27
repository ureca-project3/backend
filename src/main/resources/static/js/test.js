
let currentPage = 0;
const pageSize = 10;
const totalQuestions = 20;
const answers = {};
let allQuestions = {};

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
    if (answers[questionId] === i) {
    circle.classList.add('selected');
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

    // 페이지를 맨 위로 스크롤
    window.scrollTo(0, 0);
}


    function updateNavigation() {
    const prevButton = document.getElementById('prev-button');
    const nextButton = document.getElementById('next-button');

    prevButton.disabled = currentPage === 0;

    if (currentPage === 1) {
    nextButton.textContent = '제출하기';
} else {
    nextButton.textContent = '다음';
}

    // 마지막 페이지에서는 모든 문항이 답변되어야 제출 버튼이 활성화됨
    if (currentPage === 1) {
    const totalAnswered = Object.keys(answers).length;
    nextButton.disabled = totalAnswered < totalQuestions;
} else {
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

    function selectScore(questionId, index, selectedCircle) {
    const scoreValues = [-3, -2, -1, 1, 2, 3]; // 각 버튼의 실제 점수값
    const score = scoreValues[index - 1]; // index는 1부터 시작하므로 1을 빼줍니다

    const parentSelection = selectedCircle.parentElement;
    const circles = parentSelection.querySelectorAll('.circle');
    circles.forEach(circle => circle.classList.remove('selected'));
    selectedCircle.classList.add('selected');
    answers[questionId] = score; // 실제 점수값 저장
    updateProgress();
    updateNavigation();
}

    document.getElementById('prev-button').addEventListener('click', () => {
    if (currentPage > 0) {
    currentPage--;
    displayCurrentPage();
}
});

    document.getElementById('next-button').addEventListener('click', () => {
    if (currentPage === 1) {
    // 제출 데이터 형식 변환
    const answerList = Object.entries(answers).map(([questionId, score]) => ({
    [questionId]: score
}));

    // 서버로 전송 (첫 번째 요청은 삭제하거나 필요한 경우 수정)
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

    // 서버로 답변 제출
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
    // 결과 페이지로 이동
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