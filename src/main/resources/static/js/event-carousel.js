// 이벤트 데이터를 가져오는 함수
async function fetchEvents() {
    try {
        const response = await fetch('/event/api/list');
        if (!response.ok) throw new Error('이벤트 목록을 가져오는데 실패했습니다.');
        const data = await response.json();

        // 각 이벤트에 대해 질문 데이터 가져오기
        const eventsWithQuestions = await Promise.all(data.data.map(async (event) => {
            try {
                const questionResponse = await fetch(`/event/api/${event.eventId}/question`);
                if (!questionResponse.ok) throw new Error('이벤트 질문을 가져오는데 실패했습니다.');
                const questionData = await questionResponse.json();
                return {
                    ...event,
                    // API 응답 구조에 맞게 수정
                    questionText: questionData.data.eventQText // eventQText 속성 사용
                };
            } catch (error) {
                console.error(`이벤트 ${event.eventId}의 질문 로딩 실패:`, error);
                return {
                    ...event,
                    questionText: '설문에 참여하고 선물 받아가세요!' // 기본 텍스트
                };
            }
        }));

        return eventsWithQuestions || [];
    } catch (error) {
        console.error('이벤트 데이터 로딩 실패:', error);
        return [];
    }
}
// 이벤트 상태 결정 함수
function getEventStatus(startDate, endDate) {
    const now = new Date();
    const start = new Date(startDate);
    const end = new Date(endDate);

    if (now < start) return '시작전';
    if (now > end) return '마감';
    return '진행중';
}

// 이벤트 배너 HTML 생성
function createEventBanner(event) {
    const status = getEventStatus(event.startTime, event.endTime, event.announceTime);
    const statusClass = {
        '시작전': 'upcoming',
        '진행중': 'ongoing',
        '마감': 'ended'
    }[status];

    // 날짜 포맷팅
    const formatDateTime = (date) => {
        return new Date(date).toLocaleString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        });
    };

    const formattedStartDate = formatDateTime(event.startTime);
    const formattedEndDate = formatDateTime(event.endTime);
    const formattedAnnounceDate = formatDateTime(event.announceTime);

    // 버튼 클릭 핸들러 설정
    const handleButtonClick = status === '마감'
        ? `handleResultButtonClick(${event.eventId}, '${event.announceTime}')`
        : `handleApplyButtonClick(${event.eventId}, '${event.startTime}')`;

    const buttonText = status === '마감' ? '응모 결과 보러가기' : '이벤트 응모하러 가기';
    const questionText = event.questionText || '설문에 참여하고 선물 받아가세요!';


    return `
    <div class="carousel-item">
        <div class="event-content">
            <div class="event-image-container">
                <img src="/image/cat_on_books.png" alt="이벤트 이미지" class="event-image">
            </div>
            <div class="event-info">
                <div class="event-title-container">
                    <h2 class="event-title">${event.eventName}</h2>
                    <div class="event-badge ${statusClass}">${status}</div>
                </div>
                <p class="event-question">${questionText}</p>
                <div class="event-datetime">
                    <div class="datetime-row">
                        <span class="label">이벤트 기간</span>
                        <span class="date">${formattedStartDate} ~ ${formattedEndDate}</span>
                    </div>
                    <div class="datetime-row">
                        <span class="label">당첨자 발표</span>
                        <span class="date">${formattedAnnounceDate}</span>
                    </div>
                </div>
                <button class="event-button" onclick="${handleButtonClick}">
                    ${buttonText}
                </button>
            </div>
        </div>
    </div>
`;
}

// 이벤트 응모하기 버튼 클릭 핸들러
function handleApplyButtonClick(eventId, startTime) {
    const now = new Date();
    const start = new Date(startTime);

    if (now < start) {
        showModal('아직 이벤트 시작시간이 아닙니다');
        return;
    }

    window.location.href = `/event.html?eventId=${eventId}`;
}

// 결과 보기 버튼 클릭 핸들러
function handleResultButtonClick(eventId, announceTime) {
    const now = new Date();
    const announce = new Date(announceTime);

    if (now < announce) {
        showModal('아직 응모결과가 나오지 않았습니다');
        return;
    }

    window.location.href = `/eventResult.html?eventId=${eventId}`;
}

// 모달 표시 함수
function showModal(message) {
    const modalHTML = `
        <div id="event-modal" class="modal">
            <div class="modal-content">
                <p>${message}</p>
                <button onclick="closeModal()">확인</button>
            </div>
        </div>
    `;

    // 기존 모달이 있다면 제거
    const existingModal = document.getElementById('event-modal');
    if (existingModal) {
        existingModal.remove();
    }

    // 새 모달 추가
    document.body.insertAdjacentHTML('beforeend', modalHTML);
}

// 모달 닫기 함수
function closeModal() {
    const modal = document.getElementById('event-modal');
    if (modal) {
        modal.remove();
    }
}

// 캐러셀 초기화 및 이벤트 설정
function initializeCarousel() {
    let currentSlide = 0;
    const carousel = document.querySelector('.carousel');
    const items = carousel.querySelectorAll('.carousel-item');
    const indicators = carousel.querySelector('.carousel-indicators');

    // 인디케이터 생성
    for (let i = 0; i < items.length; i++) {
        const indicator = document.createElement('div');
        indicator.className = `carousel-indicator ${i === 0 ? 'active' : ''}`;
        indicator.addEventListener('click', () => {
            currentSlide = i;
            showSlide(currentSlide);
        });
        indicators.appendChild(indicator);
    }

    function showSlide(index) {
        items.forEach(item => item.style.display = 'none');
        items[index].style.display = 'block';

        // 인디케이터 업데이트
        const indicators = carousel.querySelectorAll('.carousel-indicator');
        indicators.forEach((indicator, i) => {
            indicator.classList.toggle('active', i === index);
        });
    }

    function nextSlide() {
        currentSlide = (currentSlide + 1) % items.length;
        showSlide(currentSlide);
    }

    function previousSlide() {
        currentSlide = (currentSlide - 1 + items.length) % items.length;
        showSlide(currentSlide);
    }

    // 자동 슬라이드 설정
    let autoSlideInterval;

    function startAutoSlide() {
        autoSlideInterval = setInterval(nextSlide, 5000);
    }

    function stopAutoSlide() {
        clearInterval(autoSlideInterval);
    }

// 마우스 오버시 자동 슬라이드 멈춤
    carousel.addEventListener('mouseenter', stopAutoSlide);
    carousel.addEventListener('mouseleave', startAutoSlide);

// 초기 자동 슬라이드 시작
    startAutoSlide();

    // 네비게이션 버튼 이벤트 리스너
    document.querySelector('.carousel-prev').addEventListener('click', previousSlide);
    document.querySelector('.carousel-next').addEventListener('click', nextSlide);

    // 초기 슬라이드 표시
    showSlide(0);
}

// 이벤트 페이지로 이동하는 함수
function navigateToEvent(eventId) {
    if (!eventId) return;
    window.location.href = `/event.html?eventId=${eventId}`;
}

// 페이지 로드 시 실행
window.addEventListener('DOMContentLoaded', async () => {
    const events = await fetchEvents();

    // 캐러셀 컨테이너 생성 및 설정
    const heroContainer = document.querySelector('.hero-container');
    heroContainer.innerHTML = `
        <div class="carousel">
            ${events.map(event => createEventBanner(event)).join('')}
            <div class="carousel-nav-container">
                <div class="carousel-indicators"></div>
                <button class="carousel-prev">❮</button>
                <button class="carousel-next">❯</button>
            </div>
        </div>
    `;

    initializeCarousel();
});