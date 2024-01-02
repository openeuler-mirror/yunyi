import request from '@/utils/request'

// 查询服务列表
export function getRdsServiceList(data) {
  return request({
    url: '/console/rdsService/list',
    method: 'get',
    params: data
  })
}
// redis的db下拉框
export function rdsgetDB(data) {
  return request({
    url: '/console/rdsClient/getDB',
    method: 'post',
    data: data
  })
}
// String类型的新增key
export function rdssetKey(data) {
  return request({
    url: '/console/rdsClient/setKey',
    method: 'post',
    data: data
  })
}
// Hash类型的新增key
export function rdshset(data) {
  return request({
    url: '/console/rdsClient/hset',
    method: 'post',
    data: data
  })
}
// List类型的新增key
export function rdslpush(data) {
  return request({
    url: '/console/rdsClient/lpush',
    method: 'post',
    data: data
  })
}
// List类型的更新key
export function rdslset(data) {
  return request({
    url: '/console/rdsClient/lset',
    method: 'post',
    data: data
  })
}
//set新增key
export function rdssadd(data) {
  return request({
    url: '/console/rdsClient/sadd',
    method: 'post',
    data: data
  })
}
//stream新增key
export function rdsxadd(data) {
  return request({
    url: '/console/rdsClient/xadd',
    method: 'post',
    data: data
  })
}


// KeyList列表
export function rdskeyList(data) {
  return request({
    url: '/console/rdsClient/keyList',
    method: 'post',
    data: data
  })
}
//List类型详情
export function rdslrange(data) {
  return request({
    url: '/console/rdsClient/lrange',
    method: 'post',
    data: data
  })
}
//zset新增key
export function rdszadd(data) {
  return request({
    url: '/console/rdsClient/zadd',
    method: 'post',
    data: data
  })
}
// 选中每个key的类型
export function rdsgetType(data) {
  return request({
    url: '/console/rdsClient/getType',
    method: 'post',
    data: data
  })
}
// redis的String的getKey
export function rdsgetKey(data) {
  return request({
    url: '/console/rdsClient/getKey',
    method: 'post',
    data: data
  })
}
// redis的hash获取列表
export function rdshgetAll(data) {
  return request({
    url: '/console/rdsClient/hgetAll',
    method: 'post',
    data: data
  })
}

//redis的set详情
export function rdssmembers(data) {
  return request({
    url: '/console/rdsClient/smembers',
    method: 'post',
    data: data
  })
}
//zset类型详情
export function rdszsetList(data) {
  return request({
    url: '/console/rdsClient/zsetList',
    method: 'post',
    data: data
  })
}

//Stream类型详情
export function rdsstreamList(data) {
  return request({
    url: '/console/rdsClient/xrange',
    method: 'post',
    data: data
  })
}

//删除key
export function rdsdelKeys(data) {
  return request({
    url: '/console/rdsClient/delKeys',
    method: 'post',
    data: data
  })
}
export function rdshdel(data) {
  return request({
    url: '/console/rdsClient/hdel',
    method: 'post',
    data: data
  })
}
//stream删除行
export function rdstreamdel(data) {
  return request({
    url: '/console/rdsClient/xdel',
    method: 'post',
    data: data
  })
}
//list列表删除
export function rdslrem(data) {
  return request({
    url: '/console/rdsClient/lrem',
    method: 'post',
    data: data
  })
}
//set列表删除
export function rdssrem(data) {
  return request({
    url: '/console/rdsClient/srem',
    method: 'post',
    data: data
  })
}
//zset删除列表
export function rdszrem(data) {
  return request({
    url: '/console/rdsClient/zrem',
    method: 'post',
    data: data
  })
}

//命令行交互
export function rdsexecCommand(data) {
  return request({
    url: '/console/rdsClient/execCommand',
    method: 'post',
    data: data
  })
}
//命令行交互
export function rdsExpireTime(data) {
  return request({
    url: '/console/rdsClient/updateTtl',
    method: 'post',
    data: data
  })
}