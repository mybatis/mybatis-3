package com.wangjh.service.impl;

import com.wangjh.entity.Test;
import com.wangjh.mapper.TestMapper;
import com.wangjh.service.TestService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (Test)表服务实现类
 *
 * @author makejava
 * @since 2023-09-07 14:59:01
 */
@Service
public class TestServiceImpl implements TestService {
    @Resource
    private TestMapper testDao;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Test queryById(Integer id) {
        return this.testDao.queryById(id);
    }


    /**
     * 新增数据
     *
     * @param test 实例对象
     * @return 实例对象
     */
    @Override
    public Test insert(Test test) {
        this.testDao.insert(test);
        return test;
    }

    /**
     * 修改数据
     *
     * @param test 实例对象
     * @return 实例对象
     */
    @Override
    public Test update(Test test) {
        this.testDao.update(test);
        return this.queryById(test.getId());
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return this.testDao.deleteById(id) > 0;
    }
}
