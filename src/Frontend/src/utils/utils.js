export const formatDateTime = (value) => {
    if (!value) return "N/A";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
};

export const truncateId = (value) => {
    if (!value) return "N/A";
    const str = String(value);
    return `${str.slice(0, 8)}...`;
};