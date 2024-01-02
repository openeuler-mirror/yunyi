import request from '@/utils/request'

// 获取节点信息列表（不分页）
export function listNode(query) {
  return request({
    url: '/console/rdsnode/list',
    method: 'get',
    params: query
  })
}

export function listSameNameNode(query) {
  return request({
    url: '/console/rdsnode/listSameName',
    method: 'get',
    params: query
  })
}

export function listSamePortNode(query) {
  return request({
    url: '/console/rdsnode/listSamePort',
    method: 'get',
    params: query
  })
}

// 查询节点信息列表(分页方式)
export function listPagingNode(query) {
  return request({
    url: '/console/rdsnode/listPaging',
    method: 'get',
    params: query
  })
}

// 查询节点信息详细
export function getNode(nodeId) {
  return request({
    url: '/console/rdsnode/' + nodeId,
    method: 'get'
  })
}

// 新增节点信息
export function addNode(data) {
  return request({
    url: '/console/rdsnode',
    method: 'post',
    data: data
  })
}

// 修改节点信息
export function updateNode(data) {
  return request({
    url: '/console/rdsnode',
    method: 'put',
    data: data
  })
}

// 删除角色
export function delNode(nodeIds) {
  //console.log("in the delNode()", nodeIds);
  return request({
    url: '/console/rdsnode/' + nodeIds,
    method: 'delete'
  })
}


export function startNode(nodeId) {
  return request({
    url: '/console/rdsnode/start/' + nodeId,
    method: 'get'
  })
}

export function stopNode(nodeId) {
  return request({
    url: '/console/rdsnode/stop/' + nodeId,
    method: 'get'
  })
}

export function restartNode(nodeId) {
  return request({
    url: '/console/rdsnode/restart/' + nodeId,
    method: 'get'
  })
}
