
/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUsername(str) {
  const valid_map = ['admin', 'editor']
  return valid_map.indexOf(str.trim()) >= 0
}

/**
 * @param {string} url
 * @returns {Boolean}
 */
export function validURL(url) {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validLowerCase(str) {
  const reg = /^[a-z]+$/
  return reg.test(str)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validUpperCase(str) {
  const reg = /^[A-Z]+$/
  return reg.test(str)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function validAlphabets(str) {
  const reg = /^[A-Za-z]+$/
  return reg.test(str)
}

/**
 * @param {string} email
 * @returns {Boolean}
 */
export function validEmail(email) {
  const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
export function isString(str) {
  if (typeof str === 'string' || str instanceof String) {
    return true
  }
  return false
}

/**
 * @param {Array} arg
 * @returns {Boolean}
 */
export function isArray(arg) {
  if (typeof Array.isArray === 'undefined') {
    return Object.prototype.toString.call(arg) === '[object Array]'
  }
  return Array.isArray(arg)
}

/**
 * @param {string} 整数或者小数
 * @returns {Boolean}
 */
 export function validNumber(num) {
  const reg = /^[1-9][0-9]*([\.][0-9]{1,2})?$/
  return reg.test(num)
 }

/**
 * 校验中文名字
 * @param {any} rule
 * @param {any} value
 * @param {any} callback
 */
export function validChineseName(rule, value, callback) {
  let reg = /^([u4e00-\u9fa5\w]|[-_]){3,20}$/
  if (reg.test(value)) {
    callback()
  } else {
    callback(new Error('名字仅限中文／大小写英文字母／数字以及下划线，长度为3到20个字符'))
  }
}

/**
 * 校验IP
 * @param {any} rule
 * @param {any} value
 * @param {any} callback
 */
export function validIp(rule, value, callback) {
  let reg = /^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$/
  if (reg.test(value) || /[A-Za-z0-9]{6,20}/.test(value)) {
    callback()
  } else {
    callback(new Error('请输入正确的IP'))
  }
}

export function validatePassword(rule, value, callback) {
  if (value == null || value.length === 0) {
    callback(new Error('请输入密码'));
  } else if (/[a-zA-Z0-9_]{3,39}/.test(value) === false) {
    callback(new Error('密码仅限输入大小写英文字母／数字以及下划线，长度为4到39位'));
  } else {
    callback();
  }
};

export function validateStrengthPassword(rule, value, callback) {
  if (value == null || value.length === 0) {
    callback(new Error('请输入密码'));
  }
  else if (value.length < 6 || value.length > 39 ) {
    callback(new Error('密码长度在 6 到 39 个字符'));
  }
  else if(passwordStrength(value) < 3 ) {
    callback(new Error('密码强度不够！应同时包含数字、字母和特殊字符'));
  }
  else {
    callback();
  }
};

function passwordStrength(val){
  var lv = 0;
  if(val.match(/[a-zA-Z]/g)){lv++;}
  if(val.match(/[0-9]/g)){lv++;}
  if(val.match(/(.[^0-9a-zA-Z_])/g)){lv++;}
  if(lv > 3){lv=3;}
  return lv;
};


/**
 * 校验服务名
 * @param {any} serverName
 * @param {any} callback
 */
export function validServiceName(rule, value, callback) {
  let reg = /^[a-zA-Z]{1}([a-zA-Z0-9]|[-_]){2,19}$/
  if (reg.test(value)) {
    callback()
  } else {
    callback(new Error('必须以字母开头，仅限输入大小写字母/数字/-_，长度为3到20个'))
  }
}

/**
 * 对比两个对象，有哪些属性不同，返回值不相同的属性名称数组
 * @param {Object} obj
 * @param {Object} otherObj
 * @param {Array} skipProps
 */
export function getDiffProps(obj, otherObj, skipProps=['__ob__']) {
  let changed = [];
  for(var propName in obj) {
    if(skipProps.includes(propName))  continue;
    if(obj[propName] != otherObj[propName]) changed.push(propName);
  }
  return changed;
}

/**
 * 校验手机号是否符合格式
 * @param {string} phone - 需要校验的手机号
 * @param {RegExp} [regex] - 可选，自定义正则表达式，默认为中国大陆手机号规则
 * @returns {boolean} 是否为有效手机号
 */
export function validatePhoneNumber(phone, regex = /^1[3-9]\d{9}$/) {
  if (typeof phone !== 'string') {
    throw new Error('参数必须是字符串');
  }
  return regex.test(phone);
}