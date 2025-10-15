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

	put(url, data, config) {
		return this.axiosInstance.put(url, data, config);
	}

	delete(url, config) {
		return this.axiosInstance.delete(url, config);
	}

	// Add more methods as needed
}

const instance = new FetchingService();
Object.freeze(instance);

export default instance;