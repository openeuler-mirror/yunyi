import request from '@/utils/request'

// 查询安装包管理列表
export function getConfig() {
  return request({
    url: '/console/home/config',
    method: 'get'
  })
}
// 获取是否修改密码
export function getisEditPassword() {
  return request({
    url: '/console/home/passwordSuggestedChanges',
    method: 'get'
  })
}