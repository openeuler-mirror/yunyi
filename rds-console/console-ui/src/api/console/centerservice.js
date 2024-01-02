import request from '@/utils/request'


// 查询RDS服务详细
export function getService(serviceId) {
  return request({
    url: '/console/centerservice/',
    method: 'get'
  })
}

// 修改中心服务
export function updateService(data) {
  return request({
    url: '/console/centerservice',
    method: 'put',
    data: data
  })
}

// 刷新所有节点的中心节点地址配置。对所有文件的中心节点地址进行刷新
export function resendCenterConfig() {
  return request({
    url: '/console/centerservice/resendCenterConfig',
    method: 'get'
  })
}

/**
 * 测试中心节点的管理端口是否可以正常连接
 */
export function testAdminConnection() {
  return request({
    url: '/console/centerservice/testAdminConnection',
    method: 'get'
  })
}

/**
 * 测试中心节点的管理端口是否可以正常连接
 */
export function testAdminConnectionNew(data) {
  return request({
    url: '/console/centerservice/testAdminConnectionNew',
    method: 'post',
    data: data
  })
}



/**
 * 清除所有中心节点，以及管理连接配置。
 * 只有在K8S模式下，才提供此操作。
 * 1.清空所有CenterNode, 2. 清空所有非手工维护的服务及下设节点。
 */
export function clearCenterConfig() {
  return request({
    url: '/console/centerservice/clearCenterConfig',
    method: 'get'
  })
}


