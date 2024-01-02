import request from '@/utils/request'

export function searchServer(data) {
  return request({
    url: '/api/monitor/server/list',
    method: 'get',
    params: data
  })
}
