package com.wangjh.controller;



import com.wangjh.entity.Test;
import com.wangjh.service.TestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * (Test)表控制层
 *
 * @author makejava
 * @since 2023-09-07 14:58:41
 */
@RestController
@RequestMapping("test")
public class TestController {
    /**
     * 服务对象
     */
    @Resource
    private TestService testService;


    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public ResponseEntity<Test> queryById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(this.testService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param test 实体
     * @return 新增结果
     */
    @PostMapping
    public ResponseEntity<Test> add(Test test) {
        return ResponseEntity.ok(this.testService.insert(test));
    }

    /**
     * 编辑数据
     *
     * @param test 实体
     * @return 编辑结果
     */
    @PutMapping
    public ResponseEntity<Test> edit(Test test) {
        return ResponseEntity.ok(this.testService.update(test));
    }

    /**
     * 删除数据
     *
     * @param id 主键
     * @return 删除是否成功
     */
    @DeleteMapping
    public ResponseEntity<Boolean> deleteById(Integer id) {
        return ResponseEntity.ok(this.testService.deleteById(id));
    }

}

