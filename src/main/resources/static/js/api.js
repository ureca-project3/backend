class Api {
    static async fetch(url, options = {}) {
        const accessToken = sessionStorage.getItem('accessToken');
        const currentChildId = sessionStorage.getItem('currentChildId');

        const defaultHeaders = {
            'Content-Type': 'application/json',
        };

        if (accessToken) {
            defaultHeaders['Authorization'] = `Bearer ${accessToken}`;
        }

        if (currentChildId) {
            defaultHeaders['X-Child-ID'] = currentChildId;
        }

        const finalOptions = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, finalOptions);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return response;
        } catch (error) {
            console.error('API request failed:', error);
            throw error;
        }
    }

    static async get(url) {
        return this.fetch(url, { method: 'GET' });
    }

    static async post(url, data) {
        return this.fetch(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }
}