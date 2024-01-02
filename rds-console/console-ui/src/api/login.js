import request from '@/utils/request';
import util from '@/utils/aesutils.js';

// 登录方法
export function login(username, password, code, uuid) {
  const data = {
    username: username,
    password: util.encrypt(password),
    code: code,
    uuid: uuid
  }

  return request({
    url: '/login',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}
// 获取lic信息
export function getCenterLic1Install() {
  return request({
    url: '/warrant/getCenterLicInstall',
    method: 'get'
  })
}

// 注册方法
export function register(data) {
  return request({
    url: '/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

// 是否当前用户密码过期，如过期给出提示
export function getisEditPassword() {
  return request({
    url: '/passwordSuggest',
    method: 'get'
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/logout',
    method: 'post'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/captchaImage',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}
