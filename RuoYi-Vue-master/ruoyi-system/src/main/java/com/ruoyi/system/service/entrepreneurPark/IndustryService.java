package com.ruoyi.system.service.entrepreneurPark;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.system.domain.entity.Industry;
import com.ruoyi.system.domain.vo.IndustryRankVO;

import java.util.List;
import java.util.Map;

public interface IndustryService extends IService<Industry> {

    boolean saveIndustry(Industry industry);    // 新增行业

    boolean updateIndustry(Industry industry);  // 更新行业信息

    boolean deleteIndustry(Integer industryId); // 删除行业

    boolean deleteBatch(List<Integer> industryIds);  // 批量删除行业

    IPage<Industry> getIndustriesPage(int page, int size);  // 分页查询行业列表

    List<IndustryRankVO> getIndustryRanking(int size);

    Map<Integer, Industry> getAllIndustriesMap();
}
