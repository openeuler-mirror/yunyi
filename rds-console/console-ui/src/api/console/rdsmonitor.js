import request from '@/utils/request'

// 查询RDS服务列表
export function listServiceStat(query) {
  return request({
    url: '/console/rdsmonitor/listServiceStat',
    method: 'get',
    params: query
  })
}

// 查询RDS服务列表,每个Service对象中附带nodes信息
export function getServiceNodes(query) {
  return request({
    url: '/console/rdsmonitor/serviceNodes',
    method: 'get',
    params: query
  })
}
