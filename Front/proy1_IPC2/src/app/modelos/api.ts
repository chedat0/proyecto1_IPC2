export interface ApiResponse<T> {
    data?: T;
    mensaje?: string;
    error?: string;
}