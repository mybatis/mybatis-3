package org.apache.ibatis.executor.flush;

import org.apache.ibatis.executor.BatchResult;

import java.util.List;

/**
 * @author : Daniel Kvasnička
 * @inheritDoc
 * @since : 21.01.2021
 **/
public interface FlushResultHandler {

    List<BatchResult> handleResults();

}
