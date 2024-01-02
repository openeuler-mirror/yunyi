import request from '@/utils/request'

export function serviceConfigInfos() {
  return request({
    url: '/home/serviceConfigInfo',
    method: 'get'
  })
}

