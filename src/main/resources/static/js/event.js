class EventPage {
    constructor() {
        this.eventId = new URLSearchParams(window.location.search).get('eventId');
        if (!this.eventId) {
            this.showModal('잘못된 접근입니다.');
            return;
        }

        this.eventData = null;
        this.answers = {};

        this.initElements();
        this.initEventListeners();
        this.loadEventData();
    }

    initElements() {
        this.form = document.getElementById('event-form');
        this.submitButton = document.querySelector('.submit-button');
        this.surveyContainer = document.getElementById('survey-container');
        this.modal = document.getElementById('event-modal');
        this.modalMessage = document.getElementById('modal-message');

        // 요소가 없는 경우 처리
        if (!this.form || !this.submitButton || !this.surveyContainer || !this.modal || !this.modalMessage) {
            console.error('Required elements not found');
            return;
        }
    }

    initEventListeners() {
        if (this.form) {
            this.form.addEventListener('submit', (e) => this.handleSubmit(e));
            this.form.addEventListener('input', () => this.validateForm());
        }
    }

    async loadEventData() {
        try {
            const response = await Api.get(`/event/${this.eventId}`);
            const result = await response.json();

            this.eventData = result.data;
            this.renderEventData();
            this.checkEventTime();
        } catch (error) {
            console.error('Error loading event data:', error);
            this.showModal('이벤트 정보를 불러오는데 실패했습니다.');
        }
    }

    renderEventData() {
        if (!this.eventData) return;

        const titleElement = document.getElementById('event-title');
        const timeElement = document.getElementById('event-time');
        const winnerElement = document.getElementById('winner-count');

        if (titleElement) {
            titleElement.textContent = this.eventData.eventName;
        }

        if (timeElement && winnerElement) {
            const startTime = new Date(this.eventData.startTime);
            const endTime = new Date(this.eventData.endTime);

            timeElement.textContent =
                `이벤트 기간: ${startTime.toLocaleString()} ~ ${endTime.toLocaleString()}`;

            winnerElement.textContent =
                `선착순 ${this.eventData.winnerCnt}명`;
        }

        this.renderSurveyQuestions();
    }

    renderSurveyQuestions() {
        if (!this.surveyContainer || !this.eventData.eventQuestion) return;

        this.surveyContainer.innerHTML = '';

        Object.entries(this.eventData.eventQuestion).forEach(([questionId, questionText]) => {
            const questionDiv = document.createElement('div');
            questionDiv.className = 'survey-question';
            questionDiv.innerHTML = `
                <p>${questionText}</p>
                <div class="survey-options">
                    <label class="survey-option">
                        <input type="radio" name="question_${questionId}" value="1" required>
                        <span>예</span>
                    </label>
                    <label class="survey-option">
                        <input type="radio" name="question_${questionId}" value="2" required>
                        <span>아니오</span>
                    </label>
                </div>
            `;

            questionDiv.querySelectorAll('input[type="radio"]').forEach(radio => {
                radio.addEventListener('change', (e) => {
                    this.answers[questionId] = parseInt(e.target.value);
                    this.validateForm();
                });
            });

            this.surveyContainer.appendChild(questionDiv);
        });
    }

    checkEventTime() {
        if (!this.eventData || !this.submitButton) return;

        const now = new Date();
        const startTime = new Date(this.eventData.startTime);
        const endTime = new Date(this.eventData.endTime);

        if (now < startTime) {
            this.showModal('아직 이벤트 시작 시간이 아닙니다');
            this.submitButton.disabled = true;
        } else if (now > endTime) {
            this.showModal('이벤트가 종료되었습니다');
            this.submitButton.disabled = true;
        }
    }

    validateForm() {
        if (!this.submitButton || !this.eventData) return;

        const name = document.getElementById('participant-name')?.value.trim();
        const phone = document.getElementById('participant-phone')?.value.trim();
        const allQuestionsAnswered = Object.keys(this.eventData.eventQuestion).length ===
            Object.keys(this.answers).length;

        this.submitButton.disabled = !name || !phone || !allQuestionsAnswered;
    }

    async handleSubmit(e) {
        e.preventDefault();

        const formData = {
            eventId: parseInt(this.eventId),
            name: document.getElementById('participant-name').value.trim(),
            phone: document.getElementById('participant-phone').value.trim(),
            answerList: Object.entries(this.answers).map(([questionId, answer]) => ({
                [questionId]: answer
            }))
        };

        try {
            const response = await Api.post('/event/participate', formData);
            const result = await response.json();

            this.showModal('이벤트 응모가 완료되었습니다.');
            if (this.submitButton) {
                this.submitButton.disabled = true;
            }
        } catch (error) {
            console.error('Error submitting event participation:', error);
            this.showModal(error.message || '이벤트 응모 중 오류가 발생했습니다.');
        }
    }

    showModal(message) {
        if (this.modalMessage && this.modal) {
            this.modalMessage.textContent = message;
            this.modal.style.display = 'flex';
        }
    }
}

function closeModal() {
    const modal = document.getElementById('event-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new EventPage();
});