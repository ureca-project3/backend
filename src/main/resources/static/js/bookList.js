
let currentPage = 0;
const pageSize = 8;

// 책 목록을 가져오는 함수
async function fetchBooks(page) {
    try {
        const response = await fetch(`/books?page=${page}&size=${pageSize}`, {
            method: 'GET'
        });
        const data = await response.json();

        if (data.message === "Get BookList Success") {
            displayBooks(data.data);
            console.log("displayBooks");
        } else {
            console.error("Failed to fetch book list:", data.message);
        }
    } catch (error) {
        console.error("Error fetching book list:", error);
    }
}

// 책 목록을 화면에 표시하는 함수
function displayBooks(books) {
    const container = document.querySelector('.book-container');
    container.innerHTML = '';

    books.forEach(book => {
        const card = document.createElement('div');
        card.className = 'book-card';

        // 카드 클릭 시 책 상세 페이지로 이동
        card.addEventListener('click', () => {
            window.location.href = `/bookDetail.html?bookId=${book.bookId}`;
        });

        card.innerHTML = `
            <img src="${book.imageUrl}" alt="${book.title}" class="book-image" />
            <h2 class="book-title">${book.title}</h2>
            <p class="book-author">권장연령: ${book.recAge}</p>
            <p class="book-publisher">${book.publisher}</p>
        `;

        container.appendChild(card);
    });
}

// 페이지 변경 함수
function changePage(offset) {
    currentPage += offset;
    if (currentPage < 0) currentPage = 0;
    fetchBooks(currentPage);
    document.getElementById("page-number").textContent = currentPage + 1;
}

// 페이지 로드 시 초기 데이터 가져오기
window.onload = () => fetchBooks(currentPage);
