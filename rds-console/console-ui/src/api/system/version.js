import request from '@/utils/request'

// 查询安装包信息列表
export function listPackage(query) {
  return request({
    url: '/system/package/list',
    method: 'get',
    params: query
  })
}

// 查询安装包信息详细
export function getPackage(packageId) {
  return request({
    url: '/system/package/' + packageId,
    method: 'get'
  })
}

// 新增安装包信息
export function addPackage(data) {
  return request({
    url: '/system/package',
    method: 'post',
    data: data
  })
}

// 修改安装包信息
export function updatePackage(data) {
  return request({
    url: '/system/package',
    method: 'put',
    data: data
  })
}

// 删除安装包信息
export function delPackage(packageId) {
  return request({
    url: '/system/package/' + packageId,
    method: 'delete'
  })
}
