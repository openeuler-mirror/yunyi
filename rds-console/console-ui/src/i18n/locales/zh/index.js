import systemZH from './system'
module.exports = {
  ...systemZH,
  search: '搜索',
  reset: '重置',
  add: '新增',
  update: '更新',
  edit: '修改',
  delete: '删除',
  export: '导出',
  import: '导入',
  refreshCache: '刷新缓存',
  actions: '操作',
  dialog: {
    // 确认
    confirm: '确认',
    submit: '提交',
    // 取消
    cancel: '取消',
    // 是否继续删除？
    confirmDelete: '是否确认删除参数编号为 {msg} 的数据项？',
    // 设为默认
    setDefault: '设为默认',
    // 添加成功
    addSuccess: '添加成功',
    // 修改成功
    editSuccess: '修改成功',
    deleteSuccess: '删除成功',
    refreshSuccess: '刷新成功'
  }
}
