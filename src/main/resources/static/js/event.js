class EventPage {
    constructor() {
        this.eventId = new URLSearchParams(window.location.search).get('eventId');
        this.eventData = null;
        this.eventQuestions = null;
        this.answers = {};
        this.modal = null;
        this.modalMessage = null;

        this.createInitialModal();

        if (!this.eventId) {
            this.showModal('잘못된 접근입니다.');
            return;
        }

        this.initElements();
        this.initEventListeners();
        this.loadEventData();
    }

    createInitialModal() {
        const modalHTML = `
            <div id="event-modal" class="modal">
                <div class="modal-content">
                    <p id="modal-message"></p>
                    <button id="modal-confirm-button" class="modal-button" onclick="closeModal()">
                        확인
                    </button>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        this.modal = document.getElementById('event-modal');
        this.modalMessage = document.getElementById('modal-message');
    }

    initElements() {
        this.form = document.getElementById('event-form');
        this.submitButton = document.querySelector('.submit-button');
        this.surveyContainer = document.getElementById('survey-container');

        // 요소가 없는 경우 처리
        if (!this.form || !this.submitButton || !this.surveyContainer) {
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
            const eventResponse = await Api.get(`/event/api/${this.eventId}`);
            const eventResult = await eventResponse.json();
            this.eventData = eventResult.data;

            // 이벤트 질문 정보 가져오기
            const questionResponse = await Api.get(`/event/api/${this.eventId}/question`);
            const questionResult = await questionResponse.json();
            this.eventQuestions = questionResult.data;

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
        if (!this.surveyContainer || !this.eventQuestions) return;

        console.log('Rendering questions:', this.eventQuestions);

        this.surveyContainer.innerHTML = '';

        const questionDiv = document.createElement('div');
        questionDiv.className = 'survey-question';
        questionDiv.innerHTML = `
        <p>${this.eventQuestions.eventQText}</p>
        <div class="survey-options">
            <label class="survey-option">
                <input type="radio" name="question_${this.eventQuestions.eventQuestionId}" value="1" required>
                <span>예</span>
            </label>
            <label class="survey-option">
                <input type="radio" name="question_${this.eventQuestions.eventQuestionId}" value="2" required>
                <span>아니오</span>
            </label>
        </div>
    `;

        questionDiv.querySelectorAll('input[type="radio"]').forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.answers[this.eventQuestions.eventQuestionId] = parseInt(e.target.value);
                this.validateForm();
            });
        });

        this.surveyContainer.appendChild(questionDiv);
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
        if (!this.submitButton || !this.eventQuestions) return;

        const name = document.getElementById('participant-name')?.value.trim();
        const phone = document.getElementById('participant-phone')?.value.trim();
        // 배열인 경우를 처리
        const questionCount = Array.isArray(this.eventQuestions) ?
            this.eventQuestions.length :
            1;
        const allQuestionsAnswered = questionCount === Object.keys(this.answers).length;

        this.submitButton.disabled = !name || !phone || !allQuestionsAnswered;
    }

    async handleSubmit(e) {
        e.preventDefault();

        const accessToken = sessionStorage.getItem('accessToken');
        if (!accessToken) {
            this.showModalWithRedirect('로그인이 필요한 서비스입니다.', '/login.html');
            return;
        }

        try {
            // answerList 형식으로 변환
            const answerList = {};
            Object.entries(this.answers).forEach(([questionId, answer]) => {
                answerList[questionId] = answer;
            });

            const formData = {
                eventId: parseInt(this.eventId),
                name: document.getElementById('participant-name').value.trim(),
                phone: document.getElementById('participant-phone').value.trim(),
                answerList: answerList
            };

            console.log('Sending data:', formData);

            const response = await Api.post('/event/apply', formData);
            const result = await response.json();

            if (result.success) {
                this.showModalWithRedirect('이벤트 응모가 완료되었습니다.', '/index.html');
                if (this.submitButton) {
                    this.submitButton.disabled = true;
                }
            } else {
                this.showModal(result.message || '이벤트 응모에 실패했습니다.');
            }
        } catch (error) {
            console.error('Error submitting event participation:', error);
            if (error.message.includes('500')) {
                this.showModal('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            } else {
                this.showModal(error.message || '이벤트 응모 중 오류가 발생했습니다.');
            }
        }
    }

    showModal(message) {
        if (this.modalMessage) {
            this.modalMessage.textContent = message;
        }

        const confirmButton = document.getElementById('modal-confirm-button');
        if (confirmButton) {
            confirmButton.onclick = closeModal;
        }

        if (this.modal) {
            this.modal.style.display = 'flex';
        }
    }

    showModalWithRedirect(message, redirectUrl) {
        if (this.modalMessage) {
            this.modalMessage.textContent = message;
        }

        const oldButton = document.getElementById('modal-confirm-button');
        if (oldButton) {
            const newButton = oldButton.cloneNode(true);
            newButton.onclick = () => {
                window.location.href = redirectUrl;
            };
            oldButton.parentNode.replaceChild(newButton, oldButton);
        }
        if (this.modal) {
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