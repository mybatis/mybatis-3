package org.apache.ibatis.submitted.blobtest;

import java.util.List;

public interface BlobMapper {
    int insert(BlobRecord blobRecord);
    List<BlobRecord> selectAll();
}
