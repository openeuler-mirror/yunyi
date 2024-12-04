

/**
 * 通用js方法封装处理
 * Copyright (c) 2019 ruoyi
 */

// 日期格式化
export function parseTime(time, pattern = '{y}-{m}-{d} {h}:{i}:{s}') {
  if (!time) return null;

  let date;
  
  // 判断 time 的类型并进行转换
  if (typeof time === 'object') {
    date = time;
  } else {
    if (typeof time === 'string') {
      if (/^\d+$/.test(time)) {
        time = parseInt(time, 10);
      } else {
        time = time
          .replace(/-/g, '/')
          .replace('T', ' ')
          .replace(/\.\d{3}/g, '');
      }
    }
    if (typeof time === 'number' && time.toString().length === 10) {
      time *= 1000;
    }
    date = new Date(time);
  }

  if (isNaN(date.getTime())) return null; // 无效日期校验

  // 格式化时间
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay(),
  };

  return pattern.replace(/{(y|m|d|h|i|s|a)+}/g, (match, key) => {
    let value = formatObj[key];
    if (key === 'a') return ['日', '一', '二', '三', '四', '五', '六'][value];
    return value < 10 && match.length > 0 ? `0${value}` : value;
  });
}


// 表单重置
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields();
  }
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  let search = params;
  search.params = typeof (search.params) === 'object' && search.params !== null && !Array.isArray(search.params) ? search.params : {};
  dateRange = Array.isArray(dateRange) ? dateRange : [];
  if (typeof (propName) === 'undefined') {
    search.params['beginTime'] = dateRange[0];
    search.params['endTime'] = dateRange[1];
  } else {
    search.params['begin' + propName] = dateRange[0];
    search.params['end' + propName] = dateRange[1];
  }
  return search;
}

// 回显数据字典
export function selectDictLabel(datas, value) {
  if (value === undefined) {
    return "";
  }
  var actions = [];
  Object.keys(datas).some((key) => {
    if (datas[key].value == ('' + value)) {
      actions.push(datas[key].label);
      return true;
    }
  })
  if (actions.length === 0) {
    actions.push(value);
  }
  return actions.join('');
}

// 回显数据字典（字符串数组）
export function selectDictLabels(datas, value, separator) {
  if (value === undefined) {
    return "";
  }
  var actions = [];
  var currentSeparator = undefined === separator ? "," : separator;
  var temp = value.split(currentSeparator);
  Object.keys(value.split(currentSeparator)).some((val) => {
    var match = false;
    Object.keys(datas).some((key) => {
      if (datas[key].value == ('' + temp[val])) {
        actions.push(datas[key].label + currentSeparator);
        match = true;
      }
    })
    if (!match) {
      actions.push(temp[val] + currentSeparator);
    }
  })
  return actions.join('').substring(0, actions.join('').length - 1);
}

// 字符串格式化(%s )
export function sprintf(str) {
  var args = arguments, flag = true, i = 1;
  str = str.replace(/%s/g, function () {
    var arg = args[i++];
    if (typeof arg === 'undefined') {
      flag = false;
      return '';
    }
    return arg;
  });
  return flag ? str : '';
}



// 转换字符串，undefined,null等转化为""
export function praseStrEmpty(str) {
  if (!str || str == "undefined" || str == "null") {
    return "";
  }
  return str;
}

// 数据合并
export function mergeRecursive(source, target) {
  if (!source || typeof source !== 'object') source = {};
  if (!target || typeof target !== 'object') return source;

  for (const key in target) {
    if (Object.prototype.hasOwnProperty.call(target, key)) {
      const targetValue = target[key];
      const sourceValue = source[key];

      if (targetValue && typeof targetValue === 'object' && !Array.isArray(targetValue)) {
        source[key] = mergeRecursive(sourceValue, targetValue);
      } else {
        source[key] = targetValue;
      }
    }
  }
  return source;
}

/**
 * 构造树型结构数据
 * @param {Array} data 数据源
 * @param {string} [id='id'] id字段
 * @param {string} [parentId='parentId'] 父节点字段
 * @param {string} [children='children'] 孩子节点字段
 * @returns {Array} 树型结构数据
 */
export function handleTree(data, id = 'id', parentId = 'parentId', children = 'children') {
  const config = { id, parentId, children };
  const childrenListMap = new Map();
  const nodeMap = new Map();
  const tree = [];

  // 构建节点映射和子节点映射
  for (const item of data) {
    const pid = item[config.parentId];
    const itemId = item[config.id];

    if (!childrenListMap.has(pid)) {
      childrenListMap.set(pid, []);
    }
    childrenListMap.get(pid).push(item);
    nodeMap.set(itemId, item);
  }

  // 构造树的根节点
  for (const item of data) {
    const pid = item[config.parentId];
    if (!nodeMap.has(pid)) {
      tree.push(item);
    }
  }

  // 递归组装子节点
  const buildTree = (node) => {
    const nodeId = node[config.id];
    if (childrenListMap.has(nodeId)) {
      node[config.children] = childrenListMap.get(nodeId);
      for (const child of node[config.children]) {
        buildTree(child);
      }
    }
  };

  for (const rootNode of tree) {
    buildTree(rootNode);
  }

  return tree;
}

/**
* 参数处理
* @param {*} params  参数
*/
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName];
    var part = encodeURIComponent(propName) + "=";
    if (value !== null && value !== "" && typeof (value) !== "undefined") {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== "" && typeof (value[key]) !== 'undefined') {
            let params = propName + '[' + key + ']';
            var subPart = encodeURIComponent(params) + "=";
            result += subPart + encodeURIComponent(value[key]) + "&";
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&";
      }
    }
  }
  return result
}

// 验证是否为blob格式
export async function blobValidate(data) {
  try {
    const text = await data.text();
    JSON.parse(text);
    return false;
  } catch (error) {
    return true;
  }
}
