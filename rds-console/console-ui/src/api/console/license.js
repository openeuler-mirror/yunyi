import request from '@/utils/request'

// 授权信息
export function getLicense() {
  return request({
    url: '/console/license/get',
    method: 'get'
  })
}

export function getTempLicense() {
  return request({
    url: '/console/license/getTemp',
    method: 'get'
  })
}

// 添加授权信息，把临时授权文件放入到中心节点目录
export function addLicense() {
  return request({
    url: '/console/license/add',
    method: 'get'
  })
}
