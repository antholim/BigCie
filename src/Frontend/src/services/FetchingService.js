import axios from "axios";

class FetchingService {
	constructor() {
		if (!FetchingService.instance) {
					this.axiosInstance = axios.create({
						withCredentials: true,
						baseURL: "http://localhost:8080/",
						// You can add more default config here
					});
			FetchingService.instance = this;
		}
		return FetchingService.instance;
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