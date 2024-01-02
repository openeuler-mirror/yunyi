import request from '@/utils/request'

// 查询执行命令历史列表
export function listCommandhistory(query) {
  return request({
    url: '/console/commandhistory/list',
    method: 'get',
    params: query
  })
}

// 查询执行命令历史详细
export function getCommandhistory(historyId) {
  return request({
    url: '/console/commandhistory/' + historyId,
    method: 'get'
  })
}

// 新增执行命令历史
export function addCommandhistory(data) {
  return request({
    url: '/console/commandhistory',
    method: 'post',
    data: data
  })
}

// 修改执行命令历史
export function updateCommandhistory(data) {
  return request({
    url: '/console/commandhistory',
    method: 'put',
    data: data
  })
}

// 删除执行命令历史
export function delCommandhistory(historyId) {
  return request({
    url: '/console/commandhistory/' + historyId,
    method: 'delete'
  })
}
