package com.tongtech.web.controller.console;

import com.tongtech.common.core.controller.BaseController;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.domain.ServiceStat;
import com.tongtech.console.domain.vo.RdsMonitorQueryVo;
import com.tongtech.console.domain.vo.RdsNodeStatQueryVo;
import com.tongtech.console.domain.vo.ServiceNodeStatVo;
import com.tongtech.console.domain.vo.ServiceStatVo;
import com.tongtech.console.service.CenterStatSrcService;
import com.tongtech.console.service.NodeStatService;
import com.tongtech.console.service.RdsServiceService;
import com.tongtech.console.service.ServiceStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * RDS服务Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
@RestController
@RequestMapping("/web-api/console/rdsmonitor")
public class RdsMonitorController extends BaseController {
    @Autowired
    private ServiceStatService servStatService;

    @Autowired
    private NodeStatService nodeStatService;

    @Autowired
    private CenterStatSrcService srcService;

    @Resource
    private RdsServiceService rdsServiceService;

    /**
     * 查询最新的ServiceStat列表，其中children包含子节点
     *
     * @param param 其中只有 name(ServiceName) 可作为模糊匹配的查询条件
     * @return
     */
    @GetMapping("/listServiceStat")
    public AjaxResult listServiceStat(ServiceStatVo param) {
        Long srcId = srcService.selectLastSrcId();
        param.setSrcId(srcId);

        List<ServiceStat> serviceStats = servStatService.selectServiceStatList(param);
        List<ServiceStatVo> result = new ArrayList<>(serviceStats.size());

        for (ServiceStat servStat : serviceStats) {
            List<NodeStat> nodes = nodeStatService.selectNodeStatList(new NodeStat(srcId, servStat.getServiceId()));
            //System.out.println("~~~~nodes:" + nodes);
            ServiceStatVo vo = new ServiceStatVo(servStat);
            vo.setChildren(nodes);
            // 进行状态条件的筛选
            if (param.getStatus() != null) {
                if (param.getStatus().equals(vo.getStatus())) {
                    result.add(vo);
                }
            } else {
                result.add(vo);
            }
        }

        return AjaxResult.success(result);
    }


    /**
     *
     */
    @GetMapping("/serviceNodes")
    public AjaxResult getServiceNodes(RdsMonitorQueryVo queryVo) {
        //过去的秒数计算查询参数
        if (queryVo.getBeginCreateSecond() == null && queryVo.getPastSecond() != null) {
            long beginSecond = (System.currentTimeMillis() / 1000) - queryVo.getPastSecond();
            queryVo.setBeginCreateSecond(beginSecond);
        }

        return AjaxResult.success(servStatService.selectSummaryServiceStat(queryVo));
    }


    @GetMapping("/listServiceNodeStat")
    AjaxResult listServiceNodeStat(RdsNodeStatQueryVo queryVo) {
        ServiceNodeStatVo result = servStatService.selectServiceNodeStatGroup(queryVo);
        return Objects.isNull(result) ? AjaxResult.error("服务不存在！") : AjaxResult.success(result);
    }

}
