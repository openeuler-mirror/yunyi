import systemEN from './system'

module.exports = {
  ...systemEN,
  search: 'search',
  reset: 'reset',
  add: 'add',
  update: 'update',
  edit: 'edit',
  delete: 'delete',
  export: 'export',
  import: 'import',
  refreshCache: 'refreshCache',
  actions: 'actions',
  dialog: {
    // 确认
    confirm: 'confirm',
    submit: 'submit',
    // 取消
    cancel: 'cancel',
    // 是否继续删除？
    confirmDelete: 'confirmDelete',
    // 设为默认
    setDefault: 'setDefault',
    // 添加成功
    addSuccess: 'addSuccess',
    // 修改成功
    editSuccess: 'editSuccess',
    deleteSuccess: 'deleteSuccess',
    refreshSuccess: 'refreshSuccess'
  }
}
