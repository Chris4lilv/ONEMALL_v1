package com.ONEMALL.service.Impl;

import com.ONEMALL.common.ServerResponse;
import com.ONEMALL.dao.ShippingMapper;
import com.ONEMALL.pojo.Shipping;
import com.ONEMALL.service.IShippingService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("Address added", result);
        }
        return ServerResponse.createByErrorMessage("Failed add address");
    }

    public ServerResponse<String> delete(Integer userId, Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if(resultCount > 0){
            return ServerResponse.createBySuccess("Address deleted");
        }
        return ServerResponse.createByErrorMessage("Failed delete address");
    }

    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("Address updated");
        }
        return ServerResponse.createByErrorMessage("Failed update address");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
        if(shipping == null){
            return ServerResponse.createByErrorMessage("Failed select this address, maybe it doesn't exist");
        }
        return ServerResponse.createBySuccess("Address selected", shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo =  new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
