import axios from "axios";

class FetchingService {
	constructor() {
		if (!FetchingService.instance) {
					// Determine base URL based on environment
					const baseURL = this.getBaseURL();
					this.axiosInstance = axios.create({
						withCredentials: true,
						baseURL: baseURL,
						// You can add more default config here
					});
			FetchingService.instance = this;
		}
		return FetchingService.instance;
	}

	getBaseURL() {
		// Use environment variable if available (recommended for production)
		if (import.meta.env.VITE_API_BASE_URL) {
			return import.meta.env.VITE_API_BASE_URL;
		}
		
		// Fallback: use the same origin as the frontend
		// This works for both localhost and production domains
		if (typeof window !== "undefined") {
			const protocol = window.location.protocol; // http: or https:
			const host = window.location.host; // includes port
			return `${protocol}//${host}/`;
		}
		
		// Final fallback (should rarely be needed)
		return "https://localhost:8080/";
	}

	get(url, config) {
		return this.axiosInstance.get(url, config);
	}

	post(url, data, config) {
		return this.axiosInstance.post(url, data, config);
	}

	// Convenience helper: send POST with query params
	// Usage: postWithParams('/api/thing', payload, { foo: 'bar' }, { headers: {...} })
	postWithParams(url, data, params = {}, config = {}) {
		const merged = { ...config, params };
		return this.axiosInstance.post(url, data, merged);
	}

	put(url, data, config) {
		return this.axiosInstance.put(url, data, config);
	}

	delete(url, config) {
		return this.axiosInstance.delete(url, config);
	}

	patch(url, data, config) {
		return this.axiosInstance.patch(url, data, config);
	}

	// Add more methods as needed
}

const instance = new FetchingService();
Object.freeze(instance);

export default instance;