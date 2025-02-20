package com.ruoyi.web.controller.entrepreneurPark;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.AjaxResult;

import com.ruoyi.system.domain.entity.StudentEntrepreneurshipPark;
import com.ruoyi.system.domain.vo.*;
import com.ruoyi.system.service.entrepreneurPark.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/DP/")
@Api(tags = "大屏api")
public class DPController {

    @Autowired
    private PolicyArticleService policyArticleService;
    @Autowired
    private AwardDetailService awardDetailService;
    @Autowired
    private JobEnterprisePositionService jobEnterprisePositionService;
    @Autowired
    private IndustryService industryService;
    @Autowired
    private StudentEntrepreneurshipParkService parkService;
    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private ChatService chatService;

    @ApiOperation("获取文章列表（分页）")
    @GetMapping("/articleList")
    public Page<PolicyArticleVO> getPolicyArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @ApiParam(value = "分类名称") @RequestParam(required = false) String category,
            @ApiParam(value = "标签名称") @RequestParam(required = false) String primaryTag
    ) {
        return policyArticleService.getPolicyArticles1(page, size, category,primaryTag);
    }

    @ApiOperation("获取企业信息分类的文章列表（分页）")
    @GetMapping("/enterpriseCategoryArticleList")
    public Page<PolicyArticleVO> getEnterpriseCategoryArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @ApiParam(value = "企业名称") @RequestParam(required = false) String enterpriseName
    ) {
        return policyArticleService.getEnterpriseCategoryArticles(page, size, enterpriseName);
    }


    @ApiOperation("获取获奖总数")
    @GetMapping("/getTotalAwardCount")
    public AjaxResult getTotalAwardCount() {
        try {
            long totalAwardCount = awardDetailService.getTotalAwardCount();
            return AjaxResult.success(totalAwardCount);
        } catch (Exception e) {
            return AjaxResult.error("获取获奖总数失败，请稍后再试");
        }
    }

    @ApiOperation("获取获奖总补贴")
    @GetMapping("/getTotalSubsidyAmount")
    public AjaxResult getTotalSubsidyAmount() {
        try {
            BigDecimal totalSubsidyAmount = awardDetailService.getTotalSubsidyAmount();
            return AjaxResult.success(totalSubsidyAmount);
        } catch (Exception e) {
            return AjaxResult.error("获取获奖总补贴，请稍后再试");
        }
    }

    @ApiOperation("获取获奖类型对应的补贴金额")
    @GetMapping("/getAwardTypeSummary")
    public AjaxResult getAwardTypeSummary() {
        try {
            List<AwardTypeVO> awardTypeSummary = awardDetailService.getAwardTypeSummary();
            return AjaxResult.success(awardTypeSummary);
        } catch (Exception e) {
            return AjaxResult.error("获取不同类型获奖情况汇总失败，请稍后再试");
        }
    }

    @ApiOperation("分页查询获奖情况Integer typeId")
    @GetMapping("/getAwardDetailsPage")
    public Page<AwardDetailVO> getAwardDetailsPage(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "年份") @RequestParam(required = false) Integer year,
            @ApiParam(value = "比赛类型") @RequestParam(required = false) Integer typeId) {
        return awardDetailService.getAwardDetailsPage(page, size, year, typeId);
    }

    @ApiOperation("分页查询获奖情况String type")
    @GetMapping("/getAwardDetailsPage1")
    public Page<AwardDetailVO> getAwardDetailsPage1(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @ApiParam(value = "年份") @RequestParam(required = false) Integer year,
            @ApiParam(value = "比赛类型") @RequestParam(required = false) String type) {
        return awardDetailService.getAwardDetailsPage1(page, size, year, type);
    }

    @ApiOperation("获取对应级别近4年获奖情况")
    @GetMapping("/getAwardDetailstypeId")
    public AwardYearVO getAwardDetailstypeId(
            @ApiParam(value = "比赛类型") @RequestParam(required = false) Integer typeId
    ) {
        return awardDetailService.getAwardDetailstypeId(typeId);
    }

    @ApiOperation(value = "获取企业与岗位关联列表（分页）")
    @GetMapping("/positionsList")
    public Page<JobEnterprisePositionVO> getJobEnterprisePositionsPage(
            @ApiParam(value = "页码", required = false) @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @ApiParam(value = "每页大小", required = false) @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @ApiParam(value = "岗位类型") @RequestParam(required = false) String positionType
    ) {
        return jobEnterprisePositionService.getJobEnterprisePositionsPage1(pageNum, pageSize,positionType);
    }


    @GetMapping("/ranking")
    @ApiOperation("所属行业占比排行")
    public List<IndustryRankVO> getIndustryRanking(@RequestParam int size) {
        return industryService.getIndustryRanking(size);
    }

    /**
     * 根据parkId查询创业园信息
     */
    @ApiOperation("根据parkId查询创业园信息")
    @GetMapping("/get/{parkId}")
    public StudentEntrepreneurshipPark get(@PathVariable Integer parkId) {
        return parkService.getById(parkId);
    }

    @ApiOperation("获取coordinate有值的企业列表")
    @GetMapping("/withCoordinate")
    public List<EnterpriseVO> getEnterprisesWithCoordinate() {
        return enterpriseService.getEnterprisesWithCoordinate();
    }

    @GetMapping("/enterprise-count-by-park")
    public Map<String, Integer> getEnterpriseCountByPark() {
        return enterpriseService.getEnterpriseCountByPark();
    }

    @ApiOperation("获取每个公司状态及其对应的企业数量")
    @GetMapping("/enterprise-count-by-status")
    public Map<String, Integer> getEnterpriseCountByCompanyStatus() {
        return enterpriseService.getEnterpriseCountByCompanyStatus();
    }








    // 处理聊天请求
    @ApiOperation("处理聊天请求")
    @PostMapping(value = "/completions", produces = MediaType.APPLICATION_JSON_VALUE)
    public StreamingResponseBody getChatCompletion(@RequestParam String content) {
        return outputStream -> {
            try {
                String accessToken = chatService.getAccessToken();
                System.out.println("Access Token: " + accessToken);

                URL url = new URL("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/ernie-4.0-turbo-128k?access_token=" + accessToken);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String jsonInputString = String.format("{\"messages\": [{\"role\":\"user\", \"content\": \"%s\"}], \"stream\": true}", content);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        // 去除前缀 "data: "（如果有的话）
                        if (responseLine.startsWith("data: ")) {
                            responseLine = responseLine.substring(5).trim();
                        }

                        // 如果是空行或者心跳包，跳过
                        if (responseLine.isEmpty() || responseLine.equals("[DONE]")) {
                            continue;
                        }

                        // 解析每一行的JSON对象
                        JSONObject jsonLine = JSON.parseObject(responseLine);
                        boolean isEnd = jsonLine.getBooleanValue("is_end");

                        // 累积结果
                        if (jsonLine.containsKey("result")) {
                            String result = jsonLine.getString("result");
                            outputStream.write(result.getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        }

                        // 如果是最后一行，跳出循环
                        if (isEnd) {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }



}
