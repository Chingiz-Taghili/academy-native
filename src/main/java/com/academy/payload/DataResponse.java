package com.academy.payload;

public class DataResponse<T> extends ApiResponse {

    private final T data;

    public DataResponse(T data) {
        super(true);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}