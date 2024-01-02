import request from '@/utils/request';
import util from '@/utils/aesutils.js';

// 查询RDS服务列表，并附带子属性nodes列表
// 默认去除了中心服务和哨兵服务， 但当指定deployMode条件后，则只查询deployMode指定的部署模式
export function listServiceWithNodes(query) {
  return request({
    url: '/console/rdsservice/listWithNodes',
    method: 'get',
    params: query
  })
}

// 查询RDS服务列表
// 默认去除了中心服务和哨兵服务， 但当指定deployMode条件后，则只查询deployMode指定的部署模式
export function listService(query) {
  return request({
    url: '/console/rdsservice/list',
    method: 'get',
    params: query
  })
}


// 查询RDS服务详细
export function getService(serviceId) {
  return request({
    url: '/console/rdsservice/' + serviceId,
    method: 'get'
  })
}

// 查询RDS服务详细 + 下属的node列表
export function getServiceWithNodes(serviceId) {
  return request({
    url: '/console/rdsservice/serviceNodes/' + serviceId,
    method: 'get'
  })
}


// 新增RDS服务 + 下属的node列表
export function addServiceWithNodes(data) {
  let upData = JSON.parse(JSON.stringify(data));
  upData.service.password = util.encrypt(data.service.password);

  return request({
    url: '/console/rdsservice/serviceNodes',
    method: 'post',
    data: upData
  })
}

// 修改RDS服务 + 下属的node列表
export function updateServiceWithNodes(data) {
  let upData = JSON.parse(JSON.stringify(data));
  upData.service.password = util.encrypt(data.service.password);

  return request({
    url: '/console/rdsservice/serviceNodes',
    method: 'put',
    data: upData
  })
}

export function existsServiceName(serviceName) {
  return request({
    url: '/console/rdsservice/existName/' + serviceName  ,
    method: 'get'
  })
}

// 新增RDS服务
export function addService(data) {
  let upData = JSON.parse(JSON.stringify(data));
  upData.password = util.encrypt(data.password);


  return request({
    url: '/console/rdsservice',
    method: 'post',
    data: upData
  })
}

// 修改RDS服务
export function updateService(data) {
  let upData = JSON.parse(JSON.stringify(data));
  upData.password = util.encrypt(data.password);

  return request({
    url: '/console/rdsservice',
    method: 'put',
    data: data
  })
}

// 修改RDS服务密码
export function updateServicePassword(data) {
  return request({
    url: '/console/rdsservice/servicePassword',
    method: 'put',
    data: data
  })
}


// 删除RDS服务
export function delService(serviceId) {
  return request({
    url: '/console/rdsservice/' + serviceId,
    method: 'delete'
  })
}

/**
 * 为服务的所有节点重新下发配置信息
 * @param serviceId
 * @returns {AxiosPromise}
 */
export function resendService(serviceId) {
  return request({
    url: '/console/rdsservice/resend/' + serviceId,
    method: 'get'
  })
}

export function startService(serviceId) {
  return request({
    url: '/console/rdsservice/start/' + serviceId,
    method: 'get'
  })
}

export function stopService(serviceId) {
  return request({
    url: '/console/rdsservice/stop/' + serviceId,
    method: 'get'
  })
}

export function restartService(serviceId) {
  return request({
    url: '/console/rdsservice/restart/' + serviceId,
    method: 'get'
  })
}
