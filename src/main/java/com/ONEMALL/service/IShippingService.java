package com.ONEMALL.service;


import com.ONEMALL.common.ServerResponse;
import com.ONEMALL.pojo.Shipping;
import com.github.pagehelper.PageInfo;

public interface IShippingService {

    ServerResponse add(Integer userId, Shipping shipping);
    ServerResponse<String> delete(Integer userId, Integer shippingId);
    ServerResponse update(Integer userId, Shipping shipping);
    ServerResponse<Shipping> select(Integer userId, Integer shippingId);
    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
