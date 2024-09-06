package com.tongtech.web.controller.console;

import com.tongtech.common.core.controller.BaseController;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.domain.ServiceStat;
import com.tongtech.console.domain.vo.ServiceStatVo;
import com.tongtech.console.service.CenterStatSrcService;
import com.tongtech.console.service.NodeStatService;
import com.tongtech.console.service.ServiceStatService;
import com.tongtech.console.domain.vo.RdsMonitorQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * RDS服务Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
@RestController
@RequestMapping("/web-api/console/rdsmonitor")
public class RdsMonitorController extends BaseController
{
    @Autowired
    private ServiceStatService servStatService;

    @Autowired
    private NodeStatService nodeStatService;

    @Autowired
    private CenterStatSrcService srcService;

    /**
     * 查询最新的ServiceStat列表，其中children包含子节点
     *
     * @param param 其中只有 name(ServiceName) 可作为模糊匹配的查询条件
     * @return
     */
    @GetMapping("/listServiceStat")
    public AjaxResult listServiceStat(ServiceStatVo param)
    {
        Long srcId = srcService.selectLastSrcId();
        param.setSrcId(srcId);

        List<ServiceStat> serviceStats = servStatService.selectServiceStatList(param);
        List<ServiceStatVo> result = new ArrayList<>(serviceStats.size());

        List<NodeStat> nodeStatsList = nodeStatService.selectNodeStatList(new NodeStat(srcId));
        Map<Long, List<NodeStat>> nodeStatsMap = nodeStatsList.stream()
                .collect(Collectors.groupingBy(e -> e.getServiceId()));

        for(ServiceStat servStat : serviceStats) {
            List<NodeStat> nodes = nodeStatsMap.get(servStat.getServiceId());
            //System.out.println("~~~~nodes:" + nodes);
            ServiceStatVo vo = new ServiceStatVo(servStat);
            if(!CollectionUtils.isEmpty(nodes)){
                vo.setChildren(nodes);
            }
            // 进行状态条件的筛选
            if( param.getStatus() != null ) {
                if(param.getStatus().equals(vo.getStatus()) ) {
                    result.add(vo);
                }
            }
            else {
                result.add(vo);
            }
        }

        return AjaxResult.success(result);
    }


    /**
     *
     */
    @GetMapping("/serviceNodes")
    public AjaxResult getServiceNodes(RdsMonitorQueryVo queryVo)
    {
        //过去的秒数计算查询参数
        if(queryVo.getBeginCreateSecond() == null && queryVo.getPastSecond() != null) {
            long beginSecond = (System.currentTimeMillis() / 1000) - queryVo.getPastSecond();
            queryVo.setBeginCreateSecond(beginSecond);
        }

        return AjaxResult.success(servStatService.selectSummaryServiceStat(queryVo));
    }

}
